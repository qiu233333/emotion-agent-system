package com.bishe.backend.security;

/**
 * 当前请求的登录用户上下文。
 *
 * <p>Spring MVC 每次请求通常由一个线程处理，因此这里使用 ThreadLocal 保存当前请求的用户信息。
 * 请求结束后拦截器会清理，避免影响后续请求。</p>
 */
public final class AuthContext {

    /**
     * 当前线程中的登录用户信息。
     */
    private static final ThreadLocal<AuthUser> USER_HOLDER = new ThreadLocal<>();

    /**
     * 工具类不需要实例化。
     */
    private AuthContext() {
    }

    /**
     * 保存当前请求的登录用户。
     *
     * @param authUser 从 JWT 中解析出的登录用户信息
     */
    public static void setCurrentUser(AuthUser authUser) {
        USER_HOLDER.set(authUser);
    }

    /**
     * 获取当前请求的登录用户。
     *
     * @return 当前登录用户；未登录时返回 null
     */
    public static AuthUser getCurrentUser() {
        return USER_HOLDER.get();
    }

    /**
     * 获取当前请求的登录用户 ID。
     *
     * @return 当前登录用户 ID；未登录时返回 null
     */
    public static Long getCurrentUserId() {
        AuthUser authUser = getCurrentUser();
        return authUser == null ? null : authUser.getUserId();
    }

    /**
     * 清理当前线程中的登录用户信息。
     */
    public static void clear() {
        USER_HOLDER.remove();
    }
}
