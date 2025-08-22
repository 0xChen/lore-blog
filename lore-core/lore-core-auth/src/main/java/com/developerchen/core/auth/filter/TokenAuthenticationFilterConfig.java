package com.developerchen.core.auth.filter;

import com.developerchen.core.auth.converter.TokenAuthenticationConverter;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationFilter;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.stereotype.Component;

/**
 * 令牌认证过滤器配置
 * 使用 Spring Security 的 AuthenticationFilter 配置令牌认证
 * 
 * @author syc
 */
@Component
public class TokenAuthenticationFilterConfig {
    
    private final TokenAuthenticationConverter tokenAuthenticationConverter;
    
    public TokenAuthenticationFilterConfig(TokenAuthenticationConverter tokenAuthenticationConverter) {
        this.tokenAuthenticationConverter = tokenAuthenticationConverter;
    }
    
    /**
     * 创建令牌认证过滤器
     * 
     * @param authenticationManager 认证管理器
     * @param requestMatcher 请求匹配器，用于确定哪些请求需要令牌认证
     * @return 配置好的 AuthenticationFilter
     */
    public AuthenticationFilter createTokenAuthenticationFilter(
            AuthenticationManager authenticationManager,
            RequestMatcher requestMatcher) {
        
        AuthenticationFilter filter = new AuthenticationFilter(
            authenticationManager, 
            tokenAuthenticationConverter
        );
        
        // 设置请求匹配器
        filter.setRequestMatcher(requestMatcher);
        
        return filter;
    }
    
    /**
     * 创建令牌认证过滤器，带成功和失败处理器
     * 
     * @param authenticationManager 认证管理器
     * @param requestMatcher 请求匹配器
     * @param successHandler 认证成功处理器
     * @param failureHandler 认证失败处理器
     * @return 配置好的 AuthenticationFilter
     */
    public AuthenticationFilter createTokenAuthenticationFilter(
            AuthenticationManager authenticationManager,
            RequestMatcher requestMatcher,
            AuthenticationSuccessHandler successHandler,
            AuthenticationFailureHandler failureHandler) {
        
        AuthenticationFilter filter = createTokenAuthenticationFilter(
            authenticationManager, 
            requestMatcher
        );
        
        // 设置成功和失败处理器
        if (successHandler != null) {
            filter.setSuccessHandler(successHandler);
        }
        
        if (failureHandler != null) {
            filter.setFailureHandler(failureHandler);
        }
        
        return filter;
    }
}