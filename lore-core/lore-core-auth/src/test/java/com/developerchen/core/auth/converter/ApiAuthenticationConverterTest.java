package com.developerchen.core.auth.converter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * ApiAuthenticationConverter 单元测试
 * 
 * @author syc
 */
class ApiAuthenticationConverterTest {
    
    private ApiAuthenticationConverter converter;
    
    @BeforeEach
    void setUp() {
        converter = new ApiAuthenticationConverter();
    }
    
    @Test
    void convert_ValidJsonLogin_ReturnsAuthentication() {
        // Given
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/login");
        request.setServletPath("/api/login");
        request.setContentType("application/json");
        String jsonBody = "{\"username\":\"testuser\",\"password\":\"testpass\"}";
        request.setContent(jsonBody.getBytes(StandardCharsets.UTF_8));
        
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
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/login");
        request.setServletPath("/login");
        request.setContentType("application/json");
        String jsonBody = "{\"username\":\"testuser\",\"password\":\"testpass\"}";
        request.setContent(jsonBody.getBytes(StandardCharsets.UTF_8));
        
        // When
        Authentication result = converter.convert(request);
        
        // Then
        assertThat(result).isNull();
    }
    
    @Test
    void convert_WrongMethod_ReturnsNull() {
        // Given
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/login");
        request.setServletPath("/api/login");
        request.setContentType("application/json");
        String jsonBody = "{\"username\":\"testuser\",\"password\":\"testpass\"}";
        request.setContent(jsonBody.getBytes(StandardCharsets.UTF_8));
        
        // When
        Authentication result = converter.convert(request);
        
        // Then
        assertThat(result).isNull();
    }
    
    @Test
    void convert_WrongContentType_ReturnsNull() {
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
    void convert_NoContentType_ReturnsNull() {
        // Given
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/login");
        request.setServletPath("/api/login");
        // No content type set
        String jsonBody = "{\"username\":\"testuser\",\"password\":\"testpass\"}";
        request.setContent(jsonBody.getBytes(StandardCharsets.UTF_8));
        
        // When
        Authentication result = converter.convert(request);
        
        // Then
        assertThat(result).isNull();
    }
    
    @Test
    void convert_MissingUsername_ReturnsNull() {
        // Given
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/login");
        request.setServletPath("/api/login");
        request.setContentType("application/json");
        String jsonBody = "{\"password\":\"testpass\"}";
        request.setContent(jsonBody.getBytes(StandardCharsets.UTF_8));
        
        // When
        Authentication result = converter.convert(request);
        
        // Then
        assertThat(result).isNull();
    }
    
    @Test
    void convert_MissingPassword_ReturnsNull() {
        // Given
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/login");
        request.setServletPath("/api/login");
        request.setContentType("application/json");
        String jsonBody = "{\"username\":\"testuser\"}";
        request.setContent(jsonBody.getBytes(StandardCharsets.UTF_8));
        
        // When
        Authentication result = converter.convert(request);
        
        // Then
        assertThat(result).isNull();
    }
    
    @Test
    void convert_EmptyUsername_ReturnsNull() {
        // Given
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/login");
        request.setServletPath("/api/login");
        request.setContentType("application/json");
        String jsonBody = "{\"username\":\"\",\"password\":\"testpass\"}";
        request.setContent(jsonBody.getBytes(StandardCharsets.UTF_8));
        
        // When
        Authentication result = converter.convert(request);
        
        // Then
        assertThat(result).isNull();
    }
    
    @Test
    void convert_EmptyPassword_ReturnsNull() {
        // Given
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/login");
        request.setServletPath("/api/login");
        request.setContentType("application/json");
        String jsonBody = "{\"username\":\"testuser\",\"password\":\"\"}";
        request.setContent(jsonBody.getBytes(StandardCharsets.UTF_8));
        
        // When
        Authentication result = converter.convert(request);
        
        // Then
        assertThat(result).isNull();
    }
    
    @Test
    void convert_NullUsername_ReturnsNull() {
        // Given
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/login");
        request.setServletPath("/api/login");
        request.setContentType("application/json");
        String jsonBody = "{\"username\":null,\"password\":\"testpass\"}";
        request.setContent(jsonBody.getBytes(StandardCharsets.UTF_8));
        
        // When
        Authentication result = converter.convert(request);
        
        // Then
        assertThat(result).isNull();
    }
    
    @Test
    void convert_NullPassword_ReturnsNull() {
        // Given
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/login");
        request.setServletPath("/api/login");
        request.setContentType("application/json");
        String jsonBody = "{\"username\":\"testuser\",\"password\":null}";
        request.setContent(jsonBody.getBytes(StandardCharsets.UTF_8));
        
        // When
        Authentication result = converter.convert(request);
        
        // Then
        assertThat(result).isNull();
    }
    
    @Test
    void convert_InvalidJson_ReturnsNull() {
        // Given
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/login");
        request.setServletPath("/api/login");
        request.setContentType("application/json");
        String jsonBody = "{invalid json}";
        request.setContent(jsonBody.getBytes(StandardCharsets.UTF_8));
        
        // When
        Authentication result = converter.convert(request);
        
        // Then
        assertThat(result).isNull();
    }
    
    @Test
    void convert_EmptyJson_ReturnsNull() {
        // Given
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/login");
        request.setServletPath("/api/login");
        request.setContentType("application/json");
        String jsonBody = "{}";
        request.setContent(jsonBody.getBytes(StandardCharsets.UTF_8));
        
        // When
        Authentication result = converter.convert(request);
        
        // Then
        assertThat(result).isNull();
    }
    
    @Test
    void convert_JsonWithCharsetContentType_ReturnsAuthentication() {
        // Given
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/login");
        request.setServletPath("/api/login");
        request.setContentType("application/json; charset=UTF-8");
        String jsonBody = "{\"username\":\"testuser\",\"password\":\"testpass\"}";
        request.setContent(jsonBody.getBytes(StandardCharsets.UTF_8));
        
        // When
        Authentication result = converter.convert(request);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result).isInstanceOf(UsernamePasswordAuthenticationToken.class);
        assertThat(result.getName()).isEqualTo("testuser");
        assertThat(result.getCredentials()).isEqualTo("testpass");
    }
}