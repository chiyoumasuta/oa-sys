package cn.gson.oasys.service;

import org.flowable.engine.IdentityService;
import org.flowable.engine.ProcessEngine;
import org.flowable.engine.ProcessEngines;
import org.flowable.idm.api.Group;

/**
 * 维护flowable用户和用户组数据
 */
public interface FlowableUserService {
    //维护用户
    public void createUser(String name);

    //维护用户组
    public void createGroup(String groupName,String name,String type);

    //用户和用户组关联
    public void userGroup(String userId,String groupName);
}
