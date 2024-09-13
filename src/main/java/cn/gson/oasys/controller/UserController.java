package cn.gson.oasys.controller;

import cn.gson.oasys.entity.User;
import cn.gson.oasys.service.UserService;
import cn.gson.oasys.support.Page;
import cn.gson.oasys.support.UtilResultSet;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user")
@Api(tags = "用户管理")
public class UserController {

    @Autowired
    private UserService userService;

    @RequestMapping(value = "/page")
    @ApiOperation(value = "分页获取用户列表")
    public UtilResultSet getPage(String name, String phone, Integer type, String roleName, int pageNo, int pageSize) {
        return UtilResultSet.success(userService.page(name, phone, type, roleName, pageNo, pageSize));
    }

    @RequestMapping(value = "/findDetailByIds")
    @ApiOperation(value = "根据id获取用户详情")
    public UtilResultSet findDetailByIds(@RequestBody List<Long> userIds) {
        return UtilResultSet.success(userService.findDetailByIds(userIds));
    }

    @RequestMapping(value = "/saveOrUpdate")
    @ApiOperation(value = "添加或修改用户信息")
    public UtilResultSet saveOrUpdate(
            Long id,
            String userName,
            String phone,
            String password,
            String roleIds,
            String fiberArea,
            Integer type,
            Long deptId,
            boolean inRole) {
        userService.saveOrUpdate(id, userName, phone, password, roleIds, fiberArea, type, deptId, inRole);
        return UtilResultSet.success("User saved/updated successfully");
    }

    @RequestMapping(value = "/del")
    @ApiOperation(value = "删除用户")
    public UtilResultSet del(Long id) {
        userService.del(id);
        return UtilResultSet.success("User deleted successfully");
    }

    @RequestMapping(value = "/verifyByPhone")
    @ApiOperation(value = "根据手机号修改密码")
    public UtilResultSet verifyByPhone(String phone, String password) {
        User user = userService.verifyByPhone(phone, password);
        if (user != null) {
            return UtilResultSet.success(user);
        } else {
            return UtilResultSet.bad_request(null);
        }
    }

    @RequestMapping(value = "/resetPwd")
    @ApiOperation(value = "")
    public UtilResultSet resetPwd(Long id) {
        boolean result = userService.resetPwd(id);
        if (result) {
            return UtilResultSet.success("Password reset successfully");
        } else {
            return UtilResultSet.bad_request("Failed to reset password");
        }
    }

    @RequestMapping(value = "/changePwd")
    @ApiOperation(value = "")
    public UtilResultSet changePwd(
            String phone,
            String oldpwd,
            String newpwd) {
        boolean result = userService.changePwd(phone, oldpwd, newpwd);
        if (result) {
            return UtilResultSet.success("Password changed successfully");
        } else {
            return UtilResultSet.bad_request("Failed to change password");
        }
    }

    @RequestMapping(value = "/changePwdSimple")
    @ApiOperation(value = "")
    public UtilResultSet changePwdSimple(
            String phone,
            String newPwd) {
        boolean result = userService.changePwd(phone, newPwd);
        if (result) {
            return UtilResultSet.success("Password changed successfully");
        } else {
            return UtilResultSet.bad_request("Failed to change password");
        }
    }

    @RequestMapping(value = "/findByLoginName")
    @ApiOperation(value = "")
    public UtilResultSet findByLoginName(String loginName) {
        User user = userService.findByLoginName(loginName);
        if (user != null) {
            return UtilResultSet.success(user);
        } else {
            return UtilResultSet.bad_request(null);
        }
    }

    @RequestMapping(value = "/findByToken")
    @ApiOperation(value = "")
    public UtilResultSet findByToken(String token) {
        User user = userService.findByToken(token);
        if (user != null) {
            return UtilResultSet.success(user);
        } else {
            return UtilResultSet.bad_request(null);
        }
    }

    @RequestMapping(value = "/findByPhone")
    @ApiOperation(value = "")
    public UtilResultSet findByPhone(String phone) {
        User user = userService.findByPhone(phone);
        if (user != null) {
            return UtilResultSet.success(user);
        } else {
            return UtilResultSet.bad_request(null);
        }
    }
}
