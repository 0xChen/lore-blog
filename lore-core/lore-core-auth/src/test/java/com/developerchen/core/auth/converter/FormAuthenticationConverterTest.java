package com.developerchen.core.auth.converter;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * FormAuthenticationConverter 单元测试
 * 
 * @author syc
 */
class FormAuthenticationConverterTest {
    
    private FormAuthenticationConverter converter;
    
    @BeforeEach
    void setUp() {
        converter = new FormAuthenticationConverter();
    }
    
    @Test
    void convert_ValidFormLogin_ReturnsAuthentication() {
        // Given
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/login");
        request.setServletPath("/login");
        request.setContentType("application/x-www-form-urlencoded");
        request.setParameter("username", "testuser");
        request.setParameter("password", "testpass");
        
        // When
        Authentication result = converter.convert(request);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result).isInstanceOf(UsernamePasswordAuthenticationToken.class);
        assertThat(result.getName()).isEqualTo("testuser");
        assertThat(result.getCredentials()).isEqualTo("testpass");
        assertThat(result.isAuthenticated()).isFalse();
    }
    
    @Test
    void convert_WrongPath_ReturnsNull() {
        // Given
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/login");
        request.setServletPath("/api/login");
        request.setContentType("application/x-www-form-urlencoded");
        request.setParameter("username", "testuser");
        request.setParameter("password", "testpass");
        
        // When
        Authentication result = converter.convert(request);
        
        // Then
        assertThat(result).isNull();
    }
    
    @Test
    void convert_WrongMethod_ReturnsNull() {
        // Given
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/login");
        request.setServletPath("/login");
        request.setContentType("application/x-www-form-urlencoded");
        request.setParameter("username", "testuser");
        request.setParameter("password", "testpass");
        
        // When
        Authentication result = converter.convert(request);
        
        // Then
        assertThat(result).isNull();
    }
    
    @Test
    void convert_JsonContentType_ReturnsNull() {
        // Given
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/login");
        request.setServletPath("/login");
        request.setContentType("application/json");
        request.setParameter("username", "testuser");
        request.setParameter("password", "testpass");
        
        // When
        Authentication result = converter.convert(request);
        
        // Then
        assertThat(result).isNull();
    }
    
    @Test
    void convert_MissingUsername_ReturnsNull() {
        // Given
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/login");
        request.setServletPath("/login");
        request.setContentType("application/x-www-form-urlencoded");
        request.setParameter("password", "testpass");
        
        // When
        Authentication result = converter.convert(request);
        
        // Then
        assertThat(result).isNull();
    }
    
    @Test
    void convert_MissingPassword_ReturnsNull() {
        // Given
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/login");
        request.setServletPath("/login");
        request.setContentType("application/x-www-form-urlencoded");
        request.setParameter("username", "testuser");
        
        // When
        Authentication result = converter.convert(request);
        
        // Then
        assertThat(result).isNull();
    }
    
    @Test
    void convert_EmptyUsername_ReturnsNull() {
        // Given
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/login");
        request.setServletPath("/login");
        request.setContentType("application/x-www-form-urlencoded");
        request.setParameter("username", "");
        request.setParameter("password", "testpass");
        
        // When
        Authentication result = converter.convert(request);
        
        // Then
        assertThat(result).isNull();
    }
    
    @Test
    void convert_EmptyPassword_ReturnsNull() {
        // Given
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/login");
        request.setServletPath("/login");
        request.setContentType("application/x-www-form-urlencoded");
        request.setParameter("username", "testuser");
        request.setParameter("password", "");
        
        // When
        Authentication result = converter.convert(request);
        
        // Then
        assertThat(result).isNull();
    }
    
    @Test
    void convert_NoContentType_ReturnsAuthentication() {
        // Given
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/login");
        request.setServletPath("/login");
        // No content type set - should still work for form submissions
        request.setParameter("username", "testuser");
        request.setParameter("password", "testpass");
        
        // When
        Authentication result = converter.convert(request);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result).isInstanceOf(UsernamePasswordAuthenticationToken.class);
        assertThat(result.getName()).isEqualTo("testuser");
        assertThat(result.getCredentials()).isEqualTo("testpass");
    }
}