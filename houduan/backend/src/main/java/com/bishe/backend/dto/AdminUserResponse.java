package com.bishe.backend.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 后台用户列表响应对象。
 *
 * <p>该对象用于向后台管理页面展示用户完整基础资料，但不会包含 password 字段，
 * 避免把密码密文或明文传给前端。</p>
 */
@Data
public class AdminUserResponse {

    /**
     * 用户主键 ID。
     */
    private Long id;

    /**
     * 登录用户名。
     */
    private String username;

    /**
     * 用户昵称。
     */
    private String nickname;

    /**
     * 用户头像地址。
     */
    private String avatar;

    /**
     * 用户邮箱。
     */
    private String email;

    /**
     * 用户角色，例如 user 或 admin。
     */
    private String role;

    /**
     * 用户状态，1 表示正常，0 表示禁用。
     */
    private Integer status;

    /**
     * 用户创建时间。
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;

    /**
     * 用户更新时间。
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime updateTime;
}
