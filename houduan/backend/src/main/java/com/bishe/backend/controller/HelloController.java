package com.bishe.backend.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

    @GetMapping("/hello")
    public String hello() {
        System.out.println("后端收到 /hello 请求");
        return "Hello World";
    }
}
