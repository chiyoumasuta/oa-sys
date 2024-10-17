package cn.gson.oasys.service.impl;

import cn.gson.oasys.dao.DepartmentDao;
import cn.gson.oasys.dao.UserDao;
import cn.gson.oasys.dao.UserDeptRoleDao;
import cn.gson.oasys.entity.Department;
import cn.gson.oasys.entity.User;
import cn.gson.oasys.entity.UserDeptRole;
import cn.gson.oasys.entity.config.SysConfig;
import cn.gson.oasys.service.UserDeptRoleService;
import cn.gson.oasys.support.exception.ServiceException;
import cn.gson.oasys.service.DepartmentService;
import cn.gson.oasys.service.SysConfigService;
import cn.gson.oasys.service.UserService;
import cn.gson.oasys.vo.DepartmentVo;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DepartmentServiceImpl implements DepartmentService {

    @Resource
    private DepartmentDao departmentDao;
    @Resource
    private UserService userService;
    @Resource
    private UserDao userDao;
    @Resource
    private SysConfigService sysConfigService;
    @Resource
    private UserDeptRoleDao userDeptRoleDao;
    @Resource
    private UserDeptRoleService userDeptRoleService;

    @Override
    public boolean saveDepartment(Department department) {
        if (departmentDao.insert(department) > 0) {
            SysConfig sysConfig = new SysConfig();
            sysConfig.setName(department.getName());
            sysConfigService.saveOrUpdate(sysConfig);
            return true;
        }
        return false;
    }

    @Override
    public boolean deleteDepartment(Long id) {
        List<User> users = userDeptRoleService.findByDepartmentId(id);
        if (!users.isEmpty()) {
            throw new ServiceException("当前部门存在职员，不允许删除");
        }
        return departmentDao.deleteByPrimaryKey(id) > 0;
    }

    @Override
    public boolean updateDepartment(Department department) {
        return departmentDao.updateByPrimaryKeySelective(department) > 0;
    }

    @Override
    public List<Department> findDepartmentById(String ids) {
        Example example = new Example(Department.class);
        example.createCriteria().andIn("id", Arrays.asList(ids.split(",")));
        return departmentDao.selectByExample(example);
    }

    @Override
    public List<DepartmentVo> findAllDepartments() {
        List<DepartmentVo> departmentVos = new ArrayList<DepartmentVo>();
        departmentDao.selectAll().forEach(department -> {
            DepartmentVo departmentVo = new DepartmentVo();
            BeanUtils.copyProperties(department, departmentVo);
            List<User> allByDeptId = userDeptRoleService.findByDepartmentId(department.getId());
            departmentVo.setUsers(allByDeptId);
            departmentVo.setManager(null);
            departmentVos.add(departmentVo);
        });
        return departmentVos;
    }

    @Override
    public boolean setDept(Long deptId, String users, String role) {
        Department department = departmentDao.selectByPrimaryKey(deptId);
        if (department == null) {
            throw new ServiceException("部门错误");
        }
        if (users == null) {
            throw new ServiceException("请选择用户");
        }
        for (User user : userService.findDetailByIds(Arrays.stream(users.split(",")).map(Long::valueOf).collect(Collectors.toList()))) {
            UserDeptRole userDeptRole = new UserDeptRole();
            userDeptRole.setDepartmentId(deptId);
            userDeptRole.setUserId(user.getId());
            userDeptRole.setRole(role);
            if (userDeptRoleDao.insert(userDeptRole) <= 0) {
                throw new ServiceException("用户部门设置失败");
            }
        }
        return true;
    }

    @Override
    public boolean deleteUserFormDept(Long deptId, Long userId) {
        User user = userDao.selectByPrimaryKey(userId);
        Department department = departmentDao.selectByPrimaryKey(deptId);
        if (user == null) {
            throw new ServiceException("为找到用户");
        }
        if (department == null) {
            throw new ServiceException("未找到部门");
        }
        Example example = new Example(UserDeptRole.class);
        example.createCriteria().andEqualTo("departmentId", department.getId()).andEqualTo("userId", user.getId());
        return userDeptRoleDao.deleteByExample(example) > 0;
    }
}
