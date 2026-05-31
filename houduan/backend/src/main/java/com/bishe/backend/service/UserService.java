package com.bishe.backend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.bishe.backend.dto.UserInfoResponse;
import com.bishe.backend.dto.UserLoginRequest;
import com.bishe.backend.dto.UserLoginResponse;
import com.bishe.backend.dto.UserRegisterRequest;
import com.bishe.backend.entity.User;

/**
 * 用户业务接口。
 *
 * <p>用户相关的注册、登录和密码加密校验逻辑统一放在 Service 中，
 * Controller 只负责接收请求和返回统一 Result。</p>
 */
public interface UserService extends IService<User> {

    /**
     * 注册新用户。
     *
     * @param request 前端提交的注册信息
     * @return 注册成功后的用户基础信息
     */
    UserInfoResponse register(UserRegisterRequest request);

    /**
     * 用户登录。
     *
     * @param request 前端提交的用户名和密码
     * @return 登录成功后的 token 和用户基础信息
     */
    UserLoginResponse login(UserLoginRequest request);
}
