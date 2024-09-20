package cn.gson.oasys.service.impl;

import cn.gson.oasys.service.FlowableUserService;
import org.flowable.engine.IdentityService;
import org.flowable.engine.ProcessEngine;
import org.flowable.engine.ProcessEngines;
import org.flowable.idm.api.Group;
import org.springframework.stereotype.Service;

@Service
public class FlowableUserServiceImpl implements FlowableUserService {
    //维护用户
    @Override
    public void createUser(String name) {
//        ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
//        // 通过 IdentityService 完成相关的用户和组的管理
//        IdentityService identityService = processEngine.getIdentityService();
//
//        org.flowable.idm.api.User user = null;
//        for (int i = 1; i <= 3; i++) {
//            user = identityService.newUser(name);
//            user.setFirstName(name);
//            identityService.saveUser(user);
//        }
    }

    //维护用户组
    @Override
    public void createGroup(String groupName,String name,String type) {
        ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
        // 通过 IdentityService 完成相关的用户和组的管理
        IdentityService identityService = processEngine.getIdentityService();
        Group group = identityService.newGroup(groupName);
        group.setName(name);
        group.setType(type);
        identityService.saveGroup(group);

    }


    //用户和用户组关联
    @Override
    public void userGroup(String userId,String groupName) {
        ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
        IdentityService identityService = processEngine.getIdentityService();
        Group group = identityService.createGroupQuery().groupId(groupName).singleResult();
        identityService.createMembership(userId, group.getId());
    }
}
