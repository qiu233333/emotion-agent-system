package com.bishe.backend.security;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT 生成与解析工具。
 *
 * <p>该工具使用标准 HMAC-SHA256 算法签名 token，不依赖额外 JWT 框架。
 * token 的 payload 中保存 userId、username、iat 和 exp，用于识别登录用户和过期时间。</p>
 */
@Component
public class JwtUtil {

    /**
     * JWT 签名密钥，实际项目建议放到环境变量或配置中心。
     */
    @Value("${jwt.secret:bishe-emotion-system-secret-key-change-me-2026}")
    private String secret;

    /**
     * JWT 有效期，单位为小时。
     */
    @Value("${jwt.expire-hours:24}")
    private Long expireHours;

    /**
     * JSON 序列化工具，用于把 JWT header 和 payload 转为 JSON。
     */
    @Resource
    private ObjectMapper objectMapper;

    /**
     * 根据用户 ID 和用户名生成 JWT。
     *
     * @param userId 登录用户 ID
     * @param username 登录用户名
     * @return 可以返回给前端保存的 JWT 字符串
     */
    public String generateToken(Long userId, String username) {
        try {
            Instant now = Instant.now();
            Map<String, Object> header = new HashMap<>();
            header.put("alg", "HS256");
            header.put("typ", "JWT");

            Map<String, Object> payload = new HashMap<>();
            payload.put("userId", userId);
            payload.put("username", username);
            payload.put("iat", now.getEpochSecond());
            payload.put("exp", now.plus(expireHours, ChronoUnit.HOURS).getEpochSecond());

            String encodedHeader = base64UrlEncode(objectMapper.writeValueAsBytes(header));
            String encodedPayload = base64UrlEncode(objectMapper.writeValueAsBytes(payload));
            String unsignedToken = encodedHeader + "." + encodedPayload;
            return unsignedToken + "." + sign(unsignedToken);
        } catch (Exception e) {
            throw new IllegalArgumentException("token 生成失败");
        }
    }

    /**
     * 解析并校验 JWT。
     *
     * @param token 前端提交的 JWT 字符串
     * @return token 中保存的登录用户信息
     */
    public AuthUser parseToken(String token) {
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("token 不能为空");
        }

        String[] parts = token.split("\\.");
        if (parts.length != 3) {
            throw new IllegalArgumentException("token 格式错误");
        }

        String unsignedToken = parts[0] + "." + parts[1];
        String expectedSignature = sign(unsignedToken);
        if (!MessageDigest.isEqual(expectedSignature.getBytes(StandardCharsets.UTF_8),
                parts[2].getBytes(StandardCharsets.UTF_8))) {
            throw new IllegalArgumentException("token 签名无效");
        }

        Map<String, Object> payload = readPayload(parts[1]);
        Long expireAt = readLong(payload, "exp");
        if (expireAt == null || Instant.now().getEpochSecond() > expireAt) {
            throw new IllegalArgumentException("token 已过期");
        }

        Long userId = readLong(payload, "userId");
        String username = readString(payload, "username");
        if (userId == null || username == null || username.isBlank()) {
            throw new IllegalArgumentException("token 缺少用户信息");
        }

        return new AuthUser(userId, username);
    }

    /**
     * 使用 Base64 URL 安全格式编码字节数组。
     *
     * @param bytes 原始字节数组
     * @return 去掉等号填充的 Base64 URL 字符串
     */
    private String base64UrlEncode(byte[] bytes) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    /**
     * 对 JWT 的 header.payload 部分生成 HMAC-SHA256 签名。
     *
     * @param unsignedToken 未签名的 token 内容
     * @return Base64 URL 格式签名
     */
    private String sign(String unsignedToken) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(secretKey);
            return base64UrlEncode(mac.doFinal(unsignedToken.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new IllegalArgumentException("token 签名失败");
        }
    }

    /**
     * 读取 JWT payload JSON。
     *
     * @param encodedPayload Base64 URL 格式的 payload
     * @return payload 键值对
     */
    private Map<String, Object> readPayload(String encodedPayload) {
        try {
            byte[] payloadBytes = Base64.getUrlDecoder().decode(encodedPayload);
            return objectMapper.readValue(payloadBytes, new TypeReference<>() {
            });
        } catch (Exception e) {
            throw new IllegalArgumentException("token 内容无效");
        }
    }

    /**
     * 从 payload 中读取 Long 类型字段。
     *
     * @param payload JWT payload 键值对
     * @param key 字段名
     * @return Long 值；字段不存在或类型不支持时返回 null
     */
    private Long readLong(Map<String, Object> payload, String key) {
        Object value = payload.get(key);
        if (value instanceof Number number) {
            return number.longValue();
        }
        if (value instanceof String text && !text.isBlank()) {
            return Long.parseLong(text);
        }
        return null;
    }

    /**
     * 从 payload 中读取 String 类型字段。
     *
     * @param payload JWT payload 键值对
     * @param key 字段名
     * @return 字符串值；字段不存在时返回 null
     */
    private String readString(Map<String, Object> payload, String key) {
        Object value = payload.get(key);
        return value == null ? null : String.valueOf(value);
    }
}
