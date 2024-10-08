package cn.gson.oasys.controller;

import cn.gson.oasys.dao.LeaveApplicationDao;
import cn.gson.oasys.entity.LeaveApplication;
import cn.gson.oasys.entity.User;
import cn.gson.oasys.exception.ServiceException;
import cn.gson.oasys.support.FlowableApiUtils;
import cn.gson.oasys.service.*;
import cn.gson.oasys.support.UserTokenHolder;
import cn.gson.oasys.support.UtilResultSet;
import cn.gson.oasys.vo.TaskDTO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.codec.binary.Base64;
import org.flowable.bpmn.converter.BpmnXMLConverter;
import org.flowable.bpmn.model.BpmnModel;
import org.flowable.engine.*;
import org.flowable.engine.history.HistoricActivityInstance;
import org.flowable.engine.repository.Deployment;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.image.ProcessDiagramGenerator;
import org.flowable.task.api.Task;
import org.flowable.ui.common.model.UserRepresentation;
import org.flowable.ui.common.security.DefaultPrivileges;
import org.flowable.ui.modeler.domain.Model;
import org.flowable.ui.modeler.serviceapi.ModelService;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Flowable 相关接口
 * @author linjinp
 * @date 2019/10/31 10:55
*/
@RestController
@RequestMapping("/flowable")
@Api(tags = "流程通用接口")
public class FlowableController {
    @Resource
    private ProcessEngine processEngine;
    @Resource
    private RepositoryService repositoryService;
    @Resource
    private ModelService modelService;
    @Resource
    private IdentityService identityService;
    @Resource
    private RuntimeService runtimeService;
    @Resource
    private ActDeModelService actDeModelService;
    @Resource
    private ActReprocdefService actReprocdefService;
    @Resource
    private FlowableApiUtils flowableApiUtils;
    @Resource
    private TaskService taskService;
    @Resource
    private HistoryService historyService;
    @Resource
    private LeaveApplicationDao leaveApplicationDao;
    @Resource
    private UserService userService;
    @Resource
    private DepartmentService departmentService;
    @Resource
    FlowableService flowableService;
    @Resource LeaveApplicationService leaveApplicationService;

    //获取默认的管理员信息
//    @RequestMapping(value = "/rest/account", method = RequestMethod.GET, produces = "application/json")
    @ApiOperation(value = "获取默认的管理员信息")
    public UserRepresentation getAccount() {
        UserRepresentation userRepresentation = new UserRepresentation();
        userRepresentation.setId("admin");
        userRepresentation.setEmail("admin@flowable.org");
        userRepresentation.setFullName("Administrator");
        userRepresentation.setFirstName("Administrator");
        List<String> privileges = new ArrayList<>();
        privileges.add(DefaultPrivileges.ACCESS_MODELER);
        privileges.add(DefaultPrivileges.ACCESS_IDM);
        privileges.add(DefaultPrivileges.ACCESS_ADMIN);
        privileges.add(DefaultPrivileges.ACCESS_TASK);
        privileges.add(DefaultPrivileges.ACCESS_REST_API);
        userRepresentation.setPrivileges(privileges);
        return userRepresentation;
    }

    //获取流程图
    @RequestMapping(value = "/getFlowDiagram",method = RequestMethod.POST)
    @ApiOperation(value = "获取流程图")
    public UtilResultSet getFlowDiagram(String processDefinedId) throws IOException {
        List<String> flows = new ArrayList<>();
        //获取流程图
        BpmnModel bpmnModel = repositoryService.getBpmnModel(processDefinedId);
        ProcessEngineConfiguration processEngineConfig = processEngine.getProcessEngineConfiguration();

        ProcessDiagramGenerator diagramGenerator = processEngineConfig.getProcessDiagramGenerator();
        InputStream in = diagramGenerator.generateDiagram(bpmnModel, "bmp", new ArrayList<>(), flows, processEngineConfig.getActivityFontName(),
                processEngineConfig.getLabelFontName(), processEngineConfig.getAnnotationFontName(), processEngineConfig.getClassLoader(), 1.0, true);

        // in.available()返回文件的字节长度
        byte[] buf = new byte[in.available()];
        // 将文件中的内容读入到数组中
        in.read(buf);
        // 进行Base64编码处理
        String base64Img =  new String(Base64.encodeBase64(buf));
        in.close();
        return UtilResultSet.success(base64Img);
    }


