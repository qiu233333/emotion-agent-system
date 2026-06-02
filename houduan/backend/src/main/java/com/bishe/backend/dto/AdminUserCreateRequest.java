package com.bishe.backend.dto;

import lombok.Data;

/**
 * 后台新增用户请求对象。
 *
 * <p>管理员新增用户时可以指定账号、密码、昵称、头像、邮箱、角色和状态。
 * 密码不会明文保存，业务层会统一进行 BCrypt 加密。</p>
 */
@Data
public class AdminUserCreateRequest {

    /**
     * 登录用户名，必须唯一。
     */
    private String username;

    /**
     * 初始登录密码，保存前会被 BCrypt 加密。
     */
    private String password;

    /**
     * 用户昵称，可为空。
     */
    private String nickname;

    /**
     * 用户头像地址，可为空。
     */
    private String avatar;

    /**
     * 用户邮箱，可为空。
     */
    private String email;

    /**
     * 用户角色，可选值为 user 或 admin。
     */
    private String role;

    /**
     * 用户状态，1 表示正常，0 表示禁用。
     */
    private Integer status;
}
