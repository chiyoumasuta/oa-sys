package cn.gson.oasys.controller;

import cn.gson.oasys.entity.Department;
import cn.gson.oasys.service.DepartmentService;
import cn.gson.oasys.support.UtilResultSet;
import cn.gson.oasys.vo.DepartmentVo;
import org.hibernate.service.spi.ServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/department")
public class DepartmentController {

    @Autowired
    private DepartmentService departmentService;

    // 新增部门
    @PostMapping("/save")
    public UtilResultSet saveDepartment(@RequestBody Department department) {
        if (departmentService.saveDepartment(department)) {
            return UtilResultSet.success("部门保存成功");
        } else {
            return UtilResultSet.bad_request("部门保存失败");
        }
    }

    // 删除部门
    @DeleteMapping("/delete/{id}")
    public UtilResultSet deleteDepartment(@PathVariable Long id) {
        if (departmentService.deleteDepartment(id)) {
            return UtilResultSet.success("部门删除成功");
        } else {
            return UtilResultSet.bad_request("部门删除失败");
        }
    }

    // 更新部门信息
    @PutMapping("/update")
    public UtilResultSet updateDepartment(@RequestBody Department department) {
        if (departmentService.updateDepartment(department)) {
            return UtilResultSet.success("部门更新成功");
        } else {
            return UtilResultSet.bad_request("部门更新失败");
        }
    }

    // 根据ID查找部门
    @GetMapping("/find/{id}")
    public UtilResultSet findDepartmentById(@PathVariable Long id) {
        Department department = departmentService.findDepartmentById(id);
        if (department != null) {
            return UtilResultSet.success(department);
        } else {
            return UtilResultSet.bad_request("部门未找到");
        }
    }

    // 查询所有部门及其用户信息
    @GetMapping("/list")
    public UtilResultSet findAllDepartments() {
        List<DepartmentVo> departmentVos = departmentService.findAllDepartments();
        if (!departmentVos.isEmpty()) {
            return UtilResultSet.success(departmentVos);
        } else {
            return UtilResultSet.bad_request("没有找到任何部门");
        }
    }

    // 设置部门下的用户
    @PostMapping("/setDept")
    public UtilResultSet setDept(@RequestParam("deptId") Long deptId, @RequestParam("users") String users) {
        try {
            if (departmentService.setDept(deptId, users)) {
                return UtilResultSet.success("用户部门设置成功");
            } else {
                return UtilResultSet.bad_request("用户部门设置失败");
            }
        } catch (ServiceException e) {
            return UtilResultSet.bad_request("操作失败: " + e.getMessage());
        }
    }
}
