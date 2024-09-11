package cn.gson.oasys.permission.aspectj;

import cn.gson.oasys.entity.User;
import cn.gson.oasys.permission.anno.RequestPermission;
import cn.gson.oasys.permission.exception.AuthorizationException;
import cn.gson.oasys.permission.exception.AuthorizationFromException;
import cn.gson.oasys.permission.exception.UnknownAccountException;
import cn.gson.oasys.support.UserTokenHolder;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 权限过滤处理
 *
 * @author 不愿透露
 * @date 2022/6/20 17:05
 */
@Aspect
@Component
public class RequestPermissionAspect {
    private static final Pattern pattern = Pattern.compile(".*update.*|.*del.*|.*add.*|.*import.*|.*export.*|.*save.*|.*edit.*|.*disable.*|.*reset.*|.*enable.*|.*roll.*");
    @Value("${spring.profiles.active}")
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

    /**
     * 请求前鉴权操作
     *
     * @param point             切点
     * @param requestPermission 权限标识
     * @author 不愿透露
     * @date 2022/7/4 16:07
     */
    @Before("@annotation(requestPermission)")
    public void doBefore(JoinPoint point, RequestPermission requestPermission) {
        handlePermission(point, requestPermission);
    }

    protected void handlePermission(final JoinPoint joinPoint, RequestPermission requestPermission) {
        if (!"dev".equals(active)) {
            HttpServletRequest request = UserTokenHolder.getRequest();
            User user = UserTokenHolder.getUser();
            if (user == null) {
                throw new UnknownAccountException();
            }
            if (!user.isAdmin()) {
               /* if (!user.isResetPwd()) {
                    //未修改初始密码
                    authorizationExceptionResponse(request);
                }*/
                //获取用户权限列表
                Set<String> permission = user.getPermissions();
                if (null == permission) {
                    authorizationExceptionResponse(request);
                }
                String[] expression = requestPermission.value();
                String param = containsExpression(permission, expression);
                if (StringUtils.isEmpty(param)) {
                    authorizationExceptionResponse(request);
                }
                // 有权限 查询是否需要验证参数
                if (requestPermission.param()) {
                    param = getParameter(request);
                    if (containsExpression(permission, new String[]{param}) == null) {
                        authorizationExceptionResponse(request);
                    }
                }
            }
        }
    }

    private void authorizationExceptionResponse(HttpServletRequest request) {
        String type = request.getHeader("Content-Type");
        String token = request.getHeader("token");//appp用户
        // 适用于列表接口返回数据
        if ((null != type && type.contains("multipart/form-data")) || StringUtils.isNotBlank(token)) {
            throw new AuthorizationException();
        }
        throw new AuthorizationFromException();
    }

    /**
     * 处理完请求后执行
     * 2
     *
     * @param joinPoint 切点
     */
    @AfterReturning(pointcut = "@annotation(requestPermission)", returning = "jsonResult")
    public void doAfterReturning(JoinPoint joinPoint, RequestPermission requestPermission, Object jsonResult) {
        String permission = getLogPermission(requestPermission.value());
        if (null != permission) {
            if (requestPermission.param()) {
                permission = getParameter(UserTokenHolder.getRequest());
            }
        }
    }


    /**
     * 判断权限字符中是否存在某关键字 并且返回
     *
     * @param values
     * @return boolean
     * @author 不愿透露
     * @date 2022/7/4 14:44
     */
    protected String getLogPermission(String[] values) {
        if (null != values) {
            for (String value : values) {
                if (pattern.matcher(value.toLowerCase()).matches()) {
                    return value;
                }
            }
        }
        return null;
    }
}
