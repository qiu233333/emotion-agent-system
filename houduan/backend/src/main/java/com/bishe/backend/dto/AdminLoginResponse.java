package com.bishe.backend.dto;

import lombok.Data;

/**
 * 管理员登录成功响应对象。
 *
 * <p>后台登录成功后，前端会把 token 保存为 adminToken，并把管理员基础信息保存为
 * adminInfo，避免和普通用户登录状态混用。</p>
 */
@Data
public class AdminLoginResponse {

    /**
     * 管理员访问后台接口所需的 JWT。
     */
    private String token;

    /**
     * 管理员用户 ID。
     */
    private Long userId;

    /**
     * 管理员用户名。
     */
    private String username;

    /**
     * 管理员昵称。
     */
    private String nickname;

    /**
     * 管理员头像地址。
     */
    private String avatar;

    /**
     * 管理员邮箱。
     */
    private String email;

    /**
     * 管理员角色，固定应为 admin。
     */
    private String role;

    /**
     * 管理员账号状态，1 表示正常，0 表示禁用。
     */
    private Integer status;
}
