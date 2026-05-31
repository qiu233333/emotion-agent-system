package com.bishe.backend.security;

import com.bishe.backend.common.Result;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import java.nio.charset.StandardCharsets;

/**
 * JWT 登录拦截器。
 *
 * <p>除登录和注册接口外，请求进入 Controller 前都会先经过该拦截器。
 * 拦截器负责读取 Authorization 请求头、校验 token，并把当前用户放入 AuthContext。</p>
 */
@Component
public class JwtInterceptor implements HandlerInterceptor {

    /**
     * JWT 工具对象，用于解析和校验 token。
     */
    @Resource
    private JwtUtil jwtUtil;

    /**
     * JSON 工具对象，用于把未登录错误写回前端。
     */
    @Resource
    private ObjectMapper objectMapper;

    /**
     * 请求进入 Controller 前执行登录校验。
     *
     * @param request 当前 HTTP 请求
     * @param response 当前 HTTP 响应
     * @param handler 被拦截的处理器
     * @return true 表示允许继续访问；false 表示拦截请求
     * @throws Exception 写响应失败时抛出
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String token = getToken(request);
        if (!StringUtils.hasText(token)) {
            writeUnauthorized(response, "未登录，请先登录");
            return false;
        }

        try {
            AuthContext.setCurrentUser(jwtUtil.parseToken(token));
            return true;
        } catch (IllegalArgumentException e) {
            writeUnauthorized(response, "登录状态失效，请重新登录");
            return false;
        }
    }

    /**
     * 请求结束后清理当前线程中的登录用户信息。
     *
     * @param request 当前 HTTP 请求
     * @param response 当前 HTTP 响应
     * @param handler 被拦截的处理器
     * @param ex 请求处理过程中抛出的异常
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        AuthContext.clear();
    }

    /**
     * 从 Authorization 请求头中提取 token。
     *
     * @param request 当前 HTTP 请求
     * @return JWT 字符串；没有提交时返回 null
     */
    private String getToken(HttpServletRequest request) {
        String authorization = request.getHeader("Authorization");
        if (!StringUtils.hasText(authorization)) {
            return null;
        }
        if (authorization.startsWith("Bearer ")) {
            return authorization.substring(7);
        }
        return authorization;
    }

    /**
     * 写出 401 未登录响应。
     *
     * @param response 当前 HTTP 响应
     * @param message 返回给前端的错误提示
     * @throws Exception 写响应失败时抛出
     */
    private void writeUnauthorized(HttpServletResponse response, String message) throws Exception {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType("application/json;charset=UTF-8");
        objectMapper.writeValue(response.getWriter(), Result.error(401, message));
    }
}
