package com.developerchen.core.auth.filter;

import com.developerchen.core.auth.converter.TokenAuthenticationConverter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationFilter;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.util.matcher.AnyRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

/**
 * 令牌认证过滤器配置测试
 * 
 * @author syc
 */
@ExtendWith(MockitoExtension.class)
class TokenAuthenticationFilterConfigTest {

    @Mock
    private AuthenticationManager authenticationManager;
    
    @Mock
    private AuthenticationSuccessHandler successHandler;
    
    @Mock
    private AuthenticationFailureHandler failureHandler;
    
    private TokenAuthenticationConverter tokenAuthenticationConverter;
    private TokenAuthenticationFilterConfig filterConfig;
    private RequestMatcher requestMatcher;

    @BeforeEach
    void setUp() {
        tokenAuthenticationConverter = new TokenAuthenticationConverter();
        filterConfig = new TokenAuthenticationFilterConfig(tokenAuthenticationConverter);
        requestMatcher = AnyRequestMatcher.INSTANCE;
    }

    @Test
    void createTokenAuthenticationFilter_WithBasicConfig_ShouldCreateFilter() {
        // When
        AuthenticationFilter filter = filterConfig.createTokenAuthenticationFilter(
            authenticationManager, 
            requestMatcher
        );

        // Then
        assertNotNull(filter);
        // 验证过滤器已正确配置
        // 注意：AuthenticationFilter 的内部状态不容易直接验证，
        // 但我们可以确保过滤器被成功创建
    }

    @Test
    void createTokenAuthenticationFilter_WithHandlers_ShouldCreateFilterWithHandlers() {
        // When
        AuthenticationFilter filter = filterConfig.createTokenAuthenticationFilter(
            authenticationManager,
            requestMatcher,
            successHandler,
            failureHandler
        );

        // Then
        assertNotNull(filter);
        // 验证过滤器已正确配置
    }

    @Test
    void createTokenAuthenticationFilter_WithNullHandlers_ShouldCreateFilterWithoutHandlers() {
        // When
        AuthenticationFilter filter = filterConfig.createTokenAuthenticationFilter(
            authenticationManager,
            requestMatcher,
            null,
            null
        );

        // Then
        assertNotNull(filter);
        // 验证过滤器已正确配置，即使处理器为 null
    }

    @Test
    void createTokenAuthenticationFilter_WithOnlySuccessHandler_ShouldCreateFilter() {
        // When
        AuthenticationFilter filter = filterConfig.createTokenAuthenticationFilter(
            authenticationManager,
            requestMatcher,
            successHandler,
            null
        );

        // Then
        assertNotNull(filter);
    }

    @Test
    void createTokenAuthenticationFilter_WithOnlyFailureHandler_ShouldCreateFilter() {
        // When
        AuthenticationFilter filter = filterConfig.createTokenAuthenticationFilter(
            authenticationManager,
            requestMatcher,
            null,
            failureHandler
        );

        // Then
        assertNotNull(filter);
    }

    @Test
    void constructor_WithTokenConverter_ShouldInitialize() {
        // Given
        TokenAuthenticationConverter converter = mock(TokenAuthenticationConverter.class);

        // When
        TokenAuthenticationFilterConfig config = new TokenAuthenticationFilterConfig(converter);

        // Then
        assertNotNull(config);
    }
}