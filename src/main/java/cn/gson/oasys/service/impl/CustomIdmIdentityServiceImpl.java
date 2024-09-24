package cn.gson.oasys.service.impl;

import cn.gson.oasys.service.UserService;
import org.flowable.idm.api.*;
import org.flowable.idm.engine.impl.IdmIdentityServiceImpl;
import org.flowable.idm.engine.impl.UserQueryImpl;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.List;

/**
 * 重写flowable的idm管理器
 */
//@Service
public class CustomIdmIdentityServiceImpl implements IdmIdentityService {

    @Resource
    private UserService userService;

    /**
     * @param s
     * @return
     */
    @Override
    public User newUser(String s) {
        return null;
    }

    /**
     * @param user
     */
    @Override
    public void saveUser(User user) {

    }

    /**
     * @param user
     */
    @Override
    public void updateUserPassword(User user) {

    }

    /**
     * @return
     */
    @Override
    public UserQuery createUserQuery() {
        cn.gson.oasys.entity.User user = new cn.gson.oasys.entity.User();
        userService.saveOrUpdate(user);
        return new UserQueryImpl();
    }

    /**
     * @return
     */
    @Override
    public NativeUserQuery createNativeUserQuery() {
        return null;
    }

    /**
     * @param s
     */
    @Override
    public void deleteUser(String s) {

    }

    /**
     * @param s
     * @return
     */
    @Override
    public Group newGroup(String s) {
        return null;
    }

    /**
     * @return
     */
    @Override
    public GroupQuery createGroupQuery() {
        return null;
    }

    /**
     * @return
     */
    @Override
    public NativeGroupQuery createNativeGroupQuery() {
        return null;
    }

    /**
     * @param group
     */
    @Override
    public void saveGroup(Group group) {

    }

    /**
     * @param s
     */
    @Override
    public void deleteGroup(String s) {

    }

    /**
     * @param s
     * @param s1
     */
    @Override
    public void createMembership(String s, String s1) {

    }

    /**
     * @param s
     * @param s1
     */
    @Override
    public void deleteMembership(String s, String s1) {

    }

    /**
     * @param s
     * @param s1
     * @return
     */
    @Override
    public boolean checkPassword(String s, String s1) {
        return false;
    }

    /**
     * @param s
     */
    @Override
    public void setAuthenticatedUserId(String s) {

    }

    /**
     * @param s
     * @param picture
     */
    @Override
    public void setUserPicture(String s, Picture picture) {

    }

    /**
     * @param s
     * @return
     */
    @Override
    public Picture getUserPicture(String s) {
        return null;
    }

    /**
     * @param s
     * @return
     */
    @Override
    public Token newToken(String s) {
        return null;
    }

    /**
     * @param token
     */
    @Override
    public void saveToken(Token token) {

    }

    /**
     * @param s
     */
    @Override
    public void deleteToken(String s) {

    }

    /**
     * @return
     */
    @Override
    public TokenQuery createTokenQuery() {
        return null;
    }

    /**
     * @return
     */
    @Override
    public NativeTokenQuery createNativeTokenQuery() {
        return null;
    }

    /**
     * @param s
     * @param s1
     * @param s2
     */
    @Override
    public void setUserInfo(String s, String s1, String s2) {

    }

    /**
     * @param s
     * @param s1
     * @return
     */
    @Override
    public String getUserInfo(String s, String s1) {
        return "";
    }

    /**
     * @param s
     * @return
     */
    @Override
    public List<String> getUserInfoKeys(String s) {
        return Collections.emptyList();
    }

    /**
     * @param s
     * @param s1
     */
    @Override
    public void deleteUserInfo(String s, String s1) {

    }

    /**
     * @param s
     * @return
     */
    @Override
    public Privilege createPrivilege(String s) {
        return null;
    }

    /**
     * @param s
     * @param s1
     */
    @Override
    public void addUserPrivilegeMapping(String s, String s1) {

    }

    /**
     * @param s
     * @param s1
     */
    @Override
    public void deleteUserPrivilegeMapping(String s, String s1) {

    }

    /**
     * @param s
     * @param s1
     */
    @Override
    public void addGroupPrivilegeMapping(String s, String s1) {

    }

    /**
     * @param s
     * @param s1
     */
    @Override
    public void deleteGroupPrivilegeMapping(String s, String s1) {

    }

    /**
     * @param s
     * @return
     */
    @Override
    public List<PrivilegeMapping> getPrivilegeMappingsByPrivilegeId(String s) {
        return Collections.emptyList();
    }

    /**
     * @param s
     */
    @Override
    public void deletePrivilege(String s) {

    }

    /**
     * @param s
     * @return
     */
    @Override
    public List<User> getUsersWithPrivilege(String s) {
        return Collections.emptyList();
    }

    /**
     * @param s
     * @return
     */
    @Override
    public List<Group> getGroupsWithPrivilege(String s) {
        return Collections.emptyList();
    }

    /**
     * @return
     */
    @Override
    public PrivilegeQuery createPrivilegeQuery() {
        return null;
    }
}
