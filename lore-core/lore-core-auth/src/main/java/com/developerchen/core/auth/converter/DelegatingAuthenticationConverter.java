package com.developerchen.core.auth.converter;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationConverter;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 委托认证转换器
 * 组合多个转换器，实现转换器的优先级和匹配逻辑
 * 
 * @author syc
 */
@Component
public class DelegatingAuthenticationConverter implements AuthenticationConverter {
    
    private final List<AuthenticationConverter> converters;
    
    public DelegatingAuthenticationConverter(
            FormAuthenticationConverter formConverter,
            ApiAuthenticationConverter apiConverter,
            TokenAuthenticationConverter tokenConverter) {
        // 设置转换器优先级：
        // 1. FormAuthenticationConverter - 处理表单登录
        // 2. ApiAuthenticationConverter - 处理 API JSON 登录
        // 3. TokenAuthenticationConverter - 处理令牌认证
        this.converters = List.of(formConverter, apiConverter, tokenConverter);
    }
    
    @Override
    public Authentication convert(HttpServletRequest request) {
        // 按优先级顺序尝试每个转换器
        for (AuthenticationConverter converter : converters) {
            Authentication authentication = converter.convert(request);
            if (authentication != null) {
                return authentication;
            }
        }
        
        // 没有转换器能处理该请求
        return null;
    }
    
    /**
     * 获取所有转换器
     */
    public List<AuthenticationConverter> getConverters() {
        return converters;
    }
}