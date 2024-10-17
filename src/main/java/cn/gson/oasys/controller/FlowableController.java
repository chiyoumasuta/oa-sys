package cn.gson.oasys.controller;

import cn.gson.oasys.support.exception.ServiceException;
import cn.gson.oasys.service.*;
import cn.gson.oasys.support.UtilResultSet;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.flowable.bpmn.converter.BpmnXMLConverter;
import org.flowable.bpmn.model.BpmnModel;
import org.flowable.engine.*;
import org.flowable.engine.history.HistoricActivityInstance;
import org.flowable.engine.repository.Deployment;
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
import java.util.*;

/**
 * Flowable 相关接口
 *
 * @author linjinp
 * @date 2019/10/31 10:55
 */
@RestController
@RequestMapping("/flowable")
@Api(tags = "流程通用接口")
public class FlowableController {
    @Resource
    private RepositoryService repositoryService;
    @Resource
    private ModelService modelService;
    @Resource
    private ActDeModelService actDeModelService;
    @Resource
    private ActReprocdefService actReprocdefService;
    @Resource
    private TaskService taskService;
    @Resource
    FlowableService flowableService;

    @RequestMapping(value = "/rest/account", method = RequestMethod.GET, produces = "application/json")
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

    @RequestMapping(value = "/deploy", method = RequestMethod.POST)
    @ApiImplicitParams({
            @ApiImplicitParam(name = "modelId", value = "流程ID，来自 ACT_DE_MODEL", required = true, dataType = "String")
    })
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
        System.out.println(
                "流程部署成功："+
                "\ndeploy.getId() = " + deploy.getId()+
                "\ndeploy.getName() = " + deploy.getName()+
                "\ndeploy.getCategory() = " + deploy.getCategory()
        );
        return UtilResultSet.success("流程部署成功：" + modelId + " " + new Date());
    }

    @RequestMapping(value = "/start", method = RequestMethod.POST)
    @ApiImplicitParams({
            @ApiImplicitParam(name = "deployId", value = "部署的流程Id,来自ACT_RE_PROCDEF", required = true, dataType = "String"),
            @ApiImplicitParam(name = "type", value = "project_process(项目管理) leave(请假审批) reimbursement_process(报销流程)", required = true, dataType = "String")
    })
    @ApiOperation(value = "启动流程 流程实例化接口 ")
    public UtilResultSet start(String deployId, String dateJson, String type) {
        if (flowableService.start(deployId, dateJson, type)) {
            return UtilResultSet.success("流程实例化成功");
        }
        return UtilResultSet.bad_request("流程实例化失败");
    }

    @RequestMapping(value = "/getActDeModels", method = RequestMethod.POST)
    @ApiOperation(value = "获取未部署流程列表")
    public UtilResultSet getActDeModels() {
        return UtilResultSet.success(actDeModelService.getActDeModels());
    }

    @RequestMapping(value = "getProcdefList", method = RequestMethod.POST)
    @ApiOperation(value = "获取已部署列表")
    public UtilResultSet getProcdefList() {
        return UtilResultSet.success(actReprocdefService.getActReprocdef());
    }

    @RequestMapping(value = "/deleteProcess", method = RequestMethod.POST)
    @ApiOperation(value = "中止流程")
    public UtilResultSet deleteProcess(String taskId) {
        flowableService.deleteProcess(taskId);
        return UtilResultSet.success("终止成功");
    }

    @RequestMapping(value = "/getInstantiateList", method = RequestMethod.POST)
    @ApiOperation(value = "获取流程实例化列表")
    public UtilResultSet getInstantiateList(String searchType, String type) {
        return UtilResultSet.success(flowableService.getInstantiateList(searchType, type));
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

    @RequestMapping(value = "/audit", method = RequestMethod.POST)
    @ApiOperation(value = "流程审核接口")
    public UtilResultSet audit(String taskId, String result) {
        try {
            if (flowableService.audit(taskId, result)) {
                return UtilResultSet.success("审批成功");
            } else return UtilResultSet.bad_request("审批失败");
        } catch (Exception e) {
            throw new ServiceException(e.getMessage());
        }
    }

    @RequestMapping(value = "/getFlowDiagram", method = RequestMethod.POST)
    @ApiOperation(value = "获取流程图(未实例化)")
    public UtilResultSet getFlowDiagram(String processDefinedId) throws IOException {
        String base64Img = flowableService.getFlowDiagram(processDefinedId);
        return UtilResultSet.success(base64Img);
    }

    @RequestMapping(value = "/getTaskProcessDiagram", method = RequestMethod.GET)
    @ApiOperation(value = "根据实例化id获取流程图实例（标明执行情况）")
    public void getTaskProcessDiagram(String taskId, HttpServletResponse httpServletResponse) {
        flowableService.getTaskProcessDiagram(taskId, httpServletResponse);
    }

    @RequestMapping(value = "/getHistoryList", method = RequestMethod.POST)
    @ApiOperation(value = "查看当前实例化流程审批历史")
    public UtilResultSet getHistoryList(String taskId) {
        // 根据任务 ID 获取流程实例 ID
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        String processInstanceId = task.getProcessInstanceId();
        List<HistoricActivityInstance> historyList = flowableService.getHistoryList(processInstanceId);
        if (historyList.isEmpty()) {
            return UtilResultSet.bad_request("当前流程无审批记录");
        }
        return UtilResultSet.success(historyList);
    }

    @RequestMapping(value = "/checkProcessInstanceFinish", method = RequestMethod.POST)
    @ApiImplicitParams({
            @ApiImplicitParam(name = "processInstanceId", value = "部署的流程Id,来自ACT_RE_PROCDEF", required = true, dataType = "String"),
    })
    @ApiOperation(value = "检查流程实例是否结束")
    public UtilResultSet checkProcessInstanceFinish(String processInstanceId) {
        if (flowableService.checkProcessInstanceFinish(processInstanceId)) {
            return UtilResultSet.success("结束");
        }
        return UtilResultSet.success("未结束");
    }

    @RequestMapping(value = "/getRuntimeBusinessKeyByUser", method = RequestMethod.POST)
    @ApiImplicitParams({
            @ApiImplicitParam(name = "userId", value = "用户的ID/用户名", required = true, dataType = "String"),
            @ApiImplicitParam(name = "type", value = "流程类型", required = true, dataType = "String")
    })
    @ApiOperation("根据用户 ID 获取需要审核的业务键列表")
    List<Map<String, Object>> getRuntimeBusinessKeyByUser(String userId, String type) {
        return flowableService.getRuntimeBusinessKeyByUser(userId, type);
    }
}
