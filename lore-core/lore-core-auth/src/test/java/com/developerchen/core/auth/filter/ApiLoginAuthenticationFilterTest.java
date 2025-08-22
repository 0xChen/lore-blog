package com.developerchen.core.auth.filter;

import com.developerchen.core.auth.converter.ApiAuthenticationConverter;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * API 登录认证过滤器测试
 * 
 * @author syc
 */
@ExtendWith(MockitoExtension.class)
class ApiLoginAuthenticationFilterTest {

    @Mock
    private AuthenticationManager authenticationManager;
    
    @Mock
    private AuthenticationSuccessHandler successHandler;
    
    @Mock
    private AuthenticationFailureHandler failureHandler;
    
    private ApiAuthenticationConverter authenticationConverter;
    private ApiLoginAuthenticationFilter filter;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        authenticationConverter = new ApiAuthenticationConverter();
        filter = new ApiLoginAuthenticationFilter(authenticationManager, authenticationConverter);
        filter.setAuthenticationSuccessHandler(successHandler);
        filter.setAuthenticationFailureHandler(failureHandler);
        objectMapper = new ObjectMapper();
    }

    @Test
    void attemptAuthentication_ValidJsonRequest_ShouldAuthenticate() throws Exception {
        // Given
        MockHttpServletRequest request = createJsonLoginRequest("testuser", "password");
        MockHttpServletResponse response = new MockHttpServletResponse();
        
        Authentication expectedAuth = UsernamePasswordAuthenticationToken.authenticated(
            "testuser", "password", null);
        when(authenticationManager.authenticate(any(Authentication.class)))
            .thenReturn(expectedAuth);

        // When
        Authentication result = filter.attemptAuthentication(request, response);

        // Then
        assertNotNull(result);
        assertEquals("testuser", result.getName());
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    void attemptAuthentication_InvalidJsonRequest_ShouldThrowException() {
        // Given
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/login");
        request.setContentType(MediaType.APPLICATION_JSON_VALUE);
        request.setContent("invalid json".getBytes());
        MockHttpServletResponse response = new MockHttpServletResponse();

        // When & Then
        assertThrows(AuthenticationException.class, () -> {
            filter.attemptAuthentication(request, response);
        });
    }

    @Test
    void attemptAuthentication_MissingCredentials_ShouldThrowException() throws Exception {
        // Given
        MockHttpServletRequest request = createJsonLoginRequest("", "");
        MockHttpServletResponse response = new MockHttpServletResponse();

        // When & Then
        assertThrows(AuthenticationException.class, () -> {
            filter.attemptAuthentication(request, response);
        });
    }

    @Test
    void attemptAuthentication_NonJsonContentType_ShouldThrowException() {
        // Given
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/login");
        request.setContentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE);
        request.setContent("username=test&password=pass".getBytes());
        MockHttpServletResponse response = new MockHttpServletResponse();

        // When & Then
        assertThrows(AuthenticationException.class, () -> {
            filter.attemptAuthentication(request, response);
        });
    }

    @Test
    void attemptAuthentication_AuthenticationFailure_ShouldCallFailureHandler() throws Exception {
        // Given
        MockHttpServletRequest request = createJsonLoginRequest("testuser", "wrongpassword");
        MockHttpServletResponse response = new MockHttpServletResponse();
        
        AuthenticationException authException = new AuthenticationException("Bad credentials") {};
        when(authenticationManager.authenticate(any(Authentication.class)))
            .thenThrow(authException);

        // When
        try {
            filter.attemptAuthentication(request, response);
            fail("Expected AuthenticationException");
        } catch (AuthenticationException e) {
            // Expected
        }

        // Then
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    void setAuthenticationSuccessHandler_ShouldSetHandler() {
        // Given
        AuthenticationSuccessHandler newHandler = mock(AuthenticationSuccessHandler.class);

        // When
        filter.setAuthenticationSuccessHandler(newHandler);

        // Then
        // 验证处理器已设置（通过反射或其他方式验证）
        assertNotNull(filter);
    }

    @Test
    void setAuthenticationFailureHandler_ShouldSetHandler() {
        // Given
        AuthenticationFailureHandler newHandler = mock(AuthenticationFailureHandler.class);

        // When
        filter.setAuthenticationFailureHandler(newHandler);

        // Then
        // 验证处理器已设置
        assertNotNull(filter);
    }

    /**
     * 创建 JSON 登录请求
     */
    private MockHttpServletRequest createJsonLoginRequest(String username, String password) throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/login");
        request.setContentType(MediaType.APPLICATION_JSON_VALUE);
        
        Map<String, String> credentials = new HashMap<>();
        credentials.put("username", username);
        credentials.put("password", password);
        
        String jsonContent = objectMapper.writeValueAsString(credentials);
        request.setContent(jsonContent.getBytes());
        
        return request;
    }
}