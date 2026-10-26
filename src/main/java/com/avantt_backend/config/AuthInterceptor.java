package com.avantt_backend.config;

import com.avantt_backend.exception.UnauthorizedException;
import com.avantt_backend.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    public static final String AUTHENTICATED_USER_ATTRIBUTE = "avantt.authenticatedUser";

    private final AuthService authService;

    public AuthInterceptor(AuthService authService) {
        this.authService = authService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String path = request.getRequestURI();
        if (isPublic(path) || "OPTIONS".equalsIgnoreCase(request.getMethod())) return true;

        String authorization = request.getHeader("Authorization");
        if (authorization == null || authorization.isBlank()) {
            throw new UnauthorizedException("Token ausente");
        }

        String token = authorization.toLowerCase().startsWith("bearer ")
                ? authorization.substring(7).trim()
                : authorization.trim();
        var authenticatedUser = authService.authenticateToken(token);
        if (authenticatedUser == null) {
            throw new UnauthorizedException("Token inválido ou expirado");
        }
        request.setAttribute(AUTHENTICATED_USER_ATTRIBUTE, authenticatedUser);
        return true;
    }

    private boolean isPublic(String path) {
        return path.equals("/api/auth/login")
                || path.equals("/api/auth/register")
                || path.equals("/api/auth/register-organizacao")
                || path.equals("/api/auth/password-reset/request")
                || path.equals("/api/auth/password-reset/confirm")
                || path.equals("/api/auth/convite/aceitar")
                || path.equals("/api/status")
                || path.startsWith("/swagger-ui")
                || path.startsWith("/v3/api-docs");
    }
}
