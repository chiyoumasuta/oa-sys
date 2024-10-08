package cn.gson.oasys.support;


import cn.gson.oasys.entity.User;

import java.io.Serializable;

/**
 * app线程令牌
 *
 * @author 不愿透露
 */
public class AppUserToken implements Serializable {
    public static final ThreadLocal<AppUserToken> THREAD_LOCAL_TOKEN = new ThreadLocal<>();
    private User user;


    public AppUserToken() {
        super();
    }

    public AppUserToken(User user) {
        this.user = user;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }


}