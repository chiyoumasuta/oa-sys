package cn.gson.oasys.service.impl;

import cn.gson.oasys.dao.DepartmentDao;
import cn.gson.oasys.dao.UserDao;
import cn.gson.oasys.entity.Department;
import cn.gson.oasys.entity.User;
import cn.gson.oasys.service.DepartmentService;
import cn.gson.oasys.service.UserService;
import cn.gson.oasys.vo.DepartmentVo;
import org.hibernate.service.spi.ServiceException;
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
    @Override
    public boolean saveDepartment(Department department) {
        return departmentDao.insert(department)>0;
    }

    @Override
    public boolean deleteDepartment(Long id) {
        Example example = new Example(User.class);
        example.createCriteria().andLike("deptId", "%"+id+"%");
        List<User> users = userDao.selectByExample(example);
        if (users.size()>0) {
            throw new ServiceException("当前部门存在职员，不允许删除");
        }
        return departmentDao.deleteByPrimaryKey(id)>0;
    }

    @Override
    public boolean updateDepartment(Department department) {
        return departmentDao.updateByPrimaryKeySelective(department)>0;
    }

    @Override
    public Department findDepartmentById(Long id) {
        return departmentDao.selectByPrimaryKey(id);
    }

    @Override
    public List<DepartmentVo> findAllDepartments() {
        List<DepartmentVo> departmentVos = new ArrayList<DepartmentVo>();
        departmentDao.selectAll().forEach(department -> {
            DepartmentVo departmentVo = new DepartmentVo();
            BeanUtils.copyProperties(department, departmentVo);
            List<User> allByDeptId = userService.findAllByDeptId(department.getId());
            departmentVo.setUsers(allByDeptId);
            departmentVo.setManager(userService.findById(department.getManagerId()));
            departmentVos.add(departmentVo);
        });
        return departmentVos;
    }

    @Override
    public boolean setDept(Long deptId,String users) {
        Department department = departmentDao.selectByPrimaryKey(deptId);
        if (department == null) {
            throw new ServiceException("部门错误");
        }
        if (users == null) {
            throw new ServiceException("请选择用户");
        }
        for (User user : userService.findDetailByIds(Arrays.asList(users.split(",")).stream().map(Long::valueOf).collect(Collectors.toList()))) {
            Set<String> userDept = new HashSet<>();
            if (user.getDeptId()!=null) {
                userDept.addAll(Arrays.asList(user.getDeptId().split(",")));
            }
            userDept.add(String.valueOf(department.getId()));
            user.setDeptId(String.join(",", userDept));
            if (userDao.updateByPrimaryKeySelective(user) <= 0) {
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
        String dept = Arrays.stream(user.getDeptId().split(",")).filter(it->!it.equals(String.valueOf(deptId))).collect(Collectors.joining(","));
        user.setDeptId(dept);
        return userDao.updateByPrimaryKeySelective(user) > 0;
    }

}