package com.developerchen.core.auth.entrypoint;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 委托认证入口点，根据请求类型返回不同的响应
 * - API 请求：返回 JSON 错误响应
 * - 表单请求：重定向到登录页面
 */
@Component
public class DelegatingAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ApiAuthenticationEntryPoint apiEntryPoint;
    private final FormAuthenticationEntryPoint formEntryPoint;

    public DelegatingAuthenticationEntryPoint(
            ApiAuthenticationEntryPoint apiEntryPoint,
            FormAuthenticationEntryPoint formEntryPoint) {
        this.apiEntryPoint = apiEntryPoint;
        this.formEntryPoint = formEntryPoint;
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                        AuthenticationException authException) throws IOException, ServletException {
        
        if (isApiRequest(request)) {
            apiEntryPoint.commence(request, response, authException);
        } else {
            formEntryPoint.commence(request, response, authException);
        }
    }

    /**
     * 判断是否为 API 请求
     */
    private boolean isApiRequest(HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        String acceptHeader = request.getHeader("Accept");
        String contentType = request.getContentType();

        // 检查 URL 路径
        if (requestURI.startsWith("/api/")) {
            return true;
        }

        // 检查 Accept 头
        if (acceptHeader != null && acceptHeader.contains(MediaType.APPLICATION_JSON_VALUE)) {
            return true;
        }

        // 检查 Content-Type 头
        if (contentType != null && contentType.contains(MediaType.APPLICATION_JSON_VALUE)) {
            return true;
        }

        return false;
    }
}