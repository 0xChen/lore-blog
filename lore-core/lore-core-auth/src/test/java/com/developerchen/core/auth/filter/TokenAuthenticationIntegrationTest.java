package com.developerchen.core.auth.filter;

import com.developerchen.core.auth.converter.OpaqueTokenAuthenticationToken;
import com.developerchen.core.auth.converter.TokenAuthenticationConverter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthenticationToken;
import org.springframework.security.web.authentication.AuthenticationFilter;
import org.springframework.security.web.util.matcher.AnyRequestMatcher;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 令牌认证集成测试
 * 测试令牌认证过滤器的完整流程
 * 
 * @author syc
 */
@ExtendWith(MockitoExtension.class)
class TokenAuthenticationIntegrationTest {

    @Mock
    private AuthenticationManager authenticationManager;
    
    @Mock
    private FilterChain filterChain;
    
    private TokenAuthenticationConverter tokenAuthenticationConverter;
    private TokenAuthenticationFilterConfig filterConfig;

    @BeforeEach
    void setUp() {
        tokenAuthenticationConverter = new TokenAuthenticationConverter();
        filterConfig = new TokenAuthenticationFilterConfig(tokenAuthenticationConverter);
    }

    @Test
    void tokenAuthentication_WithValidJwtToken_ShouldAuthenticate() throws ServletException, IOException {
        // Given
        String jwtToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ0ZXN0dXNlciJ9.signature";
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + jwtToken);
        MockHttpServletResponse response = new MockHttpServletResponse();
        
        Authentication expectedAuth = new TestingAuthenticationToken("testuser", null, "ROLE_USER");
        when(authenticationManager.authenticate(any(BearerTokenAuthenticationToken.class)))
            .thenReturn(expectedAuth);
        
        AuthenticationFilter filter = filterConfig.createTokenAuthenticationFilter(
            authenticationManager, 
            AnyRequestMatcher.INSTANCE
        );

        // When
        filter.doFilter(request, response, filterChain);

        // Then
        verify(authenticationManager).authenticate(any(BearerTokenAuthenticationToken.class));
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void tokenAuthentication_WithValidOpaqueToken_ShouldAuthenticate() throws ServletException, IOException {
        // Given
        String opaqueToken = "550e8400-e29b-41d4-a716-446655440000"; // UUID format
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + opaqueToken);
        MockHttpServletResponse response = new MockHttpServletResponse();
        
        Authentication expectedAuth = new TestingAuthenticationToken("testuser", null, "ROLE_USER");
        when(authenticationManager.authenticate(any(OpaqueTokenAuthenticationToken.class)))
            .thenReturn(expectedAuth);
        
        AuthenticationFilter filter = filterConfig.createTokenAuthenticationFilter(
            authenticationManager, 
            AnyRequestMatcher.INSTANCE
        );

        // When
        filter.doFilter(request, response, filterChain);

        // Then
        verify(authenticationManager).authenticate(any(OpaqueTokenAuthenticationToken.class));
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void tokenAuthentication_WithTokenFromParameter_ShouldAuthenticate() throws ServletException, IOException {
        // Given
        String jwtToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ0ZXN0dXNlciJ9.signature";
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setParameter("access_token", jwtToken);
        MockHttpServletResponse response = new MockHttpServletResponse();
        
        Authentication expectedAuth = new TestingAuthenticationToken("testuser", null, "ROLE_USER");
        when(authenticationManager.authenticate(any(BearerTokenAuthenticationToken.class)))
            .thenReturn(expectedAuth);
        
        AuthenticationFilter filter = filterConfig.createTokenAuthenticationFilter(
            authenticationManager, 
            AnyRequestMatcher.INSTANCE
        );

        // When
        filter.doFilter(request, response, filterChain);

        // Then
        verify(authenticationManager).authenticate(any(BearerTokenAuthenticationToken.class));
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void tokenAuthentication_WithNoToken_ShouldContinueFilterChain() throws ServletException, IOException {
        // Given
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        
        AuthenticationFilter filter = filterConfig.createTokenAuthenticationFilter(
            authenticationManager, 
            AnyRequestMatcher.INSTANCE
        );

        // When
        filter.doFilter(request, response, filterChain);

        // Then
        verify(authenticationManager, never()).authenticate(any());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void tokenAuthentication_WithInvalidToken_ShouldHandleAuthenticationException() throws ServletException, IOException {
        // Given
        String invalidToken = "invalid-token";
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + invalidToken);
        MockHttpServletResponse response = new MockHttpServletResponse();
        
        when(authenticationManager.authenticate(any()))
            .thenThrow(new AuthenticationException("Invalid token") {});
        
        AuthenticationFilter filter = filterConfig.createTokenAuthenticationFilter(
            authenticationManager, 
            AnyRequestMatcher.INSTANCE
        );

        // When
        filter.doFilter(request, response, filterChain);

        // Then
        verify(authenticationManager).authenticate(any(OpaqueTokenAuthenticationToken.class));
        // 注意：AuthenticationFilter 的默认行为是在认证失败时不继续过滤器链
        // 具体行为取决于配置的失败处理器
    }
}