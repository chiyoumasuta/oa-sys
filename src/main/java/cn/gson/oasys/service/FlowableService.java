package cn.gson.oasys.service;

import cn.gson.oasys.support.UtilResultSet;

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
}
