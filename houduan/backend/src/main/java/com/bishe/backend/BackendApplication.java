package com.bishe.backend;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 后端服务启动类。
 *
 * <p>Spring Boot 会从该类所在的 {@code com.bishe.backend} 包开始扫描组件，
 * 因此 controller、service、mapper、entity 等业务代码建议都放在该包或其子包下。</p>
 */
@SpringBootApplication
@MapperScan("com.bishe.backend.mapper")
public class BackendApplication {


    /**
     * 后端程序入口方法。
     *
     * @param args 启动命令传入的参数，通常本项目开发阶段不需要手动传参
     */
    public static void main(String[] args) {
        SpringApplication.run(BackendApplication.class, args);
    }
}
