package com.developerchen.core.auth.config;

import com.developerchen.core.auth.converter.ApiAuthenticationConverter;
import com.developerchen.core.auth.converter.DelegatingAuthenticationConverter;
import com.developerchen.core.auth.entrypoint.DelegatingAuthenticationEntryPoint;
import com.developerchen.core.auth.filter.ApiLoginAuthenticationFilter;
import com.developerchen.core.auth.filter.HybridSessionCreationFilter;
import com.developerchen.core.auth.filter.TokenAuthenticationFilterConfig;
import com.developerchen.core.auth.handler.DelegatingAccessDeniedHandler;
import com.developerchen.core.auth.handler.DelegatingAuthenticationFailureHandler;
import com.developerchen.core.auth.handler.DelegatingAuthenticationSuccessHandler;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFilter;
import org.springframework.security.web.authentication.RememberMeServices;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.NegatedRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

import java.util.List;

/**
 * Spring Security 主配置类
 * 配置安全过滤器链，集成所有自定义组件
 * 
 * @author syc
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final SecurityProperties securityProperties;
    private final SessionRegistry sessionRegistry;
    private final DelegatingAuthenticationSuccessHandler delegatingAuthenticationSuccessHandler;
    private final DelegatingAuthenticationFailureHandler delegatingAuthenticationFailureHandler;
    private final DelegatingAuthenticationEntryPoint delegatingAuthenticationEntryPoint;
    private final DelegatingAccessDeniedHandler delegatingAccessDeniedHandler;
    private final ApiAuthenticationConverter apiAuthenticationConverter;
    private final TokenAuthenticationFilterConfig tokenAuthenticationFilterConfig;

    public SecurityConfig(SecurityProperties securityProperties,
                         SessionRegistry sessionRegistry,
                         DelegatingAuthenticationSuccessHandler delegatingAuthenticationSuccessHandler,
                         DelegatingAuthenticationFailureHandler delegatingAuthenticationFailureHandler,
                         DelegatingAuthenticationEntryPoint delegatingAuthenticationEntryPoint,
                         DelegatingAccessDeniedHandler delegatingAccessDeniedHandler,
                         ApiAuthenticationConverter apiAuthenticationConverter,
                         TokenAuthenticationFilterConfig tokenAuthenticationFilterConfig) {
        this.securityProperties = securityProperties;
        this.sessionRegistry = sessionRegistry;
        this.delegatingAuthenticationSuccessHandler = delegatingAuthenticationSuccessHandler;
        this.delegatingAuthenticationFailureHandler = delegatingAuthenticationFailureHandler;
        this.delegatingAuthenticationEntryPoint = delegatingAuthenticationEntryPoint;
        this.delegatingAccessDeniedHandler = delegatingAccessDeniedHandler;
        this.apiAuthenticationConverter = apiAuthenticationConverter;
        this.tokenAuthenticationFilterConfig = tokenAuthenticationFilterConfig;
    }

    /**
     * 有状态会话管理的安全过滤器链
     */
    @Bean
    @ConditionalOnProperty(name = "security.session.stateful", havingValue = "true", matchIfMissing = true)
    public SecurityFilterChain statefulSecurityFilterChain(HttpSecurity http,
                                                          RememberMeServices rememberMeServices,
                                                          HybridSessionCreationFilter hybridSessionCreationFilter,
                                                          AuthenticationManager authenticationManager) throws Exception {
        
        // 创建 API 登录过滤器
        ApiLoginAuthenticationFilter apiLoginFilter = new ApiLoginAuthenticationFilter(
            authenticationManager, apiAuthenticationConverter);
        apiLoginFilter.setAuthenticationSuccessHandler(delegatingAuthenticationSuccessHandler);
        apiLoginFilter.setAuthenticationFailureHandler(delegatingAuthenticationFailureHandler);
        
        // 创建令牌认证过滤器
        RequestMatcher tokenRequestMatcher = createTokenRequestMatcher();
        AuthenticationFilter tokenAuthenticationFilter = tokenAuthenticationFilterConfig
            .createTokenAuthenticationFilter(authenticationManager, tokenRequestMatcher);
        
        return http
            .authorizeHttpRequests(authz -> authz
                .requestMatchers("/login", "/api/login", "/api/token/refresh").permitAll()
                .requestMatchers("/css/**", "/js/**", "/images/**", "/favicon.ico", "/error").permitAll()
                .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                .anyRequest().authenticated()
            )
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                .sessionFixation().changeSessionId() // 防止会话固定攻击
                .maximumSessions(5) // 每个用户最多5个并发会话
                .maxSessionsPreventsLogin(false) // 不阻止新登录，而是踢出最旧的会话
                .sessionRegistry(sessionRegistry)
            )
            .csrf(csrf -> csrf
                .ignoringRequestMatchers("/api/**") // API端点禁用CSRF
            )
            .formLogin(form -> form
                .loginPage("/login")
                .successHandler(delegatingAuthenticationSuccessHandler)
                .failureHandler(delegatingAuthenticationFailureHandler)
                .permitAll()
            )
            .rememberMe(remember -> remember
                .rememberMeServices(rememberMeServices)
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout=true")
                .invalidateHttpSession(true)
                .clearAuthentication(true)
                .deleteCookies("JSESSIONID", "remember-me")
                .permitAll()
            )
            .exceptionHandling(exceptions -> exceptions
                .authenticationEntryPoint(delegatingAuthenticationEntryPoint)
                .accessDeniedHandler(delegatingAccessDeniedHandler)
            )
            // 添加自定义过滤器
            .addFilterBefore(apiLoginFilter, UsernamePasswordAuthenticationFilter.class)
            .addFilterAfter(tokenAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            .addFilterAfter(hybridSessionCreationFilter, tokenAuthenticationFilter.getClass())
            .build();
    }

    /**
     * 无状态会话管理的安全过滤器链
     */
    @Bean
    @ConditionalOnProperty(name = "security.session.stateful", havingValue = "false")
    public SecurityFilterChain statelessSecurityFilterChain(HttpSecurity http,
                                                           AuthenticationManager authenticationManager) throws Exception {
        
        // 创建 API 登录过滤器
        ApiLoginAuthenticationFilter apiLoginFilter = new ApiLoginAuthenticationFilter(
            authenticationManager, apiAuthenticationConverter);
        apiLoginFilter.setAuthenticationSuccessHandler(delegatingAuthenticationSuccessHandler);
        apiLoginFilter.setAuthenticationFailureHandler(delegatingAuthenticationFailureHandler);
        
        // 创建令牌认证过滤器
        RequestMatcher tokenRequestMatcher = createTokenRequestMatcher();
        AuthenticationFilter tokenAuthenticationFilter = tokenAuthenticationFilterConfig
            .createTokenAuthenticationFilter(authenticationManager, tokenRequestMatcher);
        
        return http
            .authorizeHttpRequests(authz -> authz
                .requestMatchers("/api/login", "/api/token/refresh").permitAll()
                .requestMatchers("/css/**", "/js/**", "/images/**", "/favicon.ico", "/error").permitAll()
                .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                .anyRequest().authenticated()
            )
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .csrf(csrf -> csrf.disable()) // 无状态模式完全禁用CSRF
            .formLogin(form -> form.disable()) // 无状态模式禁用表单登录
            .httpBasic(basic -> basic.disable()) // 禁用HTTP Basic认证
            .exceptionHandling(exceptions -> exceptions
                .authenticationEntryPoint(delegatingAuthenticationEntryPoint)
                .accessDeniedHandler(delegatingAccessDeniedHandler)
            )
            // 添加自定义过滤器
            .addFilterBefore(apiLoginFilter, UsernamePasswordAuthenticationFilter.class)
            .addFilterAfter(tokenAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            .build();
    }



    /**
     * 创建令牌请求匹配器
     * 匹配需要令牌认证的请求（排除登录和刷新端点）
     */
    private RequestMatcher createTokenRequestMatcher() {
        // 排除的路径
        RequestMatcher excludedPaths = new OrRequestMatcher(
            new AntPathRequestMatcher("/login"),
            new AntPathRequestMatcher("/api/login"),
            new AntPathRequestMatcher("/api/token/refresh"),
            new AntPathRequestMatcher("/css/**"),
            new AntPathRequestMatcher("/js/**"),
            new AntPathRequestMatcher("/images/**"),
            new AntPathRequestMatcher("/favicon.ico"),
            new AntPathRequestMatcher("/error"),
            new AntPathRequestMatcher("/actuator/health"),
            new AntPathRequestMatcher("/actuator/info")
        );
        
        // 返回排除路径的否定匹配器
        return new NegatedRequestMatcher(excludedPaths);
    }
}