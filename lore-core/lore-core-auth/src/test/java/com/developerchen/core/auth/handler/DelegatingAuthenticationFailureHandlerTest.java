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
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;

import static org.mockito.Mockito.*;

/**
 * DelegatingAuthenticationFailureHandler 单元测试
 * 
 * @author syc
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class DelegatingAuthenticationFailureHandlerTest {

    @Mock
    private ApiAuthenticationFailureHandler apiFailureHandler;
    
    @Mock
    private FormAuthenticationFailureHandler formFailureHandler;
    
    @Mock
    private HttpServletRequest request;
    
    @Mock
    private HttpServletResponse response;
    
    private DelegatingAuthenticationFailureHandler handler;
    private AuthenticationException exception;

    @BeforeEach
    void setUp() {
        handler = new DelegatingAuthenticationFailureHandler(apiFailureHandler, formFailureHandler);
        exception = new BadCredentialsException("Bad credentials");
    }

    @Test
    void shouldDelegateToApiHandlerForApiPath() throws Exception {
        // Given
        when(request.getRequestURI()).thenReturn("/api/login");

        // When
        handler.onAuthenticationFailure(request, response, exception);

        // Then
        verify(apiFailureHandler).onAuthenticationFailure(request, response, exception);
        verify(formFailureHandler, never()).onAuthenticationFailure(any(), any(), any());
    }

    @Test
    void shouldDelegateToApiHandlerForJsonAcceptHeader() throws Exception {
        // Given
        when(request.getRequestURI()).thenReturn("/login");
        when(request.getHeader("Accept")).thenReturn(MediaType.APPLICATION_JSON_VALUE);

        // When
        handler.onAuthenticationFailure(request, response, exception);

        // Then
        verify(apiFailureHandler).onAuthenticationFailure(request, response, exception);
        verify(formFailureHandler, never()).onAuthenticationFailure(any(), any(), any());
    }

    @Test
    void shouldDelegateToApiHandlerForJsonContentType() throws Exception {
        // Given
        when(request.getRequestURI()).thenReturn("/login");
        when(request.getContentType()).thenReturn(MediaType.APPLICATION_JSON_VALUE);

        // When
        handler.onAuthenticationFailure(request, response, exception);

        // Then
        verify(apiFailureHandler).onAuthenticationFailure(request, response, exception);
        verify(formFailureHandler, never()).onAuthenticationFailure(any(), any(), any());
    }

    @Test
    void shouldDelegateToApiHandlerForAjaxRequest() throws Exception {
        // Given
        when(request.getRequestURI()).thenReturn("/login");
        when(request.getHeader("X-Requested-With")).thenReturn("XMLHttpRequest");

        // When
        handler.onAuthenticationFailure(request, response, exception);

        // Then
        verify(apiFailureHandler).onAuthenticationFailure(request, response, exception);
        verify(formFailureHandler, never()).onAuthenticationFailure(any(), any(), any());
    }

    @Test
    void shouldDelegateToFormHandlerForRegularFormRequest() throws Exception {
        // Given
        when(request.getRequestURI()).thenReturn("/login");
        when(request.getHeader("Accept")).thenReturn("text/html,application/xhtml+xml");
        when(request.getContentType()).thenReturn("application/x-www-form-urlencoded");

        // When
        handler.onAuthenticationFailure(request, response, exception);

        // Then
        verify(formFailureHandler).onAuthenticationFailure(request, response, exception);
        verify(apiFailureHandler, never()).onAuthenticationFailure(any(), any(), any());
    }

    @Test
    void shouldDelegateToFormHandlerWhenNoSpecialHeaders() throws Exception {
        // Given
        when(request.getRequestURI()).thenReturn("/login");

        // When
        handler.onAuthenticationFailure(request, response, exception);

        // Then
        verify(formFailureHandler).onAuthenticationFailure(request, response, exception);
        verify(apiFailureHandler, never()).onAuthenticationFailure(any(), any(), any());
    }

    @Test
    void shouldHandlePartialJsonContentType() throws Exception {
        // Given
        when(request.getRequestURI()).thenReturn("/login");
        when(request.getContentType()).thenReturn("application/json; charset=UTF-8");

        // When
        handler.onAuthenticationFailure(request, response, exception);

        // Then
        verify(apiFailureHandler).onAuthenticationFailure(request, response, exception);
        verify(formFailureHandler, never()).onAuthenticationFailure(any(), any(), any());
    }

    @Test
    void shouldHandlePartialJsonAcceptHeader() throws Exception {
        // Given
        when(request.getRequestURI()).thenReturn("/login");
        when(request.getHeader("Accept")).thenReturn("text/html,application/json,*/*");

        // When
        handler.onAuthenticationFailure(request, response, exception);

        // Then
        verify(apiFailureHandler).onAuthenticationFailure(request, response, exception);
        verify(formFailureHandler, never()).onAuthenticationFailure(any(), any(), any());
    }
}