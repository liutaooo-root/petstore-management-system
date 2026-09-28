package com.petstore.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;

/** 登录拦截器：解析 JWT → 写入 UserContext → 校验角色注解 */
@Component
public class AuthInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;

    public AuthInterceptor(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        if (HttpMethod.OPTIONS.matches(request.getMethod())) {
            return true;
        }
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }

        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        if (token == null || token.isBlank()) {
            reject(response, 401, "未登录或登录已过期");
            return false;
        }

        Long userId;
        String username, role;
        try {
            Claims claims = jwtUtil.parse(token);
            userId = ((Number) claims.get("userId")).longValue();
            username = claims.getSubject();
            role = (String) claims.get("role");
        } catch (JwtException | IllegalArgumentException | NullPointerException e) {
            reject(response, 401, "未登录或登录已过期");
            return false;
        }
        UserContext.set(new UserContext.LoginUser(userId, username, role));

        RequireRole requireRole = handlerMethod.getMethodAnnotation(RequireRole.class);
        if (requireRole == null) {
            requireRole = handlerMethod.getBeanType().getAnnotation(RequireRole.class);
        }
        if (requireRole != null && !requireRole.value().equals(role)) {
            reject(response, 403, "无权限执行该操作");
            return false;
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        UserContext.clear();
    }

    /** 业务码走响应体（HTTP 200），前端统一按 code 处理 */
    private void reject(HttpServletResponse response, int code, String msg) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"code\":" + code + ",\"msg\":\"" + msg + "\",\"data\":null}");
    }
}
