package cn.gson.oasys.handler;

import cn.gson.oasys.support.exception.AuthorizationException;
import cn.gson.oasys.support.exception.AuthorizationFromException;
import cn.gson.oasys.support.exception.ServiceException;
import cn.gson.oasys.support.exception.UnknownAccountException;
import cn.gson.oasys.support.UtilResultSet;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

/**
 * 异常拦截器
 */
@ControllerAdvice
@ResponseBody
public class ExceptionHandler {

    @org.springframework.web.bind.annotation.ExceptionHandler(AuthorizationException.class)
    @ResponseStatus(HttpStatus.OK)
    public Object authorizationException(HttpServletRequest request) {
        return UtilResultSet.bad_request("暂无权限");
    }

    @org.springframework.web.bind.annotation.ExceptionHandler(AuthorizationFromException.class)
    @ResponseStatus(HttpStatus.OK)
    public void authorizationFromException(AuthorizationFromException errorException, HttpServletResponse httpResponse) {
        try {
            httpResponse.setContentType("text/html; charset=UTF-8");
            httpResponse.setCharacterEncoding("UTF-8");
            String msg = "<script>alert('" + errorException.getLocalizedMessage() + "');window.close();</script>";
            OutputStream out = httpResponse.getOutputStream();
            out.write(msg.getBytes(StandardCharsets.UTF_8));
            out.flush();
        } catch (Exception e) {

        }
    }

    @org.springframework.web.bind.annotation.ExceptionHandler(UnknownAccountException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public UtilResultSet handlerRTUErrorException(UnknownAccountException errorException) {
        return UtilResultSet.success(errorException.getLocalizedMessage());
    }

    @org.springframework.web.bind.annotation.ExceptionHandler(ServiceException.class)
    @ResponseStatus(HttpStatus.OK)
    public Object handlerRTUErrorException(ServiceException errorException, HttpServletRequest request) {
        return UtilResultSet.bad_request(errorException.getLocalizedMessage());
    }


}
