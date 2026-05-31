package com.bishe.backend.config;

import com.bishe.backend.security.JwtInterceptor;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Spring MVC 配置模块。
 *
 * <p>这里集中注册 Web 层相关配置，目前主要用于注册 JWT 拦截器，
 * 控制哪些接口需要登录后才能访问。</p>
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    /**
     * JWT 登录拦截器。
     */
    @Resource
    private JwtInterceptor jwtInterceptor;

    /**
     * 注册拦截器并放行登录注册接口。
     *
     * @param registry Spring MVC 拦截器注册器
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("/user/login", "/user/register");
    }
}
