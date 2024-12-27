package cn.gson.oasys.service;

import cn.gson.oasys.entity.*;

import java.util.List;

public interface UserDeptRoleService {
    /**
     * 获取当前部门的所有职员
     * @param departmentId
     * @return
     */
    List<User> findByDepartmentId(Long departmentId);

    /**
     * 获取当前用户的所有部门
     * @param userId
     * @return
     */
    List<Department> findByUserId(Long userId);

    /**
     * 通过用户id获取当前用户的所有中间表信息
     * @param userId
     * @return
     */
    List<UserDeptRole> findRoleByUserId(Long userId);

    /**
     * 更新用户职位信息
     * @param deptId
     * @param userId
     * @param role
     * @return
     */
    boolean updateUserRole(Long deptId, Long userId, String role);

    /**
     * 获取用户所有权限
     */
    User findUserRole(User user);

    /**
     * 获取所有权限
     */
    List<Permissions> findAllPermissions();

    /**
     * 获取角色列表
     */
    List<Role> findAllRoles();

    /**
     * 增加/修改角色
     */
    boolean saveOrUpdateRole(Role role);
}
