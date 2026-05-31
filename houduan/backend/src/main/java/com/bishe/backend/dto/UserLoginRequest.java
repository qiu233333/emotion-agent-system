package com.bishe.backend.dto;

import lombok.Data;

/**
 * 用户登录请求参数。
 *
 * <p>登录接口只需要用户名和密码，后端会校验密码并在成功后签发 JWT。</p>
 */
@Data
public class UserLoginRequest {

    /**
     * 登录用户名。
     */
    private String username;

    /**
     * 登录密码。
     */
    private String password;
}
