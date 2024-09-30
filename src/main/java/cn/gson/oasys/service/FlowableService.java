package cn.gson.oasys.service;

import cn.gson.oasys.support.UtilResultSet;
import cn.gson.oasys.vo.TaskDTO;
import javafx.concurrent.Task;

import java.util.List;

public interface FlowableService {

    /**
     * 审核
     * @param taskId
     * @param result
     * @return
     */
    boolean audit(String taskId, String result);


    /**
     * 实例化流程
     */
    boolean start(String deployId, String dateJson, String type);

    /**
     * 获取任务列表
     */
    List<TaskDTO> getInstantiateList(String searchType,String type);
}
