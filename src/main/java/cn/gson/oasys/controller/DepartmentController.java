package cn.gson.oasys.controller;

import cn.gson.oasys.entity.Department;
import cn.gson.oasys.service.DepartmentService;
import cn.gson.oasys.support.UtilResultSet;
import cn.gson.oasys.vo.DepartmentVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.hibernate.service.spi.ServiceException;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/department")
@Api(tags = "部门管理")
public class DepartmentController {

    @Resource
    private DepartmentService departmentService;

    @RequestMapping(value = "/save")
    @ApiOperation("新增部门")
    public UtilResultSet saveDepartment(Department department) {
        if (departmentService.saveDepartment(department)) {
            return UtilResultSet.success("部门保存成功");
        } else {
            return UtilResultSet.bad_request("部门保存失败");
        }
    }

    // 删除部门
    @RequestMapping(value = "/delete",method = RequestMethod.POST)
    @ApiOperation("删除部门")
    public UtilResultSet deleteDepartment(Long id) {
        if (departmentService.deleteDepartment(id)) {
            return UtilResultSet.success("部门删除成功");
        } else {
            return UtilResultSet.bad_request("部门删除失败");
        }
    }

    @RequestMapping(value = "/update",method = RequestMethod.POST)
    @ApiOperation("更新部门信息")
    public UtilResultSet updateDepartment(Department department) {
        if (departmentService.updateDepartment(department)) {
            return UtilResultSet.success("部门更新成功");
        } else {
            return UtilResultSet.bad_request("部门更新失败");
        }
    }

    @RequestMapping(value = "/find/{id}",method = RequestMethod.POST)
    @ApiOperation("通过id查询部门信息")
    public UtilResultSet findDepartmentById(@PathVariable Long id) {
        Department department = departmentService.findDepartmentById(id);
        if (department != null) {
            return UtilResultSet.success(department);
        } else {
            return UtilResultSet.bad_request("部门未找到");
        }
    }

    @RequestMapping(value = "/list",method = RequestMethod.POST)
    @ApiOperation("查询所有部门及其用户信息")
    public UtilResultSet findAllDepartments() {
        List<DepartmentVo> departmentVos = departmentService.findAllDepartments();
        if (!departmentVos.isEmpty()) {
            return UtilResultSet.success(departmentVos);
        } else {
            return UtilResultSet.bad_request("没有找到任何部门");
        }
    }

    // 设置部门下的用户
    @RequestMapping(value = "/setDept",method = RequestMethod.POST)
    @ApiOperation("设置部门用户")
    public UtilResultSet setDept(Long deptId, String users) {
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

    @RequestMapping(value = "/deleteUserFormDept",method = RequestMethod.POST)
    @ApiOperation("将用户从部门删除")
    public UtilResultSet deleteUserFormDept(Long deptId, Long userId){
        if (departmentService.deleteUserFormDept(deptId, userId)) {
            return UtilResultSet.success("删除成功");
        }return UtilResultSet.bad_request("删除失败");
    }
}
