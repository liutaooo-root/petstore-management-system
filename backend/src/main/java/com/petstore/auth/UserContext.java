package com.petstore.auth;

/** 当前登录用户上下文（拦截器写入，业务层读取） */
public final class UserContext {

    public record LoginUser(Long id, String username, String role) {}

    private static final ThreadLocal<LoginUser> HOLDER = new ThreadLocal<>();

    private UserContext() {}

    public static void set(LoginUser user) {
        HOLDER.set(user);
    }

    public static LoginUser get() {
        return HOLDER.get();
    }

    public static Long currentId() {
        LoginUser user = HOLDER.get();
        return user == null ? null : user.id();
    }

    public static void clear() {
        HOLDER.remove();
    }
}
