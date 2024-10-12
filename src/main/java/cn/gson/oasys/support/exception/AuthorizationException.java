package cn.gson.oasys.support.exception;
//json提交暂无权限异常
public class AuthorizationException extends RuntimeException {
    public AuthorizationException() {
        super("暂无权限");
    }
}
