package com.bishe.backend;

import jakarta.annotation.Resource;
import com.bishe.backend.mapper.UserMapper;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * 后端应用启动测试类。
 *
 * <p>该测试用于确认 Spring Boot 应用上下文可以正常启动，
 * 并且 MyBatis Mapper 能够被 Spring 扫描和注入。</p>
 */
@SpringBootTest
class BackendApplicationTests {

    /**
     * 用户 Mapper，用于验证 MyBatis-Plus 数据访问对象是否正常注入。
     */
    @Resource
    private UserMapper userMapper;

    /**
     * 验证 Spring Boot 上下文加载成功。
     */
    @Test
    void contextLoads() {
        assertNotNull(userMapper);
    }
}
