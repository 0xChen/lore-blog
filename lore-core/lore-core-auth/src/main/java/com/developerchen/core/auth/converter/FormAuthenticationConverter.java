package com.developerchen.core.auth.converter;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationConverter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * 表单认证转换器
 * 处理标准表单登录请求，仅处理 /login 路径的表单提交
 * 
 * @author syc
 */
@Component
public class FormAuthenticationConverter implements AuthenticationConverter {
    
    private static final String USERNAME_PARAMETER = "username";
    private static final String PASSWORD_PARAMETER = "password";
    
    private final RequestMatcher requestMatcher = new AntPathRequestMatcher("/login", "POST");
    
    @Override
    public Authentication convert(HttpServletRequest request) {
        if (!requestMatcher.matches(request)) {
            return null;
        }
        
        // 检查是否为表单提交（非 JSON）
        String contentType = request.getContentType();
        if (contentType != null && contentType.contains("application/json")) {
            return null;
        }
        
        String username = obtainUsername(request);
        String password = obtainPassword(request);
        
        if (!StringUtils.hasText(username) || !StringUtils.hasText(password)) {
            return null;
        }
        
        return UsernamePasswordAuthenticationToken.unauthenticated(username, password);
    }
    
    /**
     * 从请求中获取用户名
     */
    protected String obtainUsername(HttpServletRequest request) {
        return request.getParameter(USERNAME_PARAMETER);
    }
    
    /**
     * 从请求中获取密码
     */
    protected String obtainPassword(HttpServletRequest request) {
        return request.getParameter(PASSWORD_PARAMETER);
    }
    
    /**
     * 获取请求匹配器
     */
    public RequestMatcher getRequestMatcher() {
        return requestMatcher;
    }
}