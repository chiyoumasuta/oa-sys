package cn.gson.oasys.service.impl;

import cn.gson.oasys.dao.LeaveApplicationDao;
import cn.gson.oasys.entity.LeaveApplication;
import cn.gson.oasys.entity.User;
import cn.gson.oasys.service.FlowableService;
import cn.gson.oasys.service.LeaveApplicationService;
import cn.gson.oasys.support.UserTokenHolder;
import cn.gson.oasys.support.UtilResultSet;
import com.alibaba.fastjson.JSONObject;
import org.flowable.engine.IdentityService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

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
        Map<String,Object> variables = new HashMap<>();
        Long dataKey = 0L;
        switch (type) {
            case "project_process":
                //项目标准化流程接口
            case "leave":
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
        }
        return true;
    }
}
