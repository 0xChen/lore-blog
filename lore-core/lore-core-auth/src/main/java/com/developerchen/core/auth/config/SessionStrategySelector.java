package com.developerchen.core.auth.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * 会话策略选择器
 * 根据请求上下文决定使用有状态还是无状态会话管理
 * 
 * @author syc
 */
@Component
public class SessionStrategySelector {

    private static final String API_PATH_PREFIX = "/api/";
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";
    
    /**
     * 判断请求是否应该使用无状态会话策略
     * 
     * @param request HTTP请求
     * @return true表示使用无状态策略，false表示使用有状态策略
     */
    public boolean shouldUseStatelessSession(HttpServletRequest request) {
        // API路径通常使用无状态策略
        if (isApiRequest(request)) {
            return true;
        }
        
        // 携带Authorization头的请求使用无状态策略
        if (hasBearerToken(request)) {
            return true;
        }
        
        // 明确指定会话策略的请求
        String sessionStrategy = request.getHeader("X-Session-Strategy");
        if (StringUtils.hasText(sessionStrategy)) {
            return "stateless".equalsIgnoreCase(sessionStrategy);
        }
        
        // 默认使用有状态策略
        return false;
    }
    
    /**
     * 判断是否为API请求
     */
    private boolean isApiRequest(HttpServletRequest request) {
        String requestPath = request.getRequestURI();
        return requestPath != null && requestPath.startsWith(API_PATH_PREFIX);
    }
    
    /**
     * 判断请求是否携带Bearer令牌
     */
    private boolean hasBearerToken(HttpServletRequest request) {
        String authHeader = request.getHeader(AUTHORIZATION_HEADER);
        return StringUtils.hasText(authHeader) && authHeader.startsWith(BEARER_PREFIX);
    }
    
    /**
     * 判断请求是否为JSON内容类型
     */
    public boolean isJsonRequest(HttpServletRequest request) {
        String contentType = request.getContentType();
        return contentType != null && contentType.contains("application/json");
    }
    
    /**
     * 判断请求是否为表单内容类型
     */
    public boolean isFormRequest(HttpServletRequest request) {
        String contentType = request.getContentType();
        return contentType != null && contentType.contains("application/x-www-form-urlencoded");
    }
}