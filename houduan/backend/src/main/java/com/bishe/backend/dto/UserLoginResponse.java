package com.bishe.backend.dto;

import lombok.Data;

/**
 * 用户登录成功响应对象。
 *
 * <p>登录成功后，前端会保存 token，并在后续请求中通过 Authorization 请求头自动携带。</p>
 */
@Data
public class UserLoginResponse {

    /**
     * JWT 登录令牌，保存了 userId 和 username。
     */
    private String token;

    /**
     * 用户主键 ID。
     */
    private Long userId;

    /**
     * 用户名。
     */
    private String username;

    /**
     * 用户昵称。
     */
    private String nickname;

    /**
     * 用户角色，例如 user 或 admin。
     */
    private String role;
}
