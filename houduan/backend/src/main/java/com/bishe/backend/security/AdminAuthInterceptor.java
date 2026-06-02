package com.bishe.backend.security;

import com.bishe.backend.common.Result;
import com.bishe.backend.entity.User;
import com.bishe.backend.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.nio.charset.StandardCharsets;

/**
 * 管理员权限拦截器。
 *
 * <p>JWT 拦截器只负责确认请求是否已经登录；该拦截器进一步根据当前登录用户 ID
 * 查询数据库，确认账号角色为 admin 且状态正常，防止普通用户访问后台接口。</p>
 */
@Component
public class AdminAuthInterceptor implements HandlerInterceptor {

    /**
     * 用户业务对象，用于查询当前登录账号的角色和状态。
     */
    @Resource
    private UserService userService;

    /**
     * JSON 工具对象，用于把权限错误写回前端。
     */
    @Resource
    private ObjectMapper objectMapper;

    /**
     * 请求进入后台 Controller 前执行管理员权限校验。
     *
     * @param request 当前 HTTP 请求
     * @param response 当前 HTTP 响应
     * @param handler 被拦截的处理器
     * @return true 表示允许访问后台接口；false 表示拒绝访问
     * @throws Exception 写响应失败时抛出
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        Long currentUserId = AuthContext.getCurrentUserId();
        if (currentUserId == null) {
            writeForbidden(response, "未登录，请先登录后台");
            return false;
        }

        User user = userService.getById(currentUserId);
        if (user == null || user.getStatus() == null || user.getStatus() != 1) {
            writeForbidden(response, "管理员账号不存在或已禁用");
            return false;
        }
        if (!"admin".equals(user.getRole())) {
            writeForbidden(response, "当前账号没有后台管理权限");
            return false;
        }

        return true;
    }

    /**
     * 写出 403 无权限响应。
     *
     * @param response 当前 HTTP 响应
     * @param message 返回给前端的错误提示
     * @throws Exception 写响应失败时抛出
     */
    private void writeForbidden(HttpServletResponse response, String message) throws Exception {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType("application/json;charset=UTF-8");
        objectMapper.writeValue(response.getWriter(), Result.error(403, message));
    }
}
