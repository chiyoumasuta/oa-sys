package cn.gson.oasys.service;

import cn.gson.oasys.entity.Department;
import cn.gson.oasys.vo.DepartmentVo;

import java.util.List;

public interface DepartmentService {
    /**
     * 添加部门
     * @return
     */
    boolean saveDepartment(Department department);

    /**
     * 删除部门
     */
    boolean deleteDepartment(Long id);

    /**
     * 更新部门信息
     */
    boolean updateDepartment(Department department);

    /**
     * 根据Id查询部门
     */
    Department findDepartmentById(Long id);

    /**
     * 获取部门列表
     */
    List<DepartmentVo> findAllDepartments();

    /**
     * 设置用户部门
     */
    boolean setDept(Long deptId,String users);

    /**
     * 删除部门用户
     */
    boolean deleteUserFormDept(Long deptId,Long userId);
}
