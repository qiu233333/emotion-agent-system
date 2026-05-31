package com.bishe.backend.dto;

import lombok.Data;

/**
 * 用户基础信息响应对象。
 *
 * <p>该对象只返回页面展示需要的用户信息，不包含密码字段，避免敏感信息泄露到前端。</p>
 */
@Data
public class UserInfoResponse {

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
     * 用户邮箱。
     */
    private String email;

    /**
     * 用户角色，例如 user 或 admin。
     */
    private String role;
}
