package com.developerchen.core.auth.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.RememberMeServices;
import org.springframework.security.web.authentication.rememberme.TokenBasedRememberMeServices;

/**
 * 记住我功能配置类
 * 配置 RememberMeServices 和相关的安全属性
 * 
 * @author syc
 */
@Configuration
public class RememberMeConfig {

    /**
     * 配置基于令牌的记住我服务
     * 
     * @param securityProperties 安全配置属性
     * @param userDetailsService 用户详情服务
     * @return RememberMeServices
     */
    @Bean
    @ConditionalOnProperty(name = "security.remember-me.key")
    public RememberMeServices rememberMeServices(
            SecurityProperties securityProperties,
            UserDetailsService userDetailsService) {
        
        SecurityProperties.RememberMe rememberMeConfig = securityProperties.getRememberMe();
        
        TokenBasedRememberMeServices rememberMeServices = 
            new TokenBasedRememberMeServices(rememberMeConfig.getKey(), userDetailsService);
        
        // 配置令牌有效期（转换为秒）
        rememberMeServices.setTokenValiditySeconds((int) rememberMeConfig.getTokenValiditySeconds().toSeconds());
        
        // 配置参数名称
        rememberMeServices.setParameter("remember-me");
        
        // 配置 Cookie 名称
        rememberMeServices.setCookieName("remember-me");
        
        // 配置 Cookie 安全属性
        rememberMeServices.setUseSecureCookie(true); // 在生产环境中使用 HTTPS
        rememberMeServices.setAlwaysRemember(false); // 不总是记住，需要用户明确选择
        
        return rememberMeServices;
    }
}