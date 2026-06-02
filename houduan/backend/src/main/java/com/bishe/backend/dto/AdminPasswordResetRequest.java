package com.bishe.backend.dto;

import lombok.Data;

/**
 * 后台重置用户密码请求对象。
 *
 * <p>管理员只能设置新密码，不能读取用户原密码，避免密码明文泄露。</p>
 */
@Data
public class AdminPasswordResetRequest {

    /**
     * 管理员为用户设置的新密码。
     */
    private String password;
}
