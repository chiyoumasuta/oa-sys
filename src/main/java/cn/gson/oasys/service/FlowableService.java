package cn.gson.oasys.service;

import cn.gson.oasys.support.UtilResultSet;
import cn.gson.oasys.vo.TaskDTO;
import org.flowable.engine.history.HistoricActivityInstance;
import org.flowable.task.api.Task;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface FlowableService {

    /**
     * 审核
     * @param taskId
     * @param result
     * @return
     */
    boolean audit(String taskId,boolean isPass, String result);


    /**
     * 实例化流程
     */
    boolean start(String deployId, String dateJson, String type);

    /**
     * 获取任务列表
     */
    List<TaskDTO> getInstantiateList(String searchType,String type);

    /**
     * 获取任务的候选组信息
     * @param taskId 任务的 ID，来自 ACT_RU_TASK
     * @return 组信息列表
     */
    List<String> taskInfo(String taskId);

    /**
     * 设置任务的变量参数
     * @param taskId 任务的 ID
     * @param map 任务所需的参数键值对
     */
    void setVariables(String taskId, Map<String, Object> map);

    /**
     * 设置任务的单个变量参数
     * @param taskId 任务的 ID
     * @param key 参数的键
     * @param value 参数的值
     */
    void setVariable(String taskId, String key, Object value);

    /**
     * 设置任务的列表变量参数
     * @param taskId 任务的 ID
     * @param key 参数的键
     * @param value 参数的值（列表类型）
     */
    void setListVariable(String taskId, String key, List<String> value);

    /**
     * 处理任务
     * @param taskId 任务的 ID，来自 ACT_RU_TASK
     */
    void task(String taskId);

    /**
     * 指定审核人并处理任务
     * @param taskId 任务的 ID，来自 ACT_RU_TASK
     * @param assignee 设置的审核人
     * @param map 完成任务所需的条件参数
     * @return 任务是否成功处理
     */
    boolean taskByAssignee(String taskId, String assignee, Map<String, Object> map);

    /**
     * 中止流程
     */
    void deleteProcess(Task task, String result);

    /**
     * 获取当前正在运行的流程实例的业务 ID 列表
     * @return 业务 ID 列表
     */
    List<String> getRuntimeDataId();

    /**
     * 根据用户 ID 获取需要审核的业务键列表
     * @param userId 用户的 ID
     * @param type 流程类型
     * @return 业务键数据列表
     */
    List<Map<String, Object>> getRuntimeBusinessKeyByUser(String userId, String type);

    /**
     * 根据组 ID 获取需要审核的业务键列表
     * @param groupIds 组的 ID 列表
     * @return 业务键数据列表
     */
    List<Map<String, Object>> getRuntimeBusinessKeyByGroup(List<String> groupIds);

    /**
     * 获取用户审核历史
     * @param userId 用户的 ID
     * @return 历史审核记录列表
     */
    List<Map<String, Object>> getHistoryByUser(String userId);

    /**
     * 检查流程实例是否结束
     * @param processInstanceId 流程实例的 ID
     * @return true 表示结束，false 表示未结束
     */
    boolean checkProcessInstanceFinish(String processInstanceId);

    /**
     * 获取任务的流程实例 ID
     * @param taskId 任务节点的 ID
     * @return 流程实例 ID
     */
    String getTaskInfo(String taskId);

    /**
     * 根据任务 ID 获取任务的流程进度图
     * @param taskId 任务节点的 ID
     * @param httpServletResponse 响应对象，用于输出流程图
     */
    void getTaskProcessDiagram(String taskId, HttpServletResponse httpServletResponse);

    /**
     * 获取未实例化的流程图（流程定义）
     * @param processDefinedId 流程定义的 ID
     * @return 流程图的 Base64 编码字符串
     * @throws IOException IO 异常
     */
    String getFlowDiagram(String processDefinedId) throws IOException;

    /**
     * 获取流程实例的历史活动列表
     * @param processInstanceId 流程实例的 ID
     * @return 历史活动实例列表
     */
    List<HistoricActivityInstance> getHistoryList(String processInstanceId);
}
