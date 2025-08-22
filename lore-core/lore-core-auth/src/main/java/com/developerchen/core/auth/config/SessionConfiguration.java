package com.developerchen.core.auth.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;
import org.springframework.session.web.http.CookieHttpSessionIdResolver;
import org.springframework.session.web.http.HttpSessionIdResolver;

/**
 * 会话管理配置
 * 
 * @author syc
 */
@Configuration
@ConditionalOnProperty(name = "security.session.stateful", havingValue = "true", matchIfMissing = true)
@EnableRedisHttpSession(maxInactiveIntervalInSeconds = 1800) // 30分钟
public class SessionConfiguration {

    private final SecurityProperties securityProperties;

    public SessionConfiguration(SecurityProperties securityProperties) {
        this.securityProperties = securityProperties;
    }

    /**
     * 配置会话ID解析器
     * 使用Cookie方式存储会话ID
     */
    @Bean
    public HttpSessionIdResolver httpSessionIdResolver() {
        return new CookieHttpSessionIdResolver();
    }

    /**
     * 配置Redis会话存储
     */
    @Bean
    @ConditionalOnProperty(name = "security.session.stateful", havingValue = "true")
    public org.springframework.session.data.redis.config.ConfigureRedisAction configureRedisAction() {
        // 禁用Redis配置，避免在某些环境下的权限问题
        return org.springframework.session.data.redis.config.ConfigureRedisAction.NO_OP;
    }
}