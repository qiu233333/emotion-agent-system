package com.bishe.backend.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 前后端联调用的测试控制器。
 *
 * <p>当前只提供一个简单的 {@code /hello} 接口，用来确认 Vue 前端可以通过 axios
 * 请求到 Spring Boot 后端。</p>
 */
@RestController
public class HelloController {

    /**
     * 处理前端发来的 hello 测试请求。
     *
     * @return 固定返回字符串，前端收到该内容即可说明基础链路已经打通
     */
    @GetMapping("/hello")
    public String hello() {
        System.out.println("后端收到 /hello 请求");
        return "Hello World";
    }
}
