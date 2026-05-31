package com.bishe.backend.controller;

import com.bishe.backend.common.Result;
import com.bishe.backend.dto.UserInfoResponse;
import com.bishe.backend.dto.UserLoginRequest;
import com.bishe.backend.dto.UserLoginResponse;
import com.bishe.backend.dto.UserRegisterRequest;
import com.bishe.backend.service.UserService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户认证接口控制器。
 *
 * <p>该控制器提供注册和登录接口。项目配置了统一上下文路径 {@code /api}，
 * 因此对外完整路径分别是 {@code /api/user/register} 和 {@code /api/user/login}。</p>
 */
@RestController
@RequestMapping("/user")
public class UserController {

    /**
     * 用户业务对象，用于处理注册、登录和 token 签发。
     */
    @Resource
    private UserService userService;

    /**
     * 用户注册接口。
     *
     * @param request 前端提交的注册信息
     * @return 注册成功后的用户基础信息
     */
    @PostMapping("/register")
    public Result<UserInfoResponse> register(@RequestBody UserRegisterRequest request) {
        return Result.success(userService.register(request));
    }

    /**
     * 用户登录接口。
     *
     * @param request 前端提交的用户名和密码
     * @return 登录成功后的 JWT 和用户基础信息
     */
    @PostMapping("/login")
    public Result<UserLoginResponse> login(@RequestBody UserLoginRequest request) {
        return Result.success(userService.login(request));
    }
}
