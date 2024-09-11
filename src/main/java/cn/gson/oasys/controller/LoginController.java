package cn.gson.oasys.controller;

import cn.gson.oasys.entity.User;
import cn.gson.oasys.permission.RSASupport;
import cn.gson.oasys.service.UserService;
import cn.gson.oasys.support.UserTokenHolder;
import cn.gson.oasys.support.UtilResultSet;
import cn.gson.oasys.support.kaptcha.CaptchaUtil;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.security.KeyPair;
import java.util.regex.Pattern;

@Controller
public class LoginController {

    @Autowired
    private UserService userService;

    @Autowired
    private RSASupport rsaSupport;

    @Autowired
    private CaptchaUtil captchaUtil;

    @Value("${keys.path}")
    private String outPath;

    private Pattern pattern = Pattern.compile("^(?![A-Z]*$)(?![a-z]*$)(?![0-9]*$)(?![^a-zA-Z0-9]*$)\\S{12,20}$");

    @RequestMapping(value = "/web/check" ,method = RequestMethod.POST)
    @ApiOperation(value="测试用户", notes="查询数据库user")
    public Object check(HttpServletRequest req) {
        Object user = req.getSession().getAttribute("user");
        if (user == null) {
            return UtilResultSet.bad_request("");
        }
        return UtilResultSet.success(user);
    }

    @RequestMapping("/web/login")
    public Object login(String phone, String password, String code, String uuid, HttpServletRequest req) {
        try {
            if (!captchaUtil.validateCaptcha(code, uuid)) {
                return UtilResultSet.bad_request("验证码校验出错");
            }
            String realPhone = rsaSupport.decrypt(phone);
            String realPassword = rsaSupport.decrypt(password);
//            int failureTimes;
//            try {
//                failureTimes = userLoginLogService.getLoginCapability(realPhone);
//            } catch (AccountLockedException e) {
//                return UtilResultSet.bad_request(e.getLocalizedMessage());
//            }
            User currentUser = userService.verifyAndGetUser(realPhone, realPassword);
            if (currentUser != null) {
                currentUser = userService.getPermsByUser(currentUser, -1);
                UserTokenHolder.setUser(currentUser);
                return UtilResultSet.success(currentUser.getId());
            }
            return UtilResultSet.bad_request("账号或密码不正确");
        } catch (Exception e) {
            e.printStackTrace();
            return UtilResultSet.bad_request("加密码过期，请刷新后在登陆");
        }
    }

    @RequestMapping("/web/logout")
    public Object logout(HttpServletRequest req, HttpSession session) {
        User currentUser = UserTokenHolder.getUser();
        UserTokenHolder.invalidate(currentUser.getPhone());
        return UtilResultSet.success("登出成功");
    }

    @RequestMapping("/web/changepwd")
    public Object changepwd(String oldpwd, String newpwd, HttpSession session) {
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

    @RequestMapping("/web/resetInitPwd")
    public Object resetInitPwd(String newpwd, HttpSession session) {
        try {
            String phone = UserTokenHolder.getCurrentUser();
            if (newpwd.length() < 12) {
                return UtilResultSet.bad_request("密码长度最少12位");
            }
            if (!pattern.matcher(newpwd).matches()) {
                return UtilResultSet.bad_request("密码长度12-20位必须包含英文大写、小写、数字、特殊字符");
            }
            if (userService.changePwd(phone, newpwd)) {
                return UtilResultSet.success("密码设置成功");
            }
            return UtilResultSet.bad_request("密码不正确");
        } catch (Exception e) {
            return UtilResultSet.bad_request(e.getLocalizedMessage());
        }
    }

    @RequestMapping("/web/userInfo")
    public Object userInfo(HttpServletRequest req) {
        Object user = req.getSession().getAttribute("user");
        if (user == null) {
            return UtilResultSet.bad_request("登录超时");
        }
        return UtilResultSet.success(user);
    }

    @RequestMapping("/web/loadPK")
    public Object loadPK() throws Exception {
        return UtilResultSet.success(rsaSupport.loadPublicKey());
    }

    @RequestMapping("/web/captchaImage")
    public Object captchaImage(HttpServletRequest req) {
        return UtilResultSet.success(captchaUtil.getCode(false));
    }

    @PostConstruct
    public void rsaKeysGen() throws Exception {
        File dir = new File(outPath);
        if (!dir.exists()) {
            System.out.println("--------创建密钥储存--------");
            dir.mkdir();
        }
        KeyPair keyPair = rsaSupport.genKeyPair();
        rsaSupport.saveKey(keyPair);
    }
}