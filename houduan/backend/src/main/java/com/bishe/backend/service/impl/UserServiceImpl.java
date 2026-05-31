package com.bishe.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bishe.backend.dto.UserInfoResponse;
import com.bishe.backend.dto.UserLoginRequest;
import com.bishe.backend.dto.UserLoginResponse;
import com.bishe.backend.dto.UserRegisterRequest;
import com.bishe.backend.entity.User;
import com.bishe.backend.mapper.UserMapper;
import com.bishe.backend.security.JwtUtil;
import com.bishe.backend.service.UserService;
import jakarta.annotation.Resource;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

/**
 * 用户业务实现类。
 *
 * <p>该类负责用户注册、登录、密码 BCrypt 加密校验和 JWT 签发。
 * 为兼容开发阶段已有的明文测试账号，登录成功后会自动把旧明文密码升级为 BCrypt。</p>
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    /**
     * BCrypt 密码编码器。
     */
    @Resource
    private BCryptPasswordEncoder bcryptPasswordEncoder;

    /**
     * JWT 工具对象，用于登录成功后签发 token。
     */
    @Resource
    private JwtUtil jwtUtil;

    /**
     * 注册新用户。
     *
     * @param request 前端提交的注册信息
     * @return 注册成功后的用户基础信息
     */
    @Override
    public UserInfoResponse register(UserRegisterRequest request) {
        String username = trim(request.getUsername());
        String password = trim(request.getPassword());
        if (!StringUtils.hasText(username) || !StringUtils.hasText(password)) {
            throw new IllegalArgumentException("用户名和密码不能为空");
        }

        User existedUser = getByUsername(username);
        if (existedUser != null) {
            throw new IllegalArgumentException("用户名已存在");
        }

        LocalDateTime now = LocalDateTime.now();
        User user = new User();
        user.setUsername(username);
        user.setPassword(bcryptPasswordEncoder.encode(password));
        user.setNickname(StringUtils.hasText(request.getNickname()) ? trim(request.getNickname()) : username);
        user.setEmail(trim(request.getEmail()));
        user.setRole("user");
        user.setStatus(1);
        user.setCreateTime(now);
        user.setUpdateTime(now);
        save(user);

        return toUserInfoResponse(user);
    }

    /**
     * 用户登录并返回 JWT。
     *
     * @param request 前端提交的用户名和密码
     * @return 登录成功后的 token 和用户基础信息
     */
    @Override
    public UserLoginResponse login(UserLoginRequest request) {
        String username = trim(request.getUsername());
        String password = trim(request.getPassword());
        if (!StringUtils.hasText(username) || !StringUtils.hasText(password)) {
            throw new IllegalArgumentException("用户名和密码不能为空");
        }

        User user = getByUsername(username);
        if (user == null || !passwordMatches(password, user.getPassword())) {
            throw new IllegalArgumentException("用户名或密码错误");
        }
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new IllegalArgumentException("账号已被禁用");
        }

        upgradePlainPasswordIfNeeded(user, password);

        UserLoginResponse response = new UserLoginResponse();
        response.setToken(jwtUtil.generateToken(user.getId(), user.getUsername()));
        response.setUserId(user.getId());
        response.setUsername(user.getUsername());
        response.setNickname(user.getNickname());
        response.setRole(user.getRole());
        return response;
    }

    /**
     * 根据用户名查询用户。
     *
     * @param username 用户名
     * @return 用户实体；不存在时返回 null
     */
    private User getByUsername(String username) {
        return getOne(new LambdaQueryWrapper<User>().eq(User::getUsername, username));
    }

    /**
     * 校验用户提交的密码是否正确。
     *
     * @param rawPassword 用户输入的明文密码
     * @param storedPassword 数据库中保存的密码
     * @return true 表示密码正确
     */
    private boolean passwordMatches(String rawPassword, String storedPassword) {
        if (!StringUtils.hasText(storedPassword)) {
            return false;
        }
        if (isBcryptPassword(storedPassword)) {
            return bcryptPasswordEncoder.matches(rawPassword, storedPassword);
        }
        return rawPassword.equals(storedPassword);
    }

    /**
     * 判断数据库密码是否已经是 BCrypt 格式。
     *
     * @param storedPassword 数据库中保存的密码
     * @return true 表示已是 BCrypt 密文
     */
    private boolean isBcryptPassword(String storedPassword) {
        return storedPassword.startsWith("$2a$")
                || storedPassword.startsWith("$2b$")
                || storedPassword.startsWith("$2y$");
    }

    /**
     * 兼容开发阶段旧数据：如果旧密码是明文，登录成功后立即升级为 BCrypt。
     *
     * @param user 当前登录用户
     * @param rawPassword 用户输入的明文密码
     */
    private void upgradePlainPasswordIfNeeded(User user, String rawPassword) {
        if (isBcryptPassword(user.getPassword())) {
            return;
        }

        user.setPassword(bcryptPasswordEncoder.encode(rawPassword));
        user.setUpdateTime(LocalDateTime.now());
        updateById(user);
    }

    /**
     * 转换用户实体为前端安全可见的用户信息。
     *
     * @param user 用户实体
     * @return 不包含密码的用户基础信息
     */
    private UserInfoResponse toUserInfoResponse(User user) {
        UserInfoResponse response = new UserInfoResponse();
        response.setUserId(user.getId());
        response.setUsername(user.getUsername());
        response.setNickname(user.getNickname());
        response.setEmail(user.getEmail());
        response.setRole(user.getRole());
        return response;
    }

    /**
     * 去除字符串首尾空格。
     *
     * @param text 原始字符串
     * @return 处理后的字符串；原值为空时返回 null
     */
    private String trim(String text) {
        return text == null ? null : text.trim();
    }
}
