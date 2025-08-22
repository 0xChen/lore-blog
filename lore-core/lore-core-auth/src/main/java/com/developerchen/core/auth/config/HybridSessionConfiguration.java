package com.developerchen.core.auth.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.SessionManagementConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.RequestMatcher;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 混合会话管理配置
 * 同时支持有状态和无状态会话策略，根据请求上下文动态选择
 * 
 * @author syc
 */
@Configuration
@ConditionalOnProperty(name = "security.session.hybrid", havingValue = "true")
public class HybridSessionConfiguration {

    private final SecurityProperties securityProperties;
    private final SessionRegistry sessionRegistry;
    private final SessionStrategySelector sessionStrategySelector;

    public HybridSessionConfiguration(SecurityProperties securityProperties,
                                    SessionRegistry sessionRegistry,
                                    SessionStrategySelector sessionStrategySelector) {
        this.securityProperties = securityProperties;
        this.sessionRegistry = sessionRegistry;
        this.sessionStrategySelector = sessionStrategySelector;
    }

    /**
     * 混合会话管理的安全过滤器链
     * 根据请求类型动态选择会话策略
     */
    @Bean
    public SecurityFilterChain hybridSecurityFilterChain(HttpSecurity http) throws Exception {
        return http
            .authorizeHttpRequests(authz -> authz
                .requestMatchers("/login", "/api/login", "/api/token/refresh").permitAll()
                .requestMatchers("/css/**", "/js/**", "/images/**", "/favicon.ico").permitAll()
                .anyRequest().authenticated()
            )
            .sessionManagement(session -> configureHybridSessionManagement(session))
            .csrf(csrf -> csrf
                .requireCsrfProtectionMatcher(new HybridCsrfMatcher())
            )
            .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/", true)
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout=true")
                .invalidateHttpSession(true)
                .clearAuthentication(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            )
            .build();
    }

    /**
     * 配置混合会话管理策略
     */
    private void configureHybridSessionManagement(SessionManagementConfigurer<HttpSecurity> session) {
        session
            // 默认使用IF_REQUIRED策略，允许创建会话
            .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
            .sessionFixation().changeSessionId() // 防止会话固定攻击
            .maximumSessions(5) // 每个用户最多5个并发会话
            .maxSessionsPreventsLogin(false) // 不阻止新登录，而是踢出最旧的会话
            .sessionRegistry(sessionRegistry);
    }

    /**
     * 混合CSRF保护匹配器
     * 根据请求类型决定是否需要CSRF保护
     */
    private class HybridCsrfMatcher implements RequestMatcher {
        @Override
        public boolean matches(HttpServletRequest request) {
            // 无状态请求不需要CSRF保护
            if (sessionStrategySelector.shouldUseStatelessSession(request)) {
                return false;
            }
            
            // API请求不需要CSRF保护
            if (request.getRequestURI().startsWith("/api/")) {
                return false;
            }
            
            // 其他请求需要CSRF保护
            return true;
        }
    }
}