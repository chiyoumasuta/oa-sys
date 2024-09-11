package cn.gson.oasys.service.impl;

import cn.gson.oasys.dao.DepartmentDao;
import cn.gson.oasys.dao.UserDao;
import cn.gson.oasys.entity.Department;
import cn.gson.oasys.entity.User;
import cn.gson.oasys.service.DepartmentService;
import cn.gson.oasys.service.UserService;
import cn.gson.oasys.support.Page;
import cn.gson.oasys.vo.DepartmentVo;
import org.hibernate.service.spi.ServiceException;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.sql.rowset.serial.SerialException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
            departmentVos.add(departmentVo);
        });
        return departmentVos;
    }

    public boolean setDept(Long deptId,String users) {
        Department department = departmentDao.selectByPrimaryKey(deptId);
        if (department == null) {
            throw new ServiceException("部门错误");
        }
        if (users == null) {
            throw new ServiceException("请选择用户");
        }
        for (User user : userService.findDetailByIds(Arrays.asList(users.split(",")).stream().map(Long::valueOf).collect(Collectors.toList()))) {
            user.setDeptId(deptId);
            if (userDao.updateByPrimaryKeySelective(user) <= 0) {
                throw new ServiceException("用户部门设置失败");
            }
        }
        return true;
    }
}
