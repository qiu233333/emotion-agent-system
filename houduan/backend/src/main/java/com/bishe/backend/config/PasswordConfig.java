package com.bishe.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 密码加密配置模块。
 *
 * <p>项目使用 BCrypt 保存用户密码，注册时加密，登录时使用 matches 方法校验。</p>
 */
@Configuration
public class PasswordConfig {

    /**
     * 创建 BCrypt 密码编码器。
     *
     * @return BCryptPasswordEncoder 实例
     */
    @Bean
    public BCryptPasswordEncoder bcryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
