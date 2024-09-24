package cn.gson.oasys.service.impl;

import cn.gson.oasys.service.FlowableUserService;
import org.flowable.engine.IdentityService;
import org.flowable.engine.ProcessEngine;
import org.flowable.engine.ProcessEngines;
import org.flowable.idm.api.Group;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Arrays;

@Service
public class FlowableUserServiceImpl implements FlowableUserService {

    @Resource
    private IdentityService identityService;

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

    @Test
    public void identityServiceTest() {
        // 查询方法最终调用了 CustomUserQueryImpl,里面的测试数据有3个用户，对应ID:["1","2","3"]
        long result1 = identityService.createUserQuery().userId("10001").count();
        long result2 = identityService.createUserQuery().userId("10002").count();
        long result3 = identityService.createUserQuery().userIds(Arrays.asList("10001", "10002", "10004")).count();
        Assertions.assertEquals(1, result1);
        Assertions.assertEquals(0, result2);
        Assertions.assertEquals(2, result3);
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
