package cn.gson.oasys.flowable.utils;


import cn.gson.oasys.dao.ActRuTaskDao;
import cn.gson.oasys.dao.ProjectProcessDao;
import cn.gson.oasys.entity.flowable.ActRuTask;
import cn.gson.oasys.support.Page;
import cn.gson.oasys.vo.ProcessesVo;
import com.github.pagehelper.PageHelper;
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
import org.flowable.ui.modeler.serviceapi.ModelService;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;

/**
 * 示例代码，请勿在项目中使用
 * @author: linjinp
 * @create: 2019-11-05 14:55
 **/
@Service
public class FlowableApiUtils {

    // 流程引擎
    @Resource
    private ProcessEngine processEngine;
    // 用户以及组管理服务
    @Resource
    private IdentityService identityService;
    // 模型服务
    @Resource
    private ModelService modelService;
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
    @Resource
    private ActRuTaskDao actRuTaskDao;

    /**
     * 获取当前候选组
     *
     * @param taskId 任务 Id，来自 ACT_RU_TASK
     */
    public List<String> taskInfo(String taskId) {
        List<String> group = new ArrayList<>();
        List<IdentityLink> taskName = taskService.getIdentityLinksForTask(taskId);
        taskName.forEach(identityLink -> {
            group.add(identityLink.getGroupId());
        });
        return group;
    }

    /**
     * 设置任务参数
     *
     * @param taskId 任务ID
     * @param map 用户列表

     */
    public void setVariables(String taskId,Map<String ,Object> map) {
        String processInstanceId = taskService.createTaskQuery().taskId(taskId).singleResult().getProcessInstanceId();
        runtimeService.setVariables(processInstanceId, map);
    }

    /**
     * 设置任务参数
     *
     * @param taskId 任务ID
     * @param key 键
     * @param value 值
     */
    public void setVariable(String taskId,String key,Object value) {
        String processInstanceId = taskService.createTaskQuery().taskId(taskId).singleResult().getProcessInstanceId();
        runtimeService.setVariable(processInstanceId, key, value);
    }

    /**
     * 设置任务参数，List 使用
     *
     * @param taskId 任务ID
     * @param key 键
     * @param value 值

     */
    public void setListVariable(String taskId,String key,List<String> value) {
        String processInstanceId = taskService.createTaskQuery().taskId(taskId).singleResult().getProcessInstanceId();
        runtimeService.setVariable(processInstanceId, key, value);
    }

    /**
     * 任务处理1
     *
     * @param taskId 任务 Id，来自 ACT_RU_TASK
     */
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

    /**
     * 任务处理
     *
     * @param taskId   任务 Id，来自 ACT_RU_TASK
     * @param assignee 设置审核人，替换
     * @param map      完成任务需要的条件参数
     */
    public boolean taskByAssignee(String taskId,String assignee,Map<String, Object> map) {
        try {
            // 设置审核人
            taskService.setAssignee(taskId, assignee);
            // 设置任务参数，也可不设置：key value，只是示例
            // 带 Local 为局部参数，只适用于本任务，不带 Local 为全局任务，可在其他任务调用参数
            taskService.setVariableLocal(taskId, "status", true);
            // 完成任务
            taskService.complete(taskId, map);
            return true;
        }catch (Exception e) {
            return false;
        }
    }

    /**
     * 中止流程
     * @param processId 流程ID
     *
     */
    public void deleteProcess(String processId) {
        runtimeService.deleteProcessInstance(processId, "中止流程");
    }

    /**
     * 获取正在运行的数据 Id 列表
     */
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

    /**
     * 根据用户，获取需要审核的业务键 business_key 列表
     *
     * @param userId 用户 Id
     */
    public List<Map<String, Object>> getRuntimeBusinessKeyByUser(String userId,String type) {
        List<Map<String, Object>> idList = new ArrayList<>();
        // 根据用户获取正在进行的任务
        List<Task> tasks = taskService.createTaskQuery().taskAssignee(userId).list();
        for (Task task : tasks) {
            Map<String, Object> data = new HashMap<>();
            // 根据任务获取流程实例
            ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().processInstanceId(task.getProcessInstanceId()).singleResult();
            // 过滤需要的信息
            if (processInstance.getName().equals(type))continue;
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

    /**
     * 获取组，获取需要审核的业务键 business_key 列表
     *
     * @param groupIds 组 Id
     */
    public List<Map<String, Object>> getRuntimeBusinessKeyByGroup(List<String> groupIds) {
        List<Map<String, Object>> idList = new ArrayList<>();
        // 判断是否有组信息
        if (groupIds != null && groupIds.size() > 0) {
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

    /**
     * 获取用户审核历史
     *
     * @param userId 发起人 Id
     */
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

    /**
     * 通过流程实例 Id，判断流程是否结束
     *
     * @param processInstanceId 流程实例 Id
 true 结束，false 未结束
     */
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


    /**
     * 根据任务节点获取流程实例 Id
     *
     * @param taskId 任务节点 Id

     */
    public String getTaskInfo(String taskId) {
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        return task.getProcessInstanceId();
    }

    /**
     * 根据流程实例 ID 获取任务进度流程图
     *
     * @param processInstanceId 流程实例 Id

     */
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

    /**
     * 根据任务 ID 获取任务进度流程图
     *
     * @param taskId 任务节点 Id

     */
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

    //获取流程列表
    Page<ProcessesVo> getProcessesPage(int pageNo,int pageSize,String modelName ,String searchType){
        PageHelper.startPage(pageNo,pageSize);
        Example example  = new Example(ActRuTask.class);
        example.createCriteria().andEqualTo("");
        actRuTaskDao.selectByExample(example);
        return null;
    }

    // 查看历史
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