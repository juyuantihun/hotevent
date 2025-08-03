package com.hotech.events.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 测试认证控制器
 */
@RestController
@RequestMapping("/api/test-auth")
public class TestAuthController {

    /**
     * 测试接口
     * @return 测试结果
     */
    @GetMapping("/hello")
    public Map<String, String> hello() {
        Map<String, String> result = new HashMap<>();
        result.put("message", "Hello from TestAuthController");
        return result;
    }
}