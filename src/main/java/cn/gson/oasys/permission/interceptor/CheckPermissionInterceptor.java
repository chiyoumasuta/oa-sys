package cn.gson.oasys.permission.interceptor;


import cn.gson.oasys.entity.User;
import cn.gson.oasys.permission.anno.RequestPermission;
import cn.gson.oasys.support.UserTokenHolder;
import cn.gson.oasys.support.UtilResultSet;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.Set;

/**
 * 自定义权限拦截器
 * 在controller层使用 加入@RequestPermission(value={"list","update"})
 * 一个接口多地方使用需要参数验证@RequestPermission(value={"list","fbList",param=true})
 * 前端传入参数：permission 进行识别判断验证
 *
 * @author 不愿透露
 * @date 2022/6/22 17:21
 */
@Component
public class CheckPermissionInterceptor implements HandlerInterceptor {
    //@Value("${spring.profiles.active}")
    private String active;

    /**
     * 检查用户是否有某个权限
     *
     * @param permission 用户权限集合
     * @param expression 注解权限
     * @return java.lang.String 没有则返回null
     * @author 不愿透露
     * @date 2022/6/27 11:08
     */
    public static String containsExpression(Set<String> permission, String[] expression) {
        if (expression[0] != null) {
            for (String ex : expression) {
                if (permission.contains(ex)) {
                    return ex;
                }
            }
        }
        return null;
    }

    /**
     * 获取权限参数
     *
     * @param request
     * @return java.lang.String 没有权限返回null
     * @author 不愿透露
     * @date 2022/6/27 10:44
     */
    public static String getParameter(HttpServletRequest request) {
        String param = request.getParameter("permission");
        if (null == param) {
            //可能是json字符串获取第一个参数
            Enumeration<String> names = request.getParameterNames();
            param = request.getParameter(names.nextElement());
            if (null != param) {
                try {
                    JSONObject json = JSONObject.parseObject(param);
                    param = json.getString("permission");
                } catch (Exception e) {
                }
            }
        }
        return param;
    }

    //验证
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!"dev".equals(active)) {
            User user = UserTokenHolder.getUser();
            if (user == null) {
                customResponseCode(401, response);
                return false;
            }
            if (user.isAdmin()) {
                // 超管
                return true;
            }
            if (handler instanceof HandlerMethod) {
                RequestPermission requestPermission = ((HandlerMethod) handler).getMethodAnnotation(RequestPermission.class);
                if (requestPermission == null) {
                    // 方法不需要权限
                    return true;
                }
                //获取用户权限列表
                Set<String> permission = user.getPermissions();
                if (null == permission) {
                    customResponse(response, request, "暂无权限");
                    return false;
                }
                String[] expression = requestPermission.value();
                String param = containsExpression(permission, expression);
                if (null != param) {
                    // 有权限 查询是否需要验证参数
                    if (requestPermission.param()) {
                        param = getParameter(request);
                        if (containsExpression(permission, new String[]{param}) == null) {
                            customResponse(response, request, "暂无权限");
                            return false;
                        }
                    }
                    return true;
                }
            }
            customResponse(response, request, "暂无权限");
            return false;
        }
        return true;
    }

    /**
     * 返回前端响应参数
     *
     * @param response
     * @param request
     * @param msg
     * @author 不愿透露
     * @date 2022/6/24 17:05
     */
    private void customResponse(HttpServletResponse response, HttpServletRequest request, String msg) {
        try {
            String userJson;
            String type = request.getHeader("Content-Type");
            // 适用于列表接口返回数据
            if (null != type && type.contains("multipart/form-data")) {
                response.setContentType("application/json; charset=utf-8");
                response.setCharacterEncoding("UTF-8");
                userJson = JSON.toJSONString(UtilResultSet.no_permission());
            } else {
                //打开新页面返回数据manualDrawing/page
                response.setContentType("text/html; charset=UTF-8");
                response.setCharacterEncoding("UTF-8");
                userJson = "<script>alert('" + msg + "');window.close();</script>";
            }
            OutputStream out = response.getOutputStream();
            out.write(userJson.getBytes(StandardCharsets.UTF_8));
            out.flush();
        } catch (IOException e) {
            System.out.println("权限拦截器:" + e.toString());
        }
    }

    private void customResponseCode(int code, HttpServletResponse response) {
        response.setStatus(401);
    }
}
