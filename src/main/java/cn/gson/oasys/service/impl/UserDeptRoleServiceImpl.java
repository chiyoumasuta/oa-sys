package cn.gson.oasys.service.impl;

import cn.gson.oasys.dao.*;
import cn.gson.oasys.entity.*;
import cn.gson.oasys.service.UserDeptRoleService;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserDeptRoleServiceImpl implements UserDeptRoleService {
    @Resource
    private UserDeptRoleDao userDeptRoleDao;
    @Resource
    private DepartmentDao departmentDao;
    @Resource
    private UserDao userDao;
    @Resource
    private RoleDao roleDao;
    @Resource
    private PermissionsDao permissionsDao;

    @Override
    public List<User> findByDepartmentId(Long departmentId) {
        List<User> result = new ArrayList<>();
        Example example = new Example(UserDeptRole.class);
        example.createCriteria().andEqualTo("departmentId", departmentId);
        for (UserDeptRole userDeptRole : userDeptRoleDao.selectByExample(example)) {
            User user = userDao.selectByPrimaryKey(userDeptRole.getUserId());
            user=findUserRole(user);
            result.add(user);
        }
        return result;
    }

    @Override
    public List<Department> findByUserId(Long userId) {
        List<Department> result = new ArrayList<>();
        Example example = new Example(UserDeptRole.class);
        example.createCriteria().andEqualTo("userId", userId);
        for (UserDeptRole userDeptRole : userDeptRoleDao.selectByExample(example)) {
            Department department = departmentDao.selectByPrimaryKey(userDeptRole.getDepartmentId());
            department.setRole(userDeptRole.getRole());
            result.add(department);
        }
        return result;
    }

    @Override
    public List<UserDeptRole> findRoleByUserId(Long userId) {
        Example example = new Example(UserDeptRole.class);
        example.createCriteria().andEqualTo("userId", userId);
        return userDeptRoleDao.selectByExample(example);
    }

    @Override
    public boolean updateUserRole(Long deptId, Long userId, String role) {
        Example example = new Example(UserDeptRole.class);
        example.createCriteria().andEqualTo("departmentId", deptId).andEqualTo("userId", userId);
        UserDeptRole userDeptRole = userDeptRoleDao.selectByExample(example).get(0);
        userDeptRole.setRole(role);
        return userDeptRoleDao.updateByPrimaryKeySelective(userDeptRole)>0;
    }

    @Override
    public User findUserRole(User user){
        List<UserDeptRole> userDeptRoles = findRoleByUserId(user.getId());
        List<Long> perId = new ArrayList<>();
        List<String> roles = new ArrayList<>();
        userDeptRoles.forEach(it -> {
            if (it.getRoleId()!=null){
                Role role = roleDao.selectByPrimaryKey(it.getRoleId());
                if (role.getPermissions()!=null){
                    List<String> list = Arrays.asList(role.getPermissions().split(","));
                    List<Long> collect1 = list.stream().map(Long::valueOf).collect(Collectors.toList());
                    perId.addAll(collect1);
                }
            }
            roles.add(it.getRole());
        });
        if (!perId.isEmpty()){
            Example examplePer = new Example(Permissions.class);
            examplePer.createCriteria().andIn( "id", perId).andEqualTo("isDelete",false);
            user.setPermissionsList(buildTree(permissionsDao.selectByExample(examplePer)));
        }
        user.setToken(null);
        user.setPassword(null);
        user.setRoleList(roles);
        return user;
    }

    /**
     * 获取所有权限
     */
    @Override
    public List<Permissions> findAllPermissions() {
        Example example = new Example(Permissions.class);
        example.createCriteria().andNotEqualTo("isDelete", false);
        return buildTree(permissionsDao.selectByExample(example));
    }

    /**
     * 获取角色列表
     */
    @Override
    public List<Role> findAllRoles() {
        return roleDao.selectAll();
    }

    /**
     * 增加/修改角色
     */
    @Override
    public boolean saveOrUpdateRole(Role role) {
        if (role.getId() == null) {
            return roleDao.insert(role)>0;
        }else return roleDao.updateByPrimaryKeySelective(role)>0;
    }

    public List<Permissions> buildTree(List<Permissions> permissionsList) {
        if (permissionsList == null || permissionsList.isEmpty()) {
            return new ArrayList<>();
        }
        Map<Long, Permissions> permissionsMap = permissionsList.stream().collect(Collectors.toMap(Permissions::getId, p -> p));
        List<Permissions> roots = new ArrayList<>();
        for (Permissions permission : permissionsList) {
            if (permission.getParentId() == null) {
                roots.add(permission);
            } else {
                Permissions parent = permissionsMap.get(permission.getParentId());
                if (parent != null) {
                    if (parent.getChildren() == null) {
                        parent.setChildren(new ArrayList<>());
                    }
                    parent.getChildren().add(permission);
                }
            }
        }
        return roots;
    }
}
