package cn.gson.oasys.support.exception;

//表单跳转提交暂无权限异常
public class AuthorizationFromException extends RuntimeException {
    public AuthorizationFromException() {
        super("暂无权限");
    }
}
