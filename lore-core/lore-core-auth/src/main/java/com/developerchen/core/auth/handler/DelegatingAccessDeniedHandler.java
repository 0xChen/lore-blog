package com.developerchen.core.auth.handler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 委托访问拒绝处理器，根据请求类型返回不同的响应
 * - API 请求：返回 JSON 错误响应
 * - 表单请求：重定向到错误页面
 */
@Component
public class DelegatingAccessDeniedHandler implements AccessDeniedHandler {

    private final ApiAccessDeniedHandler apiAccessDeniedHandler;
    private final FormAccessDeniedHandler formAccessDeniedHandler;

    public DelegatingAccessDeniedHandler(
            ApiAccessDeniedHandler apiAccessDeniedHandler,
            FormAccessDeniedHandler formAccessDeniedHandler) {
        this.apiAccessDeniedHandler = apiAccessDeniedHandler;
        this.formAccessDeniedHandler = formAccessDeniedHandler;
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                      AccessDeniedException accessDeniedException) throws IOException, ServletException {
        
        if (isApiRequest(request)) {
            apiAccessDeniedHandler.handle(request, response, accessDeniedException);
        } else {
            formAccessDeniedHandler.handle(request, response, accessDeniedException);
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