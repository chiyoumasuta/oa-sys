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

    @RequestMapping(value = "/page",method = RequestMethod.POST)
    @ApiOperation(value = "分页获取用户列表")
    public UtilResultSet getPage(String name, String phone, String roleName, int pageNo, int pageSize) {
        return UtilResultSet.success(userService.page(name, phone, roleName, pageNo, pageSize));
    }

    @RequestMapping(value = "/findDetailByIds",method = RequestMethod.POST)
    @ApiOperation(value = "根据id获取用户详情")
    public UtilResultSet findDetailByIds(@RequestBody List<Long> userIds) {
        return UtilResultSet.success(userService.findDetailByIds(userIds));
    }

    @RequestMapping(value = "/saveOrUpdate",method = RequestMethod.POST)
    @ApiOperation(value = "添加或修改用户信息")
    public UtilResultSet saveOrUpdate(User user) {
        userService.saveOrUpdate(user);
        return UtilResultSet.success("User saved/updated successfully");
    }

    @RequestMapping(value = "/del",method = RequestMethod.POST)
    @ApiOperation(value = "删除用户")
    public UtilResultSet del(Long id) {
        userService.del(id);
        return UtilResultSet.success("User deleted successfully");
    }

    @RequestMapping(value = "/verifyByPhone",method = RequestMethod.POST)
    @ApiOperation(value = "根据手机号修改密码")
    public UtilResultSet verifyByPhone(String phone, String password) {
        User user = userService.verifyByPhone(phone, password);
        if (user != null) {
            return UtilResultSet.success(user);
        } else {
            return UtilResultSet.bad_request(null);
        }
    }

    @RequestMapping(value = "/resetPwd",method = RequestMethod.POST)
    @ApiOperation(value = "重设用户密码")
    public UtilResultSet resetPwd(Long id) {
        boolean result = userService.resetPwd(id);
        if (result) {
            return UtilResultSet.success("Password reset successfully");
        } else {
            return UtilResultSet.bad_request("Failed to reset password");
        }
    }

    @RequestMapping(value = "/changePwd",method = RequestMethod.POST)
    @ApiOperation(value = "修改密码")
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

    @RequestMapping(value = "/changePwdSimple",method = RequestMethod.POST)
    @ApiOperation(value = "通过手机号和密码查找用户信息")
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

    @RequestMapping(value = "/findByLoginName",method = RequestMethod.POST)
    @ApiOperation(value = "通过用户登录名获取用户登录信息")
    public UtilResultSet findByLoginName(String loginName) {
        User user = userService.findByLoginName(loginName);
        if (user != null) {
            return UtilResultSet.success(user);
        } else {
            return UtilResultSet.bad_request(null);
        }
    }

    @RequestMapping(value = "/findByToken",method = RequestMethod.POST)
    @ApiOperation(value = "通过token获取用户信息")
    public UtilResultSet findByToken(String token) {
        User user = userService.findByToken(token);
        if (user != null) {
            return UtilResultSet.success(user);
        } else {
            return UtilResultSet.bad_request(null);
        }
    }

    @RequestMapping(value = "/findByPhone",method = RequestMethod.POST)
    @ApiOperation(value = "通过手机号获取用户数据")
    public UtilResultSet findByPhone(String phone) {
        User user = userService.findByPhone(phone);
        if (user != null) {
            return UtilResultSet.success(user);
        } else {
            return UtilResultSet.bad_request(null);
        }
    }
}
