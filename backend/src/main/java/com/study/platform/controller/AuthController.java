package com.study.platform.controller;

import com.study.platform.dto.LoginRequest;
import com.study.platform.dto.RegisterRequest;
import com.study.platform.service.AuthService;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    // 登录 / 注册 / 获取当前用户信息
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public Map<String, Object> register(@RequestBody RegisterRequest req) {
        return authService.register(req);
    }

    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody LoginRequest req) {
        return authService.login(req);
    }

    @GetMapping("/me")
    public Map<String, Object> me(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return authService.me(userId);
    }
}
