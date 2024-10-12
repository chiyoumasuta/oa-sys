package cn.gson.oasys.service.impl;

import cn.gson.oasys.dao.LeaveApplicationDao;
import cn.gson.oasys.dao.ProjectProcessDao;
import cn.gson.oasys.entity.LeaveApplication;
import cn.gson.oasys.entity.User;
import cn.gson.oasys.entity.reimbursement.Reimbursement;
import cn.gson.oasys.service.*;
import cn.gson.oasys.support.UserTokenHolder;
import cn.gson.oasys.vo.TaskDTO;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.flowable.bpmn.model.BpmnModel;
import org.flowable.engine.*;
import org.flowable.engine.history.HistoricActivityInstance;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.engine.runtime.Execution;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.identitylink.api.IdentityLink;
import org.flowable.image.ProcessDiagramGenerator;
import org.flowable.task.api.Task;
import org.flowable.task.api.history.HistoricTaskInstance;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class FlowableServiceImpl implements FlowableService {

    @Resource
    private LeaveApplicationService leaveApplicationService;
    @Resource
    private IdentityService identityService;
    @Resource
    private LeaveApplicationDao leaveApplicationDao;
    @Resource
    private UserService userService;
    @Resource
    private DepartmentService departmentService;
    @Resource
    private ReimbursementService reimbursementService;
    // 流程引擎
    @Resource
    private ProcessEngine processEngine;
    // 部署服务
    @Resource
    private RepositoryService repositoryService;
    // 流程实例服务
    @Resource
    private RuntimeService runtimeService;
    // 流程节点任务服务
    @Resource
    private TaskService taskService;
    // 历史数据服务
    @Resource
    private HistoryService historyService;
    @Resource
    private ProjectProcessDao projectProcessDao;

    @Override
    public boolean audit(String taskId, String result) {
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        String assignee = task.getAssignee();
        User user = UserTokenHolder.getUser();
        if (!assignee.equals(user.getUserName())) {
            return false;
        }
        Map<String, Object> resultDataMap = new HashMap<>();
        if (result != null) {
            resultDataMap.put("outcome", result);
        }
        taskService.complete(task.getId(), resultDataMap);
        ProcessInstance processInstance = runtimeService.createProcessInstanceQuery()
                .processInstanceId(task.getProcessInstanceId())
                .singleResult();

        // 业务键获取
        Long businessKey = Long.valueOf(processInstance.getBusinessKey());
        switch (task.getProcessDefinitionId().split(":")[0]) {
            case "leave":
                return leaveApplicationService.audit(businessKey, result);
            case "reimbursement_process":
                return reimbursementService.audit(businessKey, result);
            default:
                break;
        }
        return false;
    }

    @Override
    public boolean start(String deployId, String dateJson, String type) {
        switch (type) {
            case "project_process":
                //项目标准化流程接口
            case "leave":
                Long dataKey;
                Map<String, Object> variables = new HashMap<>();
                LeaveApplication data = JSONObject.toJavaObject(JSONObject.parseObject(dateJson), LeaveApplication.class);
                data.setCreatedAt(new Date());
                data.setStats("待审核");
                data.setDuration();
                leaveApplicationDao.insert(data);
                dataKey = data.getId();
                variables.put("studentUser", data.getInitiator());
                variables.put("teacherUser", data.getApprover());
                ProcessInstance processInstance = runtimeService.startProcessInstanceById(deployId, String.valueOf(dataKey), variables);
                processInstance.getProcessInstanceId();
                data.setProcessInstanceId(processInstance.getProcessInstanceId());
                leaveApplicationDao.updateByPrimaryKeySelective(data);
                return true;
            case "reimbursement_process":
                reimbursementService.start(deployId, dateJson);
        }
        return true;
    }

    @Override
    public List<TaskDTO> getInstantiateList(String searchType, String type) {
        List<Task> taskList;
        User user = UserTokenHolder.getUser();

        // 根据 searchType 进行不同类型的查询

        taskList = taskService.createTaskQuery()
                .active()
                .list();

        // 避免懒加载问题，将需要的字段包装到 DTO 中
        return taskList.stream().filter(it -> it.getProcessDefinitionId().contains(type))
                .map(task -> {
                    // 根据任务获取流程实例
                    ProcessInstance processInstance = runtimeService.createProcessInstanceQuery()
                            .processInstanceId(task.getProcessInstanceId())
                            .singleResult();

                    // 业务键获取
                    Long businessKey = Long.valueOf(processInstance.getBusinessKey());

                    TaskDTO taskDTO = new TaskDTO(
                            task.getId(),
                            task.getName(),
                            task.getTaskDefinitionKey(),
                            task.getExecutionId(),
                            task.getProcessInstanceId(),
                            businessKey
                    );

                    // 获取流程实例中的业务键
                    String processDefinitionKey = processInstance.getProcessDefinitionKey();
                    switch (processDefinitionKey) {
                        case "leave":
                            LeaveApplication leaveApplication = leaveApplicationService.getLeaveApplication(processInstance.getBusinessKey());
                            leaveApplication.setInitiatorName(userService.findById(Long.valueOf(leaveApplication.getDepartment())).getUserName());
                            leaveApplication.setApproverName(userService.findById(Long.valueOf(leaveApplication.getApprover())).getUserName());
                            leaveApplication.setCcPersonName(userService.findByIds(leaveApplication.getCcPerson()).stream().map(User::getUserName).collect(Collectors.joining(",")));
                            leaveApplication.setDepartmentName(departmentService.findDepartmentById(leaveApplication.getDepartment()).get(0).getName());
                            // 将业务数据封装到DTO中
                            taskDTO.setBusinessData(leaveApplication);
                        case "reimbursement_process":
                            Reimbursement info = reimbursementService.getInfo(businessKey,searchType);
                            // 将业务数据封装到DTO中
                            taskDTO.setBusinessData(info);
                    }
                    return taskDTO;
                }).filter(t->t.getBusinessData()!=null).collect(Collectors.toList());
    }

    @Override
    public List<String> taskInfo(String taskId) {
        List<String> group = new ArrayList<>();
        List<IdentityLink> taskName = taskService.getIdentityLinksForTask(taskId);
        taskName.forEach(identityLink -> group.add(identityLink.getGroupId()));
        return group;
    }

    @Override
    public void setVariables(String taskId, Map<String, Object> map) {
        String processInstanceId = taskService.createTaskQuery().taskId(taskId).singleResult().getProcessInstanceId();
        runtimeService.setVariables(processInstanceId, map);
    }

    @Override
    public void setVariable(String taskId, String key, Object value) {
        String processInstanceId = taskService.createTaskQuery().taskId(taskId).singleResult().getProcessInstanceId();
        runtimeService.setVariable(processInstanceId, key, value);
    }

    @Override
    public void setListVariable(String taskId, String key, List<String> value) {
        String processInstanceId = taskService.createTaskQuery().taskId(taskId).singleResult().getProcessInstanceId();
        runtimeService.setVariable(processInstanceId, key, value);
    }

    @Override
    public void task(String taskId) {
        Boolean isSuspended = taskService.createTaskQuery().taskId(taskId).singleResult().isSuspended();
        if (isSuspended) {
            return;
        }
        // 设置任务参数，也可不设置：key value
        // 带 Local 为局部参数，只适用于本任务，不带 Local 为全局任务，可在其他任务调用参数
        taskService.setVariableLocal(taskId, "status", true);
        // 完成任务
        taskService.complete(taskId);
    }

    @Override
    public boolean taskByAssignee(String taskId, String assignee, Map<String, Object> map) {
        try {
            // 设置审核人
            taskService.setAssignee(taskId, assignee);
            // 设置任务参数，也可不设置：key value，只是示例
            // 带 Local 为局部参数，只适用于本任务，不带 Local 为全局任务，可在其他任务调用参数
            taskService.setVariableLocal(taskId, "status", true);
            // 完成任务
            taskService.complete(taskId, map);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public void deleteProcess(String processId) {
        runtimeService.deleteProcessInstance(processId, "中止流程");
    }

    @Override
    public List<String> getRuntimeDataId() {
        List<String> idList = new ArrayList<>();
        // 获取正在执行的任务列表
        List<Execution> list = runtimeService.createExecutionQuery().onlyProcessInstanceExecutions().list();
        list.forEach(execution -> {
            // 根据任务获取流程实例
            // 获取流程实例种的业务 key
            ProcessInstance pi = runtimeService.createProcessInstanceQuery().processInstanceId(execution.getProcessInstanceId()).singleResult();
            idList.add(pi.getBusinessKey());
        });
        return idList;
    }

    @Override
    public List<Map<String, Object>> getRuntimeBusinessKeyByUser(String userId, String type) {
        List<Map<String, Object>> idList = new ArrayList<>();
        // 根据用户获取正在进行的任务
        List<Task> tasks = taskService.createTaskQuery().taskAssignee(userId).list();
        for (Task task : tasks) {
            Map<String, Object> data = new HashMap<>();
            // 根据任务获取流程实例
            ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().processInstanceId(task.getProcessInstanceId()).singleResult();
            // 过滤需要的信息
            if (processInstance.getName().equals(type)) continue;
            // 获取流程实例中的业务键
            data.put("businessKey", processInstance.getBusinessKey());
            // 获取任务 Id
            data.put("taskId", task.getId());
            // 流程定义名称
            data.put("processInstanceName", processInstance.getProcessDefinitionName());
            //zb TODO 获取业务数据
            switch (type) {
                case "项目管理":
                    data.put("businessData", projectProcessDao.selectByPrimaryKey(Long.valueOf(processInstance.getBusinessKey())));
                    break;
            }
            // 流程开始时间
            data.put("startTime", processInstance.getStartTime());
            idList.add(data);
        }
        return idList;
    }

    @Override
    public List<Map<String, Object>> getRuntimeBusinessKeyByGroup(List<String> groupIds) {
        List<Map<String, Object>> idList = new ArrayList<>();
        // 判断是否有组信息
        if (groupIds != null && !groupIds.isEmpty()) {
            // 根据发起人获取正在执行的任务列表
            List<Task> tasks = taskService.createTaskQuery().taskCandidateGroupIn(groupIds).list();
            tasks.forEach(task -> {
                Map<String, Object> data = new HashMap<>();
                // 根据任务获取流程实例
                ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().processInstanceId(task.getProcessInstanceId()).singleResult();
                // 获取流程实例中的业务键
                data.put("businessKey", processInstance.getBusinessKey());
                // 获取任务 Id
                data.put("taskId", task.getId());
                // 流程定义名称
                data.put("processInstanceName", processInstance.getProcessDefinitionName());
                // 流程开始时间
                data.put("startTime", processInstance.getStartTime());
                idList.add(data);
            });
        }
        return idList;
    }

    @Override
    public List<Map<String, Object>> getHistoryByUser(String userId) {
        List<Map<String, Object>> historyList = new ArrayList<>();
        // 根据用户，查询任务实例历史
        List<HistoricTaskInstance> list = historyService.createHistoricTaskInstanceQuery().taskAssignee(userId).finished().orderByHistoricTaskInstanceEndTime().desc().list();
        list.forEach(historicTaskInstance -> {
            // 历史流程实例
            HistoricProcessInstance hpi = historyService.createHistoricProcessInstanceQuery().processInstanceId(historicTaskInstance.getProcessInstanceId()).singleResult();
            // 获取需要的历史数据
            Map<String, Object> historyInfo = new HashMap<>();
            historyInfo.put("assignee", historicTaskInstance.getAssignee());
            // 节点名称
            historyInfo.put("nodeName", historicTaskInstance.getName());
            // 流程开始时间
            historyInfo.put("startTime", historicTaskInstance.getCreateTime());
            // 节点操作时间（本流程节点结束时间）
            historyInfo.put("endTime", historicTaskInstance.getEndTime());
            // 流程定义名称
            historyInfo.put("processName", hpi.getProcessDefinitionName());
            // 流程实例 ID
            historyInfo.put("processInstanceId", historicTaskInstance.getProcessInstanceId());
            // 业务键
            historyInfo.put("businessKey", hpi.getBusinessKey());
            historyList.add(historyInfo);
        });
        return historyList;
    }

    @Override
    public boolean checkProcessInstanceFinish(String processInstanceId) {
        boolean isFinish = false;
        // 根据流程 ID 获取未完成的流程中是否存在此流程
        long count = historyService.createHistoricProcessInstanceQuery().unfinished().processInstanceId(processInstanceId).count();
        // 不存在说明没有结束
        if (count == 0) {
            isFinish = true;
        }
        return isFinish;
    }

    @Override
    public String getTaskInfo(String taskId) {
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        return task.getProcessInstanceId();
    }

    @Override
    public void getProcessDiagram(String processInstanceId, HttpServletResponse httpServletResponse) {
        // 流程定义 ID
        String processDefinitionId;

        // 查看完成的进程中是否存在此进程
        long count = historyService.createHistoricProcessInstanceQuery().finished().processInstanceId(processInstanceId).count();
        if (count > 0) {
            // 如果流程已经结束，则得到结束节点
            HistoricProcessInstance pi = historyService.createHistoricProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();

            processDefinitionId = pi.getProcessDefinitionId();
        } else {// 如果流程没有结束，则取当前活动节点
            // 根据流程实例ID获得当前处于活动状态的ActivityId合集
            ProcessInstance pi = runtimeService.createProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();
            processDefinitionId = pi.getProcessDefinitionId();
        }
        List<String> highLightedActivitis = new ArrayList<>();

        // 获得活动的节点
        List<HistoricActivityInstance> highLightedActivitList = historyService.createHistoricActivityInstanceQuery().processInstanceId(processInstanceId).orderByHistoricActivityInstanceStartTime().asc().list();

        for (HistoricActivityInstance tempActivity : highLightedActivitList) {
            String activityId = tempActivity.getActivityId();
            highLightedActivitis.add(activityId);
        }

        List<String> flows = new ArrayList<>();
        // 获取流程图
        BpmnModel bpmnModel = repositoryService.getBpmnModel(processDefinitionId);
        ProcessEngineConfiguration processEngineConfig = processEngine.getProcessEngineConfiguration();

        ProcessDiagramGenerator diagramGenerator = processEngineConfig.getProcessDiagramGenerator();
        InputStream in = diagramGenerator.generateDiagram(bpmnModel, "bmp", highLightedActivitis, flows, processEngineConfig.getActivityFontName(),
                processEngineConfig.getLabelFontName(), processEngineConfig.getAnnotationFontName(), processEngineConfig.getClassLoader(), 1.0, true);
//        ByteArrayOutputStream output = new ByteArrayOutputStream();
//        byte[] buffer = new byte[1024*4];
//        int n;
//        while (-1 != (n = in.read(buffer))) {
//            output.write(buffer, 0, n);
//        }
//        byte[] imgData = output.toByteArray();
//        output.close();
//        in.close();
//
//        return imgData;
        OutputStream out = null;
        byte[] buf = new byte[1024];
        int legth;
        try {
            out = httpServletResponse.getOutputStream();
            while ((legth = in.read(buf)) != -1) {
                out.write(buf, 0, legth);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            IOUtils.closeQuietly(out);
            IOUtils.closeQuietly(in);
        }
    }

    @Override
    public void getTaskProcessDiagram(String taskId, HttpServletResponse httpServletResponse) {

        // 根据任务 ID 获取流程实例 ID
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        String processInstanceId = task.getProcessInstanceId();

        // 根据流程实例获取流程图
        // 流程定义 ID
        String processDefinitionId;

        // 查看完成的进程中是否存在此进程
        long count = historyService.createHistoricProcessInstanceQuery().finished().processInstanceId(processInstanceId).count();
        if (count > 0) {
            // 如果流程已经结束，则得到结束节点
            HistoricProcessInstance pi = historyService.createHistoricProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();

            processDefinitionId = pi.getProcessDefinitionId();
        } else {// 如果流程没有结束，则取当前活动节点
            // 根据流程实例ID获得当前处于活动状态的ActivityId合集
            ProcessInstance pi = runtimeService.createProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();
            processDefinitionId = pi.getProcessDefinitionId();
        }
        List<String> highLightedActivitis = new ArrayList<>();

        // 获得活动的节点
        List<HistoricActivityInstance> highLightedActivitList = historyService.createHistoricActivityInstanceQuery().processInstanceId(processInstanceId).orderByHistoricActivityInstanceStartTime().asc().list();

        for (HistoricActivityInstance tempActivity : highLightedActivitList) {
            String activityId = tempActivity.getActivityId();
            highLightedActivitis.add(activityId);
        }

        List<String> flows = new ArrayList<>();
        //获取流程图
        BpmnModel bpmnModel = repositoryService.getBpmnModel(processDefinitionId);
        ProcessEngineConfiguration processEngineConfig = processEngine.getProcessEngineConfiguration();

        ProcessDiagramGenerator diagramGenerator = processEngineConfig.getProcessDiagramGenerator();
        InputStream in = diagramGenerator.generateDiagram(bpmnModel, "bmp", highLightedActivitis, flows, processEngineConfig.getActivityFontName(),
                processEngineConfig.getLabelFontName(), processEngineConfig.getAnnotationFontName(), processEngineConfig.getClassLoader(), 1.0, true);

        OutputStream out = null;
        byte[] buf = new byte[1024];
        int legth;
        try {
            out = httpServletResponse.getOutputStream();
            while ((legth = in.read(buf)) != -1) {
                out.write(buf, 0, legth);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            IOUtils.closeQuietly(out);
            IOUtils.closeQuietly(in);
        }
    }

    @Override
    public String getFlowDiagram(String processDefinedId) throws IOException {
        List<String> flows = new ArrayList<>();
        //获取流程图
        BpmnModel bpmnModel = repositoryService.getBpmnModel(processDefinedId);
        ProcessEngineConfiguration processEngineConfig = processEngine.getProcessEngineConfiguration();

        ProcessDiagramGenerator diagramGenerator = processEngineConfig.getProcessDiagramGenerator();
        InputStream in = diagramGenerator.generateDiagram(
                bpmnModel, "bmp", new ArrayList<>(), flows,
                processEngineConfig.getActivityFontName(),
                processEngineConfig.getLabelFontName(),
                processEngineConfig.getAnnotationFontName(),
                processEngineConfig.getClassLoader(),
                1.0, true);
        // in.available()返回文件的字节长度
        byte[] buf = new byte[in.available()];
        // 将文件中的内容读入到数组中
        in.read(buf);
        // 进行Base64编码处理
        String base64Img = new String(Base64.encodeBase64(buf));
        in.close();
        return base64Img;
    }

    @Override
    public List<HistoricActivityInstance> getHistoryList(String processInstanceId) {
        List<HistoricActivityInstance> activities = historyService.createHistoricActivityInstanceQuery()
                .processInstanceId(processInstanceId)
                .finished()
                .orderByHistoricActivityInstanceEndTime().asc()
                .list();
        for (HistoricActivityInstance activity : activities) {
            System.out.println(activity.getActivityName());
        }
        return activities;
    }
}
