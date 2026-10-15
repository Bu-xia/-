package com.study.platform.config;

import io.jsonwebtoken.Claims;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    // 请求级 JWT 过滤器：校验 token，并把 userId / role 写入请求属性供控制器使用
    private final JwtUtil jwtUtil;

    public JwtAuthFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {
        String path = req.getRequestURI();
        String method = req.getMethod();

        boolean whitelisted = path.equals("/api/health")
                || path.equals("/api/auth/login")
                || path.equals("/api/auth/register");

        if (whitelisted || "OPTIONS".equalsIgnoreCase(method)) {
            chain.doFilter(req, res);
            return;
        }

        String header = req.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            try {
                Claims claims = jwtUtil.parse(header.substring(7));
                req.setAttribute("userId", Long.valueOf(claims.getSubject()));
                req.setAttribute("role", claims.get("role", String.class));
                chain.doFilter(req, res);
                return;
            } catch (Exception e) {
                writeUnauthorized(res, "登录已过期，请重新登录");
                return;
            }
        }
        writeUnauthorized(res, "未登录");
    }

    private void writeUnauthorized(HttpServletResponse res, String message) throws IOException {
        res.setStatus(401);
        res.setContentType("application/json;charset=UTF-8");
        res.getWriter().write("{\"message\":\"" + message + "\"}");
    }
}
