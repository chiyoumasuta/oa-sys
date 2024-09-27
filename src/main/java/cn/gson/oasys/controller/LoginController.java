package cn.gson.oasys.controller;

import cn.gson.oasys.entity.Department;
import cn.gson.oasys.entity.User;
import cn.gson.oasys.exception.UnknownAccountException;
import cn.gson.oasys.service.DepartmentService;
import cn.gson.oasys.service.UserService;
import cn.gson.oasys.support.JwtUtil;
import cn.gson.oasys.support.UserTokenHolder;
import cn.gson.oasys.support.UtilResultSet;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Example;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@RestController
@Api(tags = "用户接口")
public class LoginController {

    @Autowired
    private UserService userService;
    @Resource
    private DepartmentService departmentService;

    @Value("${user.password}")
    private String resetPassword;

    private Pattern pattern = Pattern.compile("^(?![A-Z]*$)(?![a-z]*$)(?![0-9]*$)(?![^a-zA-Z0-9]*$)\\S{12,20}$");

    @RequestMapping(value = "/check" )
    @ApiOperation(value="检查用户是否登录")
    public UtilResultSet check(HttpServletRequest req) {
        Object user = req.getSession().getAttribute("user");
        if (user == null) {
            return UtilResultSet.bad_request("");
        }
        return UtilResultSet.success(user);
    }

    @RequestMapping(value = "/web/login")
    @ApiOperation(value = "登录接口")
    public UtilResultSet login(String phone, String password, HttpServletRequest req) {
        try {
            User currentUser = userService.verifyAndGetUser(phone, password);
            if (currentUser != null) {
                currentUser = userService.getPermsByUser(currentUser, -1);
                UserTokenHolder.setUser(currentUser);
                return UtilResultSet.success(JwtUtil.createToken(currentUser));
            }
            return UtilResultSet.bad_request("账号或密码不正确");
        } catch (Exception e) {
            e.printStackTrace();
            return UtilResultSet.bad_request("登录错误");
        }
    }

    @RequestMapping(value = "/logout")
    @ApiOperation(value = "推出登录")
    public UtilResultSet logout(HttpServletRequest req) {
        User currentUser = UserTokenHolder.getUser();
        UserTokenHolder.invalidate(currentUser.getPhone());
        return UtilResultSet.success("登出成功");
    }

    @RequestMapping(value = "/changepwd")
    @ApiOperation(value = "修改密码")
    public UtilResultSet changePwd(String oldpwd, String newpwd, HttpServletRequest req) {
        try {
            User user = UserTokenHolder.getUser();
            if (newpwd.length() < 12) {
                return UtilResultSet.bad_request("密码长度最少12位");
            }
            if (newpwd.equals(user.getUserName())) {
                return UtilResultSet.bad_request("密码不能和用户名一样");
            }
            if (!pattern.matcher(newpwd).matches()) {
                return UtilResultSet.bad_request("密码长度12-20位必须包含英文大写、小写、数字、特殊字符");
            }
            if (userService.changePwd(user.getPhone(), oldpwd, newpwd)) {
                return UtilResultSet.success("密码设置成功");
            }
            return UtilResultSet.bad_request("密码不正确");
        } catch (Exception e) {
            return UtilResultSet.bad_request(e.getLocalizedMessage());
        }
    }

    @RequestMapping(value = "/resetInitPwd")
    @ApiOperation(value = "重置密码")
    public UtilResultSet resetInitPwd(HttpServletRequest req) {
        try {
            String phone = UserTokenHolder.getCurrentUser();
            if (userService.changePwd(phone, resetPassword)) {
                return UtilResultSet.success("密码设置成功");
            }
            return UtilResultSet.bad_request("重设密码失败");
        } catch (Exception e) {
            return UtilResultSet.bad_request(e.getLocalizedMessage());
        }
    }

    @RequestMapping(value = "/userInfo")
    @ApiOperation(value = "获取登录用户信息")
    public Object userInfo(HttpServletRequest req) {
        User user = UserTokenHolder.getUser();
        if (user == null) {
            return new ResponseEntity<>("登录失效", HttpStatus.UNAUTHORIZED);
        }
        String deptName = "";
        AtomicBoolean isMannger = new AtomicBoolean(false);
        if (user.getDeptId()!=null){
            deptName = Arrays.stream(user.getDeptId().split(",")).filter(v->v!=null).map(d->{
                Department departmentById = departmentService.findDepartmentById(d).get(0);
                if (departmentById!=null&&departmentById.getManagerId().equals(user.getId())) isMannger.set(true);
                return departmentById.getName();
            }).collect(Collectors.joining(","));
        }
        user.setDeptName(deptName);
        user.setManager(isMannger.get());
        return UtilResultSet.success(user);
    }
}