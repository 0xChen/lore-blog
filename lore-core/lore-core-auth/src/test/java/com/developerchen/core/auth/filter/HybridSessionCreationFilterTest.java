package com.developerchen.core.auth.filter;

import com.developerchen.core.auth.config.SessionStrategySelector;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * HybridSessionCreationFilter 单元测试
 * 
 * @author syc
 */
@ExtendWith(MockitoExtension.class)
class HybridSessionCreationFilterTest {

    @Mock
    private SessionStrategySelector sessionStrategySelector;
    
    @Mock
    private HttpServletRequest request;
    
    @Mock
    private HttpServletResponse response;
    
    @Mock
    private FilterChain filterChain;
    
    @Mock
    private HttpSession session;
    
    private HybridSessionCreationFilter filter;
    
    @BeforeEach
    void setUp() {
        filter = new HybridSessionCreationFilter(sessionStrategySelector);
    }
    
    @Test
    void doFilterInternal_WithStatefulStrategy_ShouldAllowNormalProcessing() throws ServletException, IOException {
        // Given
        when(sessionStrategySelector.shouldUseStatelessSession(request)).thenReturn(false);
        when(request.getRequestURI()).thenReturn("/dashboard");
        
        // When
        filter.doFilterInternal(request, response, filterChain);
        
        // Then
        verify(request).setAttribute("SESSION_STRATEGY", "STATEFUL");
        verify(filterChain).doFilter(request, response);
    }
    
    @Test
    void doFilterInternal_WithStatelessStrategy_ShouldWrapRequest() throws ServletException, IOException {
        // Given
        when(sessionStrategySelector.shouldUseStatelessSession(request)).thenReturn(true);
        when(request.getRequestURI()).thenReturn("/api/users");
        
        // When
        filter.doFilterInternal(request, response, filterChain);
        
        // Then
        verify(request).setAttribute("SESSION_STRATEGY", "STATELESS");
        
        ArgumentCaptor<HttpServletRequest> requestCaptor = ArgumentCaptor.forClass(HttpServletRequest.class);
        verify(filterChain).doFilter(requestCaptor.capture(), eq(response));
        
        HttpServletRequest wrappedRequest = requestCaptor.getValue();
        assertThat(wrappedRequest).isNotSameAs(request);
    }
    
    @Test
    void statelessWrapper_GetSession_ShouldReturnNull() throws ServletException, IOException {
        // Given
        when(sessionStrategySelector.shouldUseStatelessSession(request)).thenReturn(true);
        when(request.getRequestURI()).thenReturn("/api/users");
        
        // When
        filter.doFilterInternal(request, response, filterChain);
        
        // Then
        ArgumentCaptor<HttpServletRequest> requestCaptor = ArgumentCaptor.forClass(HttpServletRequest.class);
        verify(filterChain).doFilter(requestCaptor.capture(), eq(response));
        
        HttpServletRequest wrappedRequest = requestCaptor.getValue();
        assertThat(wrappedRequest.getSession()).isNull();
    }
    
    @Test
    void statelessWrapper_GetSessionWithCreate_ShouldReturnNull() throws ServletException, IOException {
        // Given
        when(sessionStrategySelector.shouldUseStatelessSession(request)).thenReturn(true);
        when(request.getRequestURI()).thenReturn("/api/users");
        
        // When
        filter.doFilterInternal(request, response, filterChain);
        
        // Then
        ArgumentCaptor<HttpServletRequest> requestCaptor = ArgumentCaptor.forClass(HttpServletRequest.class);
        verify(filterChain).doFilter(requestCaptor.capture(), eq(response));
        
        HttpServletRequest wrappedRequest = requestCaptor.getValue();
        assertThat(wrappedRequest.getSession(true)).isNull();
    }
    
    @Test
    void statelessWrapper_GetSessionWithoutCreate_ShouldCallOriginal() throws ServletException, IOException {
        // Given
        when(sessionStrategySelector.shouldUseStatelessSession(request)).thenReturn(true);
        when(request.getRequestURI()).thenReturn("/api/users");
        when(request.getSession(false)).thenReturn(session);
        
        // When
        filter.doFilterInternal(request, response, filterChain);
        
        // Then
        ArgumentCaptor<HttpServletRequest> requestCaptor = ArgumentCaptor.forClass(HttpServletRequest.class);
        verify(filterChain).doFilter(requestCaptor.capture(), eq(response));
        
        HttpServletRequest wrappedRequest = requestCaptor.getValue();
        assertThat(wrappedRequest.getSession(false)).isEqualTo(session);
        verify(request).getSession(false);
    }
    
    @Test
    void statelessWrapper_GetRequestedSessionId_ShouldReturnNull() throws ServletException, IOException {
        // Given
        when(sessionStrategySelector.shouldUseStatelessSession(request)).thenReturn(true);
        when(request.getRequestURI()).thenReturn("/api/users");
        
        // When
        filter.doFilterInternal(request, response, filterChain);
        
        // Then
        ArgumentCaptor<HttpServletRequest> requestCaptor = ArgumentCaptor.forClass(HttpServletRequest.class);
        verify(filterChain).doFilter(requestCaptor.capture(), eq(response));
        
        HttpServletRequest wrappedRequest = requestCaptor.getValue();
        assertThat(wrappedRequest.getRequestedSessionId()).isNull();
    }
    
    @Test
    void statelessWrapper_IsRequestedSessionIdValid_ShouldReturnFalse() throws ServletException, IOException {
        // Given
        when(sessionStrategySelector.shouldUseStatelessSession(request)).thenReturn(true);
        when(request.getRequestURI()).thenReturn("/api/users");
        
        // When
        filter.doFilterInternal(request, response, filterChain);
        
        // Then
        ArgumentCaptor<HttpServletRequest> requestCaptor = ArgumentCaptor.forClass(HttpServletRequest.class);
        verify(filterChain).doFilter(requestCaptor.capture(), eq(response));
        
        HttpServletRequest wrappedRequest = requestCaptor.getValue();
        assertThat(wrappedRequest.isRequestedSessionIdValid()).isFalse();
    }
    
    @Test
    void statelessWrapper_IsRequestedSessionIdFromCookie_ShouldReturnFalse() throws ServletException, IOException {
        // Given
        when(sessionStrategySelector.shouldUseStatelessSession(request)).thenReturn(true);
        when(request.getRequestURI()).thenReturn("/api/users");
        
        // When
        filter.doFilterInternal(request, response, filterChain);
        
        // Then
        ArgumentCaptor<HttpServletRequest> requestCaptor = ArgumentCaptor.forClass(HttpServletRequest.class);
        verify(filterChain).doFilter(requestCaptor.capture(), eq(response));
        
        HttpServletRequest wrappedRequest = requestCaptor.getValue();
        assertThat(wrappedRequest.isRequestedSessionIdFromCookie()).isFalse();
    }
    
    @Test
    void statelessWrapper_IsRequestedSessionIdFromURL_ShouldReturnFalse() throws ServletException, IOException {
        // Given
        when(sessionStrategySelector.shouldUseStatelessSession(request)).thenReturn(true);
        when(request.getRequestURI()).thenReturn("/api/users");
        
        // When
        filter.doFilterInternal(request, response, filterChain);
        
        // Then
        ArgumentCaptor<HttpServletRequest> requestCaptor = ArgumentCaptor.forClass(HttpServletRequest.class);
        verify(filterChain).doFilter(requestCaptor.capture(), eq(response));
        
        HttpServletRequest wrappedRequest = requestCaptor.getValue();
        assertThat(wrappedRequest.isRequestedSessionIdFromURL()).isFalse();
    }
}