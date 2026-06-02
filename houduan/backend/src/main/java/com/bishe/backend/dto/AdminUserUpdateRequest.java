package com.bishe.backend.dto;

import lombok.Data;

/**
 * 后台修改用户请求对象。
 *
 * <p>管理员修改用户资料时不直接编辑用户名和密码；用户名作为账号标识保持不变，
 * 密码通过独立重置密码接口处理。</p>
 */
@Data
public class AdminUserUpdateRequest {

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
