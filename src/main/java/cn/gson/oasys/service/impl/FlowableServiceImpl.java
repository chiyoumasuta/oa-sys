package cn.gson.oasys.service.impl;

import cn.gson.oasys.dao.LeaveApplicationDao;
import cn.gson.oasys.entity.LeaveApplication;
import cn.gson.oasys.entity.User;
import cn.gson.oasys.entity.reimbursement.Reimbursement;
import cn.gson.oasys.service.*;
import cn.gson.oasys.support.UserTokenHolder;
import cn.gson.oasys.vo.TaskDTO;
import com.alibaba.fastjson.JSONObject;
import org.flowable.engine.IdentityService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class FlowableServiceImpl implements FlowableService {

    @Resource
    private TaskService taskService;
    @Resource
    private LeaveApplicationService leaveApplicationService;
    @Resource
    private IdentityService identityService;
    @Resource
    private LeaveApplicationDao leaveApplicationDao;
    @Resource
    private RuntimeService runtimeService;
    @Resource
    private UserService userService;
    @Resource
    private DepartmentService departmentService;
    @Resource
    private ReimbursementService reimbursementService;

    /**
     * 审核
     *
     * @param taskId
     * @param result
     * @return
     */
    @Override
    public boolean audit(String taskId, String result) {
        // 领导审批
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        Map<String, Object> resultDataMap = new HashMap<>();
        resultDataMap.put("outcome", result);
        taskService.complete(task.getId(), resultDataMap);
        switch (task.getProcessDefinitionId().split(":")[0]) {
            case "leave":
                return leaveApplicationService.audit(task.getProcessInstanceId(), result);
            default:
                break;
        }

        return false;
    }

    @Override
    public boolean start(String deployId, String dateJson, String type) {
        User user = UserTokenHolder.getUser();
        // 设置发起人
        identityService.setAuthenticatedUserId(String.valueOf(user.getId()));
        // 根据流程 ID 启动流程
        switch (type) {
            case "project_process":
                //项目标准化流程接口
            case "leave":
                Long dataKey = 0L;
                Map<String,Object> variables = new HashMap<>();
                LeaveApplication data = JSONObject.toJavaObject(JSONObject.parseObject(dateJson), LeaveApplication.class);
                data.setCreatedAt(new Date());
                data.setStats("待审核");
                data.setDuration();
                leaveApplicationDao.insert(data);
                dataKey=data.getId();
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

    /**
     * 获取任务列表
     *
     * @param searchType
     */
    @Override
    public List<TaskDTO> getInstantiateList(String searchType,String type) {
        List<Task> taskList;
        User user = UserTokenHolder.getUser();

        // 根据 searchType 进行不同类型的查询
        if ("1".equals(searchType)) {
            // 查询待审核的任务，假设待审核任务与你用户相关的逻辑处理
            taskList = taskService.createTaskQuery()
//                    .active()
                    .taskCandidateOrAssigned(String.valueOf(user.getId()))
                    .list();
        } else {
            // 查询全部流程任务
            taskList = taskService.createTaskQuery()
//                    .active()
                    .list();
        }

        // 避免懒加载问题，将需要的字段包装到 DTO 中
        return taskList.stream().filter(it->it.getProcessDefinitionId().contains(type))
                .map(task -> {
                    TaskDTO taskDTO = new TaskDTO(task.getId(), task.getName(),
                            task.getTaskDefinitionKey(),
                            task.getExecutionId(),
                            task.getProcessInstanceId());
                    // 从数据库中获取与流程实例ID关联的业务数据
                    // 根据任务获取流程实例
                    ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().processInstanceId(task.getProcessInstanceId()).singleResult();
                    // 获取流程实例中的业务键
                    String processDefinitionKey = processInstance.getProcessDefinitionKey();
                    switch (processDefinitionKey){
                        case "leave":
                            LeaveApplication leaveApplication = leaveApplicationService.getLeaveApplication(processInstance.getBusinessKey());
                            leaveApplication.setInitiatorName(userService.findById(Long.valueOf(leaveApplication.getDepartment())).getUserName());
                            leaveApplication.setApproverName(userService.findById(Long.valueOf(leaveApplication.getApprover())).getUserName());
                            leaveApplication.setCcPersonName(userService.findByIds(leaveApplication.getCcPerson()).stream().map(User::getUserName).collect(Collectors.joining(",")));
                            leaveApplication.setDepartmentName(departmentService.findDepartmentById(leaveApplication.getDepartment()).get(0).getName());
                            // 将业务数据封装到DTO中
                            taskDTO.setBusinessData(leaveApplication);
                        case "reimbursement_process":
                            Reimbursement info = reimbursementService.getInfo(Long.valueOf(processInstance.getBusinessKey()));
                            taskDTO.setBusinessData(info);
                    }
                    return taskDTO;
                }).collect(Collectors.toList());
    }
}
