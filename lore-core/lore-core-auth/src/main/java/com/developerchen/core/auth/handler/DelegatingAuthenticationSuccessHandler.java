package com.developerchen.core.auth.handler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 委托认证成功处理器
 * 根据请求类型路由到不同的成功处理器
 * 
 * @author syc
 */
@Component
public class DelegatingAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final ApiAuthenticationSuccessHandler apiSuccessHandler;
    private final FormAuthenticationSuccessHandler formSuccessHandler;

    public DelegatingAuthenticationSuccessHandler(
            ApiAuthenticationSuccessHandler apiSuccessHandler,
            FormAuthenticationSuccessHandler formSuccessHandler) {
        this.apiSuccessHandler = apiSuccessHandler;
        this.formSuccessHandler = formSuccessHandler;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                      Authentication authentication) throws IOException, ServletException {
        
        if (isApiRequest(request)) {
            apiSuccessHandler.onAuthenticationSuccess(request, response, authentication);
        } else {
            formSuccessHandler.onAuthenticationSuccess(request, response, authentication);
        }
    }

    /**
     * 判断是否为 API 请求
     * 
     * @param request HTTP 请求
     * @return 如果是 API 请求返回 true
     */
    private boolean isApiRequest(HttpServletRequest request) {
        // 检查请求路径
        String requestURI = request.getRequestURI();
        if (requestURI.startsWith("/api/")) {
            return true;
        }
        
        // 检查 Accept 头
        String acceptHeader = request.getHeader("Accept");
        if (acceptHeader != null && acceptHeader.contains(MediaType.APPLICATION_JSON_VALUE)) {
            return true;
        }
        
        // 检查 Content-Type 头
        String contentType = request.getContentType();
        if (contentType != null && contentType.contains(MediaType.APPLICATION_JSON_VALUE)) {
            return true;
        }
        
        // 检查 X-Requested-With 头（AJAX 请求）
        String requestedWith = request.getHeader("X-Requested-With");
        if ("XMLHttpRequest".equals(requestedWith)) {
            return true;
        }
        
        return false;
    }
}