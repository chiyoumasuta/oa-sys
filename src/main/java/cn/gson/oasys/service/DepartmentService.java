package cn.gson.oasys.service;

import cn.gson.oasys.entity.Department;
import cn.gson.oasys.vo.DepartmentVo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface DepartmentService {
    boolean saveDepartment(Department department);
    boolean deleteDepartment(Long id);
    boolean updateDepartment(Department department);
    Department findDepartmentById(Long id);
    List<DepartmentVo> findAllDepartments();
    boolean setDept(Long deptId,String users);
}
