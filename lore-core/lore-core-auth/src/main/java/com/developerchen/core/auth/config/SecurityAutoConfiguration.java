package com.developerchen.core.auth.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.session.HttpSessionEventPublisher;

/**
 * Spring Security 认证授权模块自动配置类
 * 
 * @author syc
 */
@Configuration
@EnableConfigurationProperties(SecurityProperties.class)
@Import(JwtConfiguration.class)
public class SecurityAutoConfiguration {
    
    /**
     * 会话注册表配置
     * 用于跟踪活跃会话，支持会话并发控制和管理
     */
    @Bean
    @ConditionalOnProperty(name = "security.session.stateful", havingValue = "true", matchIfMissing = true)
    public SessionRegistry sessionRegistry() {
        return new SessionRegistryImpl();
    }
    
    /**
     * HTTP 会话事件发布器
     * 用于发布会话创建和销毁事件
     */
    @Bean
    @ConditionalOnProperty(name = "security.session.stateful", havingValue = "true", matchIfMissing = true)
    public HttpSessionEventPublisher httpSessionEventPublisher() {
        return new HttpSessionEventPublisher();
    }
    
    /**
     * 密码编码器配置
     * 使用委托密码编码器，支持多种编码格式
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }
    

}