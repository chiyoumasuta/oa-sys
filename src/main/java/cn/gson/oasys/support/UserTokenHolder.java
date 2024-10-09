package cn.gson.oasys.support;

import cn.gson.oasys.entity.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class UserTokenHolder {

    private static String active;
    private static final Map<String, HttpSession> userMap = new ConcurrentHashMap<>();

    public static HttpSession getUserMap(String phone) {
        return userMap.get(phone);
    }

    public enum TerminalType{
        APP_LOGIN,
        WEB_LOGIN
    }
    /**
     * 根据登录名清除登录信息
     *
     * @param phone 登录名
     */
    public static void removeUserMap(String phone) {
        userMap.remove(phone);
    }

    /**
     * 获取全部在线用户
     *
     * @return java.util.Map<java.lang.String, javax.servlet.http.HttpSession>
     */
    public static Map<String, HttpSession> getOnLinUserMap() {
        return userMap;
    }

    /**
     * 根据手机号清除登录信息
     *
     * @param phone 手机号
     */
    public static void invalidate(String phone) {
        try {
            HttpSession session = getUserMap(phone);
            if (null != session) {
                removeUserMap(phone);
                session.invalidate();
            }
        } catch (Exception e) {
        }
    }

    /**
     * 根据登录名为key储存登录的session
     * @param phone       手机号(phone)
     * @param httpSession 登录session
     */
    public static void setUserMap(String phone, HttpSession httpSession) {
        userMap.put(phone, httpSession);
    }

    public static ServletRequestAttributes getRequestAttributes() {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        return (ServletRequestAttributes) attributes;
    }

    //获取request
    public static HttpServletRequest getRequest() {
        return getRequestAttributes().getRequest();
    }

    //获取session
    public static HttpSession getSession() {
        return getRequest().getSession();
    }

    /**
     * 更新登录的用户信息
     *
     * @param user 需更新的用户
     */
    public static void updateUser(User user) {
        //存在登录信息则更新
        HttpSession session = getUserMap(user.getPhone());
        if (null != session) {
            try {
                session.setAttribute("user", user);
            } catch (Exception e) {
                removeUserMap(user.getPhone());
            }
        }
    }

    /**
     * 登录设置用户登录信息
     */
    public static void setUser(User user) {
        HttpSession session = getSession();
        session.removeAttribute("user");
        session.setAttribute("user", user);
        //存储用户信息
        setUserMap(user.getPhone(), session);
    }

    public static User getUser() {
        User user = null;
        String token = getRequest().getHeader("token");
        if (token != null) {
            user = JwtUtil.verifyToken(token);
        }
        if (user == null) {
            if ("dev".equals(active)) {
                //非正式环境
                user = new User();
                user.setId(1L);
                user.setUserName("admin");
                user.setPhone("123456");
            }
        }
        return user;
    }

    /**
     * 获取当前登录的手机号
     *
     * @return java.lang.String
     */
    public static String getCurrentUser() {
        return getUser().getPhone();
    }

    /**
     * 判断是否为管理员
     * @return boolean
     */
    public static boolean isAdmin() {
        return getUser().isAdmin();
    }

    /**
     * 判断用户是否为部门负责人
     * @return 是否为部门负责人
     */
    public static Boolean isManager() {
        return getUser().isManager();
    }

    /**
     * 获取用户id
     * @return 当前用户id
     */
    public static Long getCurrentUserId() {
        return getUser().getId();
    }

    /**
     * 获取用户Phone
     * @return 当前用户Phone
     */
    public static String getCurrentUserPhone() {
        return getUser().getPhone();
    }

    //获取对象字段名
    private static String getField(Object t, String fieldName) {
        try {
            String firstLetter = fieldName.substring(0, 1).toUpperCase();
            String getter = "get" + firstLetter + fieldName.substring(1);
            Method method = t.getClass().getMethod(getter);
            Object value = method.invoke(t);
            return value.toString();
        } catch (Exception e) {
        }
        return "未知";
    }

    @Value("${spring.profiles.active}")
    public void setActive(String active) {
        UserTokenHolder.active = active;
    }

}
