package cn.gson.oasys.support.exception;
//登录失效
public class UnknownAccountException extends RuntimeException {
    public UnknownAccountException() {
        super("登录失效");
    }
}