    /**
     * 流程部署
     *
     * @param modelId 流程ID，来自 ACT_DE_MODEL
     * @return
     */
    @RequestMapping(value = "/deploy", method = RequestMethod.POST)
    @ApiOperation("流程部署")
    public UtilResultSet deploy(String modelId) {
        // 根据模型 ID 获取模型
        Model modelData = modelService.getModel(modelId);
        byte[] bytes = modelService.getBpmnXML(modelData);
        if (bytes == null) {
            throw new ServiceException("模型数据为空，请先设计流程并成功保存，再进行发布");
        }
        BpmnModel model = modelService.getBpmnModel(modelData);
        if (model.getProcesses().isEmpty()) {
            throw new ServiceException("数据模型不符要求，请至少设计一条主线流程");
        }
        byte[] bpmnBytes = new BpmnXMLConverter().convertToXML(model);
        String processName = modelData.getName() + ".bpmn20.xml";

        // 部署流程
        Deployment deploy = repositoryService.createDeployment()
                .name(modelData.getName())
                .addBytes(processName, bpmnBytes)
                .deploy();
        System.out.println("deploy.getId() = " + deploy.getId());
        System.out.println("deploy.getName() = " + deploy.getName());
        System.out.println("deploy.getCategory() = " + deploy.getCategory());
        return UtilResultSet.success("流程部署成功：" + modelId + " " + new Date());
    }

    /**
     * 启动流程
     *
     * @param deployId 部署的流程 Id，来自 ACT_RE_PROCDEF
     * @return
     */
    @RequestMapping(value = "/start", method = RequestMethod.POST)
    @ApiOperation(value = "启动流程 流程实例化接口 ",notes = "type：project_process(\"项目管理\") leave(\"请假审批\")")
    public UtilResultSet start(String deployId, String dateJson, String type) {
        try {
            if (flowableService.start(deployId, dateJson, type)){
                return UtilResultSet.success("流程实例化成功");
            }return UtilResultSet.bad_request("流程实例化失败");
        } catch (Exception e){
            throw  new ServiceException(e.getMessage());
        }
    }

    @RequestMapping(value = "/getActDeModels",method = RequestMethod.POST)
    @ApiOperation(value = "获取未部署流程列表")
    public UtilResultSet getActDeModels() {
        return UtilResultSet.success(actDeModelService.getActDeModels());
    }

    @RequestMapping(value = "getProcdefList",method = RequestMethod.POST)
    @ApiOperation(value = "获取已部署列表")
    public UtilResultSet getProcdefList() {
        return UtilResultSet.success(actReprocdefService.getActReprocdef());
    }

    @RequestMapping(value = "/deleteProcess",method = RequestMethod.POST)
    @ApiOperation(value = "中止流程")
    public UtilResultSet deleteProcess(String processId) {
        flowableApiUtils.deleteProcess(processId);
        return UtilResultSet.success("终止成功");
    }

    /**
     * 流程列表查询
     * @param searchType 流程列表 1.待我审核 2.全部流程
     */
    @RequestMapping(value = "/getInstantiateList",method = RequestMethod.POST)
    @ApiOperation(value = "获取流程实例化列表")
    public UtilResultSet getInstantiateList(String searchType,String type) {
        // 返回包装后的列表
        return UtilResultSet.success(flowableService.getInstantiateList(searchType,type));
    }

    /**
     * 查询个人任务
     */

    @Test
    public void createTaskQuery() {
        String assignee = "张三";
        String processDefinitionKey = "leave";
        List<Task> list = taskService.createTaskQuery()
                .taskAssignee(assignee)
                .processDefinitionKey(processDefinitionKey).list();
        list.forEach(v -> System.out.println(v.getId() + " "
                + v.getName() + " " + v.getTaskDefinitionKey()
                + " " + v.getExecutionId() + " " + v.getProcessInstanceId() + " " + v.getCreateTime()));
    }

    /**
     * 流程审批
     */
    @RequestMapping(value = "/audit",method = RequestMethod.POST)
    @ApiOperation(value = "流程审核接口")
    public UtilResultSet audit(String taskId,String result){
        try {
            if (flowableService.audit(taskId,result)){
                return UtilResultSet.success("审批成功");
            }else return UtilResultSet.bad_request("审批失败");
        } catch (Exception e){
            throw new ServiceException(e.getMessage());
        }
    }

    /**
     * 根据实例化流程id获取流程实例图
     * @param taskId 实例化流程id
     */
    @RequestMapping(value = "/getTaskProcessDiagram",method = RequestMethod.POST)
    @ApiOperation(value = "根据实例化id获取流程图实例（对流程图进行处理标明执行情况）")
    public void getTaskProcessDiagram(String taskId, HttpServletResponse httpServletResponse){
        flowableApiUtils.getTaskProcessDiagram(taskId,httpServletResponse);
    }

    /**
     * 查看审批历史
     * @param processInstanceId 实例化流程id
     */
    @RequestMapping(value = "/getHistoryList",method = RequestMethod.POST)
    @ApiOperation(value = "查看当前实例化流程审批历史")
    public UtilResultSet getHistoryList(String processInstanceId) {
        List<HistoricActivityInstance> historyList = flowableApiUtils.getHistoryList(processInstanceId);
        if (historyList.isEmpty()) {
            return UtilResultSet.bad_request("当前流程无审批记录");
        }else return UtilResultSet.success(historyList);
    }
}
