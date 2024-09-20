package cn.gson.oasys.controller;

import cn.gson.oasys.service.ActDeModelService;
import cn.gson.oasys.support.UtilResultSet;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.codec.binary.Base64;
import org.flowable.bpmn.converter.BpmnXMLConverter;
import org.flowable.bpmn.model.BpmnModel;
import org.flowable.engine.*;
import org.flowable.engine.repository.Deployment;
import org.flowable.image.ProcessDiagramGenerator;
import org.flowable.spring.boot.app.App;
import org.flowable.ui.common.model.UserRepresentation;
import org.flowable.ui.common.security.DefaultPrivileges;
import org.flowable.ui.modeler.domain.Model;
import org.flowable.ui.modeler.serviceapi.ModelService;
import org.hibernate.service.spi.ServiceException;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Flowable 相关接口
 * @author linjinp
 * @date 2019/10/31 10:55
*/
@RestController
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
        if (model.getProcesses().size() == 0) {
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
     * @param userId   用户 Id
     * @param dataKey  数据 Key，业务键，一般为表单数据的 ID，仅作为表单数据与流程实例关联的依据
     * @return
     */
//    @RequestMapping(value = "/start/{deployId}/{userId}/{dataKey}", method = RequestMethod.GET)
    public UtilResultSet start(@PathVariable(value = "deployId") String deployId, @PathVariable(value = "userId") String userId, @PathVariable(value = "dataKey") String dataKey) {
        // 设置发起人
        identityService.setAuthenticatedUserId(userId);
        // 根据流程 ID 启动流程
        runtimeService.startProcessInstanceById(deployId, dataKey);
        return UtilResultSet.success("流程启动成功：" + deployId + " " + new Date());
    }

    @RequestMapping(value = "/getActDeModels",method = RequestMethod.POST)
    @ApiOperation(value = "获取未部署流程列表")
    public UtilResultSet getActDeModels() {
        return UtilResultSet.success(actDeModelService.getActDeModels());
    }


}
