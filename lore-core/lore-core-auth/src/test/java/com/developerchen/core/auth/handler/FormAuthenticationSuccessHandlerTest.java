package com.developerchen.core.auth.handler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.savedrequest.DefaultSavedRequest;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.RequestCache;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * FormAuthenticationSuccessHandler 集成测试
 * 
 * @author syc
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class FormAuthenticationSuccessHandlerTest {

    @Mock
    private Authentication authentication;
    
    private FormAuthenticationSuccessHandler handler;
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        handler = new FormAuthenticationSuccessHandler();
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        
        when(authentication.getName()).thenReturn("testuser");
    }

    @Test
    void shouldRedirectToDefaultTargetUrlWhenNoSavedRequest() throws Exception {
        // When
        handler.onAuthenticationSuccess(request, response, authentication);

        // Then
        assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_FOUND);
        assertThat(response.getRedirectedUrl()).isEqualTo("/");
    }

    @Test
    void shouldRedirectToSavedRequestUrl() throws Exception {
        // Given - 模拟保存的请求
        RequestCache requestCache = new HttpSessionRequestCache();
        MockHttpServletRequest originalRequest = new MockHttpServletRequest();
        originalRequest.setRequestURI("/protected/resource");
        originalRequest.setQueryString("param=value");
        originalRequest.setServerName("localhost");
        originalRequest.setServerPort(8080);
        originalRequest.setScheme("http");
        
        // 使用 RequestCache 来保存请求，这样会正确设置所有必需的属性
        requestCache.saveRequest(request, response);
        request.getSession().setAttribute("SPRING_SECURITY_SAVED_REQUEST", 
            new DefaultSavedRequest.Builder()
                .setScheme("http")
                .setServerName("localhost")
                .setServerPort(8080)
                .setRequestURI("/protected/resource")
                .setQueryString("param=value")
                .setMethod("GET")
                .build());
        
        // When
        handler.onAuthenticationSuccess(request, response, authentication);

        // Then
        assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_FOUND);
        assertThat(response.getRedirectedUrl()).contains("/protected/resource");
    }

    @Test
    void shouldRedirectToTargetUrlParameter() throws Exception {
        // Given
        request.setParameter("redirectTo", "/dashboard");

        // When
        handler.onAuthenticationSuccess(request, response, authentication);

        // Then
        assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_FOUND);
        assertThat(response.getRedirectedUrl()).isEqualTo("/dashboard");
    }

    @Test
    void shouldUseDefaultTargetUrlWhenTargetUrlParameterIsEmpty() throws Exception {
        // Given
        request.setParameter("redirectTo", "");

        // When
        handler.onAuthenticationSuccess(request, response, authentication);

        // Then
        assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_FOUND);
        assertThat(response.getRedirectedUrl()).isEqualTo("/");
    }

    @Test
    void shouldHandleRelativeUrls() throws Exception {
        // Given
        request.setParameter("redirectTo", "profile");

        // When
        handler.onAuthenticationSuccess(request, response, authentication);

        // Then
        assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_FOUND);
        assertThat(response.getRedirectedUrl()).isEqualTo("profile");
    }

    @Test
    void shouldHandleAbsoluteUrls() throws Exception {
        // Given - Spring Security 默认允许绝对URL，但在生产环境中应该配置URL验证器
        request.setParameter("redirectTo", "http://evil.com/malicious");

        // When
        handler.onAuthenticationSuccess(request, response, authentication);

        // Then - 默认行为是允许重定向（在实际配置中应该添加URL验证）
        assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_FOUND);
        // 注意：在生产环境中应该配置 TargetUrlResolver 来验证URL安全性
        assertThat(response.getRedirectedUrl()).isEqualTo("http://evil.com/malicious");
    }
}