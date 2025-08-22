package com.developerchen.core.auth.config;

import com.developerchen.core.auth.converter.ApiAuthenticationConverter;
import com.developerchen.core.auth.entrypoint.DelegatingAuthenticationEntryPoint;
import com.developerchen.core.auth.filter.TokenAuthenticationFilterConfig;
import com.developerchen.core.auth.handler.DelegatingAccessDeniedHandler;
import com.developerchen.core.auth.handler.DelegatingAuthenticationFailureHandler;
import com.developerchen.core.auth.handler.DelegatingAuthenticationSuccessHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.session.SessionRegistry;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * SecurityConfig 测试类
 * 验证安全配置是否正确应用
 * 
 * @author syc
 */
@ExtendWith(MockitoExtension.class)
class SecurityConfigTest {

    @Mock
    private SecurityProperties securityProperties;
    
    @Mock
    private SessionRegistry sessionRegistry;
    
    @Mock
    private DelegatingAuthenticationSuccessHandler delegatingAuthenticationSuccessHandler;
    
    @Mock
    private DelegatingAuthenticationFailureHandler delegatingAuthenticationFailureHandler;
    
    @Mock
    private DelegatingAuthenticationEntryPoint delegatingAuthenticationEntryPoint;
    
    @Mock
    private DelegatingAccessDeniedHandler delegatingAccessDeniedHandler;
    
    @Mock
    private ApiAuthenticationConverter apiAuthenticationConverter;
    
    @Mock
    private TokenAuthenticationFilterConfig tokenAuthenticationFilterConfig;
    
    private SecurityConfig securityConfig;
    
    @BeforeEach
    void setUp() {
        securityConfig = new SecurityConfig(
            securityProperties,
            sessionRegistry,
            delegatingAuthenticationSuccessHandler,
            delegatingAuthenticationFailureHandler,
            delegatingAuthenticationEntryPoint,
            delegatingAccessDeniedHandler,
            apiAuthenticationConverter,
            tokenAuthenticationFilterConfig
        );
    }

    @Test
    void securityConfig_ShouldHaveCorrectConfiguration() {
        // 验证配置类存在且可以实例化
        assertThat(securityConfig).isNotNull();
        
        // 验证配置类有正确的注解
        assertThat(securityConfig.getClass().isAnnotationPresent(org.springframework.context.annotation.Configuration.class)).isTrue();
        assertThat(securityConfig.getClass().isAnnotationPresent(org.springframework.security.config.annotation.web.configuration.EnableWebSecurity.class)).isTrue();
    }

    @Test
    void securityConfig_ShouldBeInstantiable() {
        // 测试 SecurityConfig 类可以正常实例化
        assertThat(securityConfig).isNotNull();
    }
}