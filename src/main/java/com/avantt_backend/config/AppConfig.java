package com.avantt_backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import jakarta.servlet.Filter;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Arquivo de configuração da aplicação. Use para beans e configurações gerais.
 * Aqui configuramos CORS para permitir conexões do frontend.
 */
@Configuration
@EnableWebMvc
public class AppConfig implements WebMvcConfigurer {

    // Defina em application.properties: app.cors.allowed-origins=http://localhost:8443,https://meu-frontend.com
    @Value("${app.cors.allowed-origins:http://localhost:8443}")
    private String allowedOrigins;

    private static final Logger logger = LoggerFactory.getLogger(AppConfig.class);

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        String[] origins = allowedOrigins.split(",");
        registry.addMapping("/**")
                .allowedOrigins(origins)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }

    /**
     * Register a CorsFilter at the servlet filter level so that even preflight
     * requests or error responses include the CORS headers. This complements
     * the WebMvcConfigurer CORS registration and covers cases where Spring MVC
     * handling doesn't run (for example, security filters or static resource
     * handling returning errors).
     */
    @Bean
    public FilterRegistrationBean<CorsFilter> corsFilterRegistration() {
        String[] origins = allowedOrigins.split(",");
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.setAllowedOrigins(Arrays.asList(origins));
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        config.addAllowedHeader("*");

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        FilterRegistrationBean<CorsFilter> bean = new FilterRegistrationBean<>(new CorsFilter(source));
        bean.setOrder(0);
        return bean;
    }

    /**
     * Temporary debug filter: when a request to register-organizacao returns 4xx
     * we log the request and response bodies to help diagnose binding/validation issues.
     * Remove or disable this in production.
     */
    @Bean
    public FilterRegistrationBean<Filter> debugRequestLoggingFilter() {
        FilterRegistrationBean<Filter> reg = new FilterRegistrationBean<>();
        reg.setFilter((servletRequest, servletResponse, chain) -> {
            HttpServletRequest req = (HttpServletRequest) servletRequest;
            HttpServletResponse res = (HttpServletResponse) servletResponse;

            // Only wrap POST calls to the problematic paths to reduce log noise
            // Include register-organizacao and clientes endpoints for debugging
            if (!"POST".equalsIgnoreCase(req.getMethod()) || 
                    !(req.getRequestURI().contains("register-organizacao") || req.getRequestURI().contains("/api/clientes"))) {
                chain.doFilter(servletRequest, servletResponse);
                return;
            }

            // Provide a cache size limit for the request wrapper (constructor requires int in this Spring version)
            ContentCachingRequestWrapper wrappedReq = new ContentCachingRequestWrapper(req, 10240);
            ContentCachingResponseWrapper wrappedRes = new ContentCachingResponseWrapper(res);
            try {
                chain.doFilter(wrappedReq, wrappedRes);
            } finally {
                int status = wrappedRes.getStatus();
                if (status >= 400) {
                    byte[] reqBuf = wrappedReq.getContentAsByteArray();
                    String reqBody = reqBuf.length == 0 ? "" : new String(reqBuf, StandardCharsets.UTF_8);
                    byte[] resBuf = wrappedRes.getContentAsByteArray();
                    String resBody = resBuf.length == 0 ? "" : new String(resBuf, StandardCharsets.UTF_8);
                    logger.error("[DEBUG] {} {} returned {}\nRequest Body: {}\nResponse Body: {}",
                            req.getMethod(), req.getRequestURI(), status, reqBody, resBody);
                }
                // important: copy cached response body back to the real response
                wrappedRes.copyBodyToResponse();
            }
        });
        reg.setOrder(1);
        return reg;
    }
}
