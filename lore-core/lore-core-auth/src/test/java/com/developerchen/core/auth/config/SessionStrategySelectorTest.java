package com.developerchen.core.auth.config;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

/**
 * SessionStrategySelector 单元测试
 * 
 * @author syc
 */
@ExtendWith(MockitoExtension.class)
class SessionStrategySelectorTest {

    @Mock
    private HttpServletRequest request;
    
    private SessionStrategySelector sessionStrategySelector;
    
    @BeforeEach
    void setUp() {
        sessionStrategySelector = new SessionStrategySelector();
    }
    
    @Test
    void shouldUseStatelessSession_ForApiRequest_ShouldReturnTrue() {
        // Given
        when(request.getRequestURI()).thenReturn("/api/users");
        
        // When
        boolean result = sessionStrategySelector.shouldUseStatelessSession(request);
        
        // Then
        assertThat(result).isTrue();
    }
    
    @Test
    void shouldUseStatelessSession_ForApiLoginRequest_ShouldReturnTrue() {
        // Given
        when(request.getRequestURI()).thenReturn("/api/login");
        
        // When
        boolean result = sessionStrategySelector.shouldUseStatelessSession(request);
        
        // Then
        assertThat(result).isTrue();
    }
    
    @Test
    void shouldUseStatelessSession_WithBearerToken_ShouldReturnTrue() {
        // Given
        when(request.getRequestURI()).thenReturn("/dashboard");
        when(request.getHeader("Authorization")).thenReturn("Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...");
        
        // When
        boolean result = sessionStrategySelector.shouldUseStatelessSession(request);
        
        // Then
        assertThat(result).isTrue();
    }
    
    @Test
    void shouldUseStatelessSession_WithStatelessHeader_ShouldReturnTrue() {
        // Given
        lenient().when(request.getRequestURI()).thenReturn("/dashboard");
        lenient().when(request.getHeader("Authorization")).thenReturn(null);
        when(request.getHeader("X-Session-Strategy")).thenReturn("stateless");
        
        // When
        boolean result = sessionStrategySelector.shouldUseStatelessSession(request);
        
        // Then
        assertThat(result).isTrue();
    }
    
    @Test
    void shouldUseStatelessSession_WithStatefulHeader_ShouldReturnFalse() {
        // Given
        lenient().when(request.getRequestURI()).thenReturn("/dashboard");
        lenient().when(request.getHeader("Authorization")).thenReturn(null);
        when(request.getHeader("X-Session-Strategy")).thenReturn("stateful");
        
        // When
        boolean result = sessionStrategySelector.shouldUseStatelessSession(request);
        
        // Then
        assertThat(result).isFalse();
    }
    
    @Test
    void shouldUseStatelessSession_ForRegularWebRequest_ShouldReturnFalse() {
        // Given
        when(request.getRequestURI()).thenReturn("/dashboard");
        
        // When
        boolean result = sessionStrategySelector.shouldUseStatelessSession(request);
        
        // Then
        assertThat(result).isFalse();
    }
    
    @Test
    void shouldUseStatelessSession_ForLoginPage_ShouldReturnFalse() {
        // Given
        when(request.getRequestURI()).thenReturn("/login");
        
        // When
        boolean result = sessionStrategySelector.shouldUseStatelessSession(request);
        
        // Then
        assertThat(result).isFalse();
    }
    
    @Test
    void shouldUseStatelessSession_WithInvalidAuthorizationHeader_ShouldReturnFalse() {
        // Given
        when(request.getRequestURI()).thenReturn("/dashboard");
        when(request.getHeader("Authorization")).thenReturn("Basic dXNlcjpwYXNzd29yZA==");
        
        // When
        boolean result = sessionStrategySelector.shouldUseStatelessSession(request);
        
        // Then
        assertThat(result).isFalse();
    }
    
    @Test
    void isJsonRequest_WithJsonContentType_ShouldReturnTrue() {
        // Given
        when(request.getContentType()).thenReturn("application/json");
        
        // When
        boolean result = sessionStrategySelector.isJsonRequest(request);
        
        // Then
        assertThat(result).isTrue();
    }
    
    @Test
    void isJsonRequest_WithJsonCharsetContentType_ShouldReturnTrue() {
        // Given
        when(request.getContentType()).thenReturn("application/json; charset=UTF-8");
        
        // When
        boolean result = sessionStrategySelector.isJsonRequest(request);
        
        // Then
        assertThat(result).isTrue();
    }
    
    @Test
    void isJsonRequest_WithFormContentType_ShouldReturnFalse() {
        // Given
        when(request.getContentType()).thenReturn("application/x-www-form-urlencoded");
        
        // When
        boolean result = sessionStrategySelector.isJsonRequest(request);
        
        // Then
        assertThat(result).isFalse();
    }
    
    @Test
    void isFormRequest_WithFormContentType_ShouldReturnTrue() {
        // Given
        when(request.getContentType()).thenReturn("application/x-www-form-urlencoded");
        
        // When
        boolean result = sessionStrategySelector.isFormRequest(request);
        
        // Then
        assertThat(result).isTrue();
    }
    
    @Test
    void isFormRequest_WithFormCharsetContentType_ShouldReturnTrue() {
        // Given
        when(request.getContentType()).thenReturn("application/x-www-form-urlencoded; charset=UTF-8");
        
        // When
        boolean result = sessionStrategySelector.isFormRequest(request);
        
        // Then
        assertThat(result).isTrue();
    }
    
    @Test
    void isFormRequest_WithJsonContentType_ShouldReturnFalse() {
        // Given
        when(request.getContentType()).thenReturn("application/json");
        
        // When
        boolean result = sessionStrategySelector.isFormRequest(request);
        
        // Then
        assertThat(result).isFalse();
    }
}