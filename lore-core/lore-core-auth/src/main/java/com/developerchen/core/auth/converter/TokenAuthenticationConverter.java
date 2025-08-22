package com.developerchen.core.auth.converter;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthenticationToken;
import org.springframework.security.web.authentication.AuthenticationConverter;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * 令牌认证转换器
 * 从请求中提取访问令牌，支持从 HTTP 头和 URL 参数中提取令牌
 * 根据令牌格式创建相应的 Authentication 对象（JWT 或不透明令牌）
 * 
 * @author syc
 */
@Component
public class TokenAuthenticationConverter implements AuthenticationConverter {
    
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";
    private static final String ACCESS_TOKEN_PARAMETER = "access_token";
    
    @Override
    public Authentication convert(HttpServletRequest request) {
        String token = extractToken(request);
        
        if (!StringUtils.hasText(token)) {
            return null;
        }
        
        // 根据令牌格式判断类型
        if (isJwtToken(token)) {
            // JWT 令牌使用 Spring Security 的标准 BearerTokenAuthenticationToken
            return new BearerTokenAuthenticationToken(token);
        } else {
            // 不透明令牌使用自定义的 OpaqueTokenAuthenticationToken
            return new OpaqueTokenAuthenticationToken(token);
        }
    }
    
    /**
     * 从请求中提取令牌
     * 优先从 Authorization 头中提取，然后从 URL 参数中提取
     */
    private String extractToken(HttpServletRequest request) {
        // 1. 从 Authorization 头中提取
        String authorizationHeader = request.getHeader(AUTHORIZATION_HEADER);
        if (StringUtils.hasText(authorizationHeader) && authorizationHeader.startsWith(BEARER_PREFIX)) {
            return authorizationHeader.substring(BEARER_PREFIX.length());
        }
        
        // 2. 从 URL 参数中提取
        String tokenParameter = request.getParameter(ACCESS_TOKEN_PARAMETER);
        if (StringUtils.hasText(tokenParameter)) {
            return tokenParameter;
        }
        
        return null;
    }
    
    /**
     * 判断是否为 JWT 令牌
     * JWT 令牌通常包含两个点号分隔符，格式为 header.payload.signature
     */
    private boolean isJwtToken(String token) {
        if (!StringUtils.hasText(token)) {
            return false;
        }
        
        // 简单的 JWT 格式检查：包含两个点号
        String[] parts = token.split("\\.");
        return parts.length == 3;
    }
}