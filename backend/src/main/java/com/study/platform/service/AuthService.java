package com.study.platform.service;

import com.study.platform.config.JwtUtil;
import com.study.platform.dto.LoginRequest;
import com.study.platform.dto.RegisterRequest;
import com.study.platform.entity.User;
import com.study.platform.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class AuthService {

    // 用户注册、登录与当前用户查询，密码使用 BCrypt 加密
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public AuthService(UserRepository userRepository, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
    }

    public Map<String, Object> register(RegisterRequest req) {
        if (req.getUsername() == null || req.getUsername().isBlank()
                || req.getPassword() == null || req.getPassword().isBlank()) {
            throw new RuntimeException("用户名和密码不能为空");
        }
        if (userRepository.existsByUsername(req.getUsername())) {
            throw new RuntimeException("用户名已存在");
        }
        User user = new User();
        user.setUsername(req.getUsername().trim());
        user.setPassword(encoder.encode(req.getPassword()));
        user.setName(req.getName() == null ? req.getUsername() : req.getName());
        user.setEmail(req.getEmail());
        user.setRole("student");
        userRepository.save(user);
        return login(new LoginRequest() {{
            setUsername(req.getUsername().trim());
            setPassword(req.getPassword());
        }});
    }

    public Map<String, Object> login(LoginRequest req) {
        User user = userRepository.findByUsername(req.getUsername() == null ? "" : req.getUsername().trim())
                .orElseThrow(() -> new RuntimeException("用户名或密码错误"));
        if (!encoder.matches(req.getPassword() == null ? "" : req.getPassword(), user.getPassword())) {
            throw new RuntimeException("用户名或密码错误");
        }
        String token = jwtUtil.generate(user.getId(), user.getUsername(), user.getRole());
        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("user", toMap(user));
        return result;
    }

    public Map<String, Object> me(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("用户不存在"));
        return toMap(user);
    }

    private Map<String, Object> toMap(User user) {
        Map<String, Object> m = new HashMap<>();
        m.put("id", user.getId());
        m.put("username", user.getUsername());
        m.put("name", user.getName());
        m.put("role", user.getRole());
        m.put("email", user.getEmail());
        return m;
    }
}
