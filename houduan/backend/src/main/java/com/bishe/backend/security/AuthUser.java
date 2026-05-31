package com.bishe.backend.security;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 当前登录用户信息。
 *
 * <p>JWT 校验通过后，拦截器会把 token 中解析出的用户 ID 和用户名放入该对象，
 * 供业务层查询当前用户自己的数据。</p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthUser {

    /**
     * 当前登录用户 ID。
     */
    private Long userId;

    /**
     * 当前登录用户名。
     */
    private String username;
}
