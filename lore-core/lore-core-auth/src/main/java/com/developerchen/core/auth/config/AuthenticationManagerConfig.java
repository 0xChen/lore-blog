package com.developerchen.core.auth.config;

import com.developerchen.core.auth.provider.OpaqueTokenAuthenticationProvider;
import com.developerchen.core.auth.service.TokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationProvider;

import java.util.ArrayList;
import java.util.List;

/**
 * 认证管理器配置类
 * 配置 AuthenticationManager 和多个 AuthenticationProvider
 * 
 * @author syc
 */
@Configuration
public class AuthenticationManagerConfig {
    
    private final TokenService tokenService;
    private final UserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;
    
    @Autowired(required = false)
    private JwtDecoder jwtDecoder;
    
    public AuthenticationManagerConfig(TokenService tokenService,
                                     UserDetailsService userDetailsService,
                                     PasswordEncoder passwordEncoder) {
        this.tokenService = tokenService;
        this.userDetailsService = userDetailsService;
        this.passwordEncoder = passwordEncoder;
    }
    
    /**
     * 配置认证管理器
     * 设置提供者的优先级和支持的认证类型
     */
    @Bean
    public AuthenticationManager authenticationManager() {
        List<org.springframework.security.authentication.AuthenticationProvider> providers = 
            new ArrayList<>();
        
        // 1. DaoAuthenticationProvider - 处理用户名密码认证
        // 优先级最高，处理表单登录和 API 登录
        DaoAuthenticationProvider daoProvider = createDaoAuthenticationProvider();
        providers.add(daoProvider);
        
        // 2. OpaqueTokenAuthenticationProvider - 处理不透明令牌认证
        // 处理 UUID 类型的访问令牌
        OpaqueTokenAuthenticationProvider opaqueTokenProvider = createOpaqueTokenAuthenticationProvider();
        providers.add(opaqueTokenProvider);
        
        // 3. JwtAuthenticationProvider - 处理 JWT 令牌认证（如果配置了）
        // 处理 JWT 格式的访问令牌
        if (jwtDecoder != null) {
            JwtAuthenticationProvider jwtProvider = createJwtAuthenticationProvider();
            providers.add(jwtProvider);
        }
        
        return new ProviderManager(providers);
    }
    
    /**
     * 创建 DAO 认证提供者
     * 用于用户名密码认证
     */
    private DaoAuthenticationProvider createDaoAuthenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        
        // 配置密码升级策略（可选）
        provider.setPasswordEncoder(passwordEncoder);
        
        // 隐藏用户未找到异常，统一返回认证失败
        provider.setHideUserNotFoundExceptions(true);
        
        return provider;
    }
    
    /**
     * 创建不透明令牌认证提供者
     * 用于 UUID 类型令牌认证
     */
    private OpaqueTokenAuthenticationProvider createOpaqueTokenAuthenticationProvider() {
        return new OpaqueTokenAuthenticationProvider(tokenService, userDetailsService);
    }
    
    /**
     * 创建 JWT 认证提供者
     * 用于 JWT 令牌认证
     */
    private JwtAuthenticationProvider createJwtAuthenticationProvider() {
        JwtAuthenticationProvider provider = new JwtAuthenticationProvider(jwtDecoder);
        
        // 可以配置 JWT 声明转换器（可选）
        // provider.setJwtAuthenticationConverter(jwtAuthenticationConverter);
        
        return provider;
    }
    
    /**
     * 获取所有配置的认证提供者
     * 用于测试和调试
     */
    public List<String> getConfiguredProviders() {
        List<String> providerNames = new ArrayList<>();
        providerNames.add("DaoAuthenticationProvider");
        providerNames.add("OpaqueTokenAuthenticationProvider");
        
        if (jwtDecoder != null) {
            providerNames.add("JwtAuthenticationProvider");
        }
        
        return providerNames;
    }
}