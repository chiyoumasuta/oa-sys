package cn.gson.oasys.service;

import cn.gson.oasys.entity.LeaveApplication;

public interface LeaveApplicationService {

    /**
     * 审核接口
     * @param id
     * @param result
     * @return
     */
    boolean audit(String id,String result);

    /**
     * 通过processInstanceId获取业务数据代码
     */
    LeaveApplication getLeaveApplication(String id);
}
