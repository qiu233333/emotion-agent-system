package com.bishe.backend.dto;

import lombok.Data;

/**
 * 用户注册请求参数。
 *
 * <p>前端注册页面会把用户名、密码、昵称和邮箱提交到该对象中，
 * 后端 Service 再负责校验用户名是否重复并加密密码。</p>
 */
@Data
public class UserRegisterRequest {

    /**
     * 登录用户名，要求在用户表中唯一。
     */
    private String username;

    /**
     * 登录密码，保存数据库前必须经过 BCrypt 加密。
     */
    private String password;

    /**
     * 用户昵称，可为空；为空时后端默认使用用户名。
     */
    private String nickname;

    /**
     * 用户邮箱，可为空，后续可用于找回密码或接收通知。
     */
    private String email;
}
