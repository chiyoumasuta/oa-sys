package cn.gson.oasys.permission.anno;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 自定义权限注解
 *
 * @author 不愿透露
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequestPermission {

    /*
     *权限标识，可多个
     */
    String[] value();

    /*
     * 是否需要验证参数：同个接口多个地方使用这里需要为true，然后前端传参数需要带有permission 来对权限进行匹配操作
     */
    boolean param() default false;

    boolean log() default false;
}
