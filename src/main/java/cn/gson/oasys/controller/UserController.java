package cn.gson.oasys.controller;

import cn.gson.oasys.entity.User;
import cn.gson.oasys.service.UserService;
import cn.gson.oasys.support.UtilResultSet;
import cn.gson.oasys.support.kaptcha.CaptchaUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

    @RestController
    public class UserController {
        @Resource
        private CaptchaUtil captchaUtil;
        @Resource
        private UserService userService;

        @Value("${keys.path}")
        private String outPath;

        private Pattern pattern = Pattern.compile("^(?![A-Z]*$)(?![a-z]*$)(?![0-9]*$)(?![^a-zA-Z0-9]*$)\\S{12,20}$");

        @RequestMapping("/web/check")
        public Object check(HttpServletRequest req) {
            Object user = req.getSession().getAttribute("user");
            if (user == null) {
                return UtilResultSet.bad_request("");
            }
            return UtilResultSet.success(user);
        }

        // @anno(value = "平台登录",operationUnit = OperationUnit.UNKNOWN)
        @RequestMapping("/web/login")
        public Object login(String phone, String password, String code, String uuid, HttpServletRequest req) {
            try {
                if (!captchaUtil.validateCaptcha(code, uuid)) {
                    return UtilResultSet.bad_request("验证码校验出错");
                }
                int failureTimes;
                User currentUser = userService.verifyAndGetUser(realPhone, realPassword);
                if (currentUser != null) {
                    if (!currentUser.isAdmin && currentUser.getFiberArea() == null) {
                        return UtilResultSet.bad_request("未配置区县，无法登录");
                    }
                    currentUser = userService.getPermsByUser(currentUser, -1);
                    currentUser.setVersion(upgradeRecordService.findNew("PC").getVersion());
                    UserTokenHolder.setUser(currentUser, sysConfigService.findByName("multipleLogin"));
                    userLoginLogService.add(currentUser.getPhone(), currentUser.getFiberArea(), "平台登录", "其他", req);
                    userLoginLogService.clearFailureRecord(phone);
                    operationService.add(Operation.Menu.OTHER, Operation.Type.LOGIN, "平台登录", currentUser.getUserName() + "-" + currentUser.getPhone(), false);
                    userLoginLogService.clearFailureRecord(realPhone);
                    return UtilResultSet.success(currentUser.getId());
                }
                userLoginLogService.add(realPhone, "未知", "平台登录失败", "其他", req);
                return UtilResultSet.bad_request("账号或密码不正确，剩余尝试次数：" + (5 - failureTimes - 1));
            } catch (Exception e) {
                e.printStackTrace();
                return UtilResultSet.bad_request("加密码过期，请刷新后在登陆");
            }
        }

        // @anno(value = "平台登出",operationUnit = OperationUnit.UNKNOWN)F
        @RequestMapping("/web/logout")
        public Object logout(HttpServletRequest req, HttpSession session) {
            User currentUser = UserTokenHolder.getUser();
            userLoginLogService.add(currentUser.getUserName(), currentUser.getFiberArea(), "平台登出", "其他", req);
            operationService.add(Operation.Menu.OTHER, Operation.Type.LOGOUT, "平台登出", currentUser.getUserName() + "-" + currentUser.getPhone(), false);
            UserTokenHolder.invalidate(currentUser.getPhone());
            return UtilResultSet.SUCCESS;
        }

        @RequestMapping("/web/changepwd")
        public Object changepwd(String oldpwd, String newpwd, HttpSession session) {
            try {
                User user = UserTokenHolder.getCurrentUser();
                if (newpwd.length() < 12) {
                    return UtilResultSet.bad_request("密码长度最少12位");
                }
                if (newpwd.equals(user.getUserName())) {
                    return UtilResultSet.bad_request("密码不能和用户名一样");
                }
                if (!pattern.matcher(newpwd).matches()) {
                    return UtilResultSet.bad_request("密码长度12-20位必须包含英文大写、小写、数字、特殊字符");
                }
                if (userService.changePwd(user, oldpwd, newpwd)) {
                    return UtilResultSet.SUCCESS;
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
                    return UtilResultSet.SUCCESS;
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
                return UtilResultSetbad_request("登录超时");
            }
            return UtilResultSet.success(user);
        }

        @RequestMapping("/web/loadPK")
        public Object loadPK() {
            return UtilResultSet.success(rsaSupport.loadPublicKey());
        }

        @RequestMapping("/web/captchaImage")
        public Object captchaImage(HttpServletRequest req) {
            return UtilResultSet.success(captchaUtil.getCode(false));
        }

        @PostConstruct
        public void rsaKeysGen() {
            File dir = new File(outPath);
            if (!dir.exists()) {
                System.out.println("--------创建密钥储存--------");
                dir.mkdir();
            }
            KeyPair keyPair = rsaSupport.genKeyPair();
            rsaSupport.saveKey(keyPair);
        }
}
