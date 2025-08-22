package com.developerchen.core.auth.handler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * FormAuthenticationFailureHandler 单元测试
 * 
 * @author syc
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class FormAuthenticationFailureHandlerTest {

    private FormAuthenticationFailureHandler handler;
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        handler = new FormAuthenticationFailureHandler();
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
    }

    @Test
    void shouldRedirectToDefaultFailureUrl() throws Exception {
        // Given
        AuthenticationException exception = new BadCredentialsException("Bad credentials");

        // When
        handler.onAuthenticationFailure(request, response, exception);

        // Then
        assertThat(response.getStatus()).isEqualTo(MockHttpServletResponse.SC_FOUND);
        assertThat(response.getRedirectedUrl()).isEqualTo("/login?error");
    }

    @Test
    void shouldStoreExceptionInSession() throws Exception {
        // Given
        AuthenticationException exception = new BadCredentialsException("Bad credentials");

        // When
        handler.onAuthenticationFailure(request, response, exception);

        // Then
        Object storedException = request.getSession().getAttribute("SPRING_SECURITY_LAST_EXCEPTION");
        assertThat(storedException).isEqualTo(exception);
    }

    @Test
    void shouldHandleDifferentExceptionTypes() throws Exception {
        // Given
        AuthenticationException exception = new BadCredentialsException("Custom error message");

        // When
        handler.onAuthenticationFailure(request, response, exception);

        // Then
        assertThat(response.getRedirectedUrl()).isEqualTo("/login?error");
        
        Object storedException = request.getSession().getAttribute("SPRING_SECURITY_LAST_EXCEPTION");
        assertThat(storedException).isInstanceOf(BadCredentialsException.class);
        assertThat(((AuthenticationException) storedException).getMessage()).isEqualTo("Custom error message");
    }

    @Test
    void shouldCreateSessionIfNotExists() throws Exception {
        // Given
        AuthenticationException exception = new BadCredentialsException("Bad credentials");
        // 确保没有现有会话
        assertThat(request.getSession(false)).isNull();

        // When
        handler.onAuthenticationFailure(request, response, exception);

        // Then
        // 应该创建新会话
        assertThat(request.getSession(false)).isNotNull();
        assertThat(request.getSession().getAttribute("SPRING_SECURITY_LAST_EXCEPTION")).isEqualTo(exception);
    }
}