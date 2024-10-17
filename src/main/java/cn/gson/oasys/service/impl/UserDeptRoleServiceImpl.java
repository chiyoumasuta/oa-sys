package cn.gson.oasys.service.impl;

import cn.gson.oasys.dao.DepartmentDao;
import cn.gson.oasys.dao.UserDao;
import cn.gson.oasys.dao.UserDeptRoleDao;
import cn.gson.oasys.entity.Department;
import cn.gson.oasys.entity.User;
import cn.gson.oasys.entity.UserDeptRole;
import cn.gson.oasys.service.UserDeptRoleService;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Service
public class UserDeptRoleServiceImpl implements UserDeptRoleService {
    @Resource
    private UserDeptRoleDao userDeptRoleDao;
    @Resource
    private DepartmentDao departmentDao;
    @Resource
    private UserDao userDao;

    @Override
    public List<User> findByDepartmentId(Long departmentId) {
        List<User> result = new ArrayList<>();
        Example example = new Example(UserDeptRole.class);
        example.createCriteria().andEqualTo("departmentId", departmentId);
        for (UserDeptRole userDeptRole : userDeptRoleDao.selectByExample(example)) {
            User user = userDao.selectByPrimaryKey(userDeptRole.getUserId());
            user.setRole(userDeptRole.getRole());
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
            result.add(department);
        }
        return result;
    }

    @Override
    public List<UserDeptRole> findItByUserId(Long userId) {
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
}
