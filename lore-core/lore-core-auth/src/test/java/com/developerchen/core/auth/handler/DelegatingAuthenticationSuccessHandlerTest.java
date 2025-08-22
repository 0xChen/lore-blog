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
import org.springframework.security.core.Authentication;

import static org.mockito.Mockito.*;

/**
 * DelegatingAuthenticationSuccessHandler 集成测试
 * 
 * @author syc
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class DelegatingAuthenticationSuccessHandlerTest {

    @Mock
    private ApiAuthenticationSuccessHandler apiSuccessHandler;
    
    @Mock
    private FormAuthenticationSuccessHandler formSuccessHandler;
    
    @Mock
    private HttpServletRequest request;
    
    @Mock
    private HttpServletResponse response;
    
    @Mock
    private Authentication authentication;
    
    private DelegatingAuthenticationSuccessHandler handler;

    @BeforeEach
    void setUp() {
        handler = new DelegatingAuthenticationSuccessHandler(apiSuccessHandler, formSuccessHandler);
        when(authentication.getName()).thenReturn("testuser");
    }

    @Test
    void shouldDelegateToApiHandlerForApiPath() throws Exception {
        // Given
        when(request.getRequestURI()).thenReturn("/api/login");

        // When
        handler.onAuthenticationSuccess(request, response, authentication);

        // Then
        verify(apiSuccessHandler).onAuthenticationSuccess(request, response, authentication);
        verify(formSuccessHandler, never()).onAuthenticationSuccess(any(), any(), any());
    }

    @Test
    void shouldDelegateToApiHandlerForJsonAcceptHeader() throws Exception {
        // Given
        when(request.getRequestURI()).thenReturn("/login");
        when(request.getHeader("Accept")).thenReturn(MediaType.APPLICATION_JSON_VALUE);

        // When
        handler.onAuthenticationSuccess(request, response, authentication);

        // Then
        verify(apiSuccessHandler).onAuthenticationSuccess(request, response, authentication);
        verify(formSuccessHandler, never()).onAuthenticationSuccess(any(), any(), any());
    }

    @Test
    void shouldDelegateToApiHandlerForJsonContentType() throws Exception {
        // Given
        when(request.getRequestURI()).thenReturn("/login");
        when(request.getContentType()).thenReturn(MediaType.APPLICATION_JSON_VALUE);

        // When
        handler.onAuthenticationSuccess(request, response, authentication);

        // Then
        verify(apiSuccessHandler).onAuthenticationSuccess(request, response, authentication);
        verify(formSuccessHandler, never()).onAuthenticationSuccess(any(), any(), any());
    }

    @Test
    void shouldDelegateToApiHandlerForAjaxRequest() throws Exception {
        // Given
        when(request.getRequestURI()).thenReturn("/login");
        when(request.getHeader("X-Requested-With")).thenReturn("XMLHttpRequest");

        // When
        handler.onAuthenticationSuccess(request, response, authentication);

        // Then
        verify(apiSuccessHandler).onAuthenticationSuccess(request, response, authentication);
        verify(formSuccessHandler, never()).onAuthenticationSuccess(any(), any(), any());
    }

    @Test
    void shouldDelegateToFormHandlerForRegularFormRequest() throws Exception {
        // Given
        when(request.getRequestURI()).thenReturn("/login");
        when(request.getHeader("Accept")).thenReturn("text/html,application/xhtml+xml");
        when(request.getContentType()).thenReturn("application/x-www-form-urlencoded");

        // When
        handler.onAuthenticationSuccess(request, response, authentication);

        // Then
        verify(formSuccessHandler).onAuthenticationSuccess(request, response, authentication);
        verify(apiSuccessHandler, never()).onAuthenticationSuccess(any(), any(), any());
    }

    @Test
    void shouldDelegateToFormHandlerWhenNoSpecialHeaders() throws Exception {
        // Given
        when(request.getRequestURI()).thenReturn("/login");

        // When
        handler.onAuthenticationSuccess(request, response, authentication);

        // Then
        verify(formSuccessHandler).onAuthenticationSuccess(request, response, authentication);
        verify(apiSuccessHandler, never()).onAuthenticationSuccess(any(), any(), any());
    }

    @Test
    void shouldHandlePartialJsonContentType() throws Exception {
        // Given
        when(request.getRequestURI()).thenReturn("/login");
        when(request.getContentType()).thenReturn("application/json; charset=UTF-8");

        // When
        handler.onAuthenticationSuccess(request, response, authentication);

        // Then
        verify(apiSuccessHandler).onAuthenticationSuccess(request, response, authentication);
        verify(formSuccessHandler, never()).onAuthenticationSuccess(any(), any(), any());
    }

    @Test
    void shouldHandlePartialJsonAcceptHeader() throws Exception {
        // Given
        when(request.getRequestURI()).thenReturn("/login");
        when(request.getHeader("Accept")).thenReturn("text/html,application/json,*/*");

        // When
        handler.onAuthenticationSuccess(request, response, authentication);

        // Then
        verify(apiSuccessHandler).onAuthenticationSuccess(request, response, authentication);
        verify(formSuccessHandler, never()).onAuthenticationSuccess(any(), any(), any());
    }

    @Test
    void shouldPrioritizeApiPathOverHeaders() throws Exception {
        // Given - API 路径应该优先于其他头信息
        when(request.getRequestURI()).thenReturn("/api/auth/login");
        when(request.getHeader("Accept")).thenReturn("text/html");
        when(request.getContentType()).thenReturn("application/x-www-form-urlencoded");

        // When
        handler.onAuthenticationSuccess(request, response, authentication);

        // Then
        verify(apiSuccessHandler).onAuthenticationSuccess(request, response, authentication);
        verify(formSuccessHandler, never()).onAuthenticationSuccess(any(), any(), any());
    }

    @Test
    void shouldHandleNestedApiPaths() throws Exception {
        // Given
        when(request.getRequestURI()).thenReturn("/api/v1/auth/login");

        // When
        handler.onAuthenticationSuccess(request, response, authentication);

        // Then
        verify(apiSuccessHandler).onAuthenticationSuccess(request, response, authentication);
        verify(formSuccessHandler, never()).onAuthenticationSuccess(any(), any(), any());
    }
}