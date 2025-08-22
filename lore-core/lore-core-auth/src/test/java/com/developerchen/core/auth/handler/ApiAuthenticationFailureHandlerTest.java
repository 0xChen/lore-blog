package com.developerchen.core.auth.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.io.PrintWriter;
import java.io.StringWriter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * ApiAuthenticationFailureHandler 单元测试
 * 
 * @author syc
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ApiAuthenticationFailureHandlerTest {

    @Mock
    private HttpServletRequest request;
    
    @Mock
    private HttpServletResponse response;
    
    private ObjectMapper objectMapper;
    private ApiAuthenticationFailureHandler handler;
    private StringWriter responseWriter;

    @BeforeEach
    void setUp() throws Exception {
        objectMapper = new ObjectMapper();
        handler = new ApiAuthenticationFailureHandler(objectMapper);
        
        responseWriter = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(responseWriter));
    }

    @Test
    void shouldReturnUnauthorizedStatusForBadCredentials() throws Exception {
        // Given
        AuthenticationException exception = new BadCredentialsException("Bad credentials");

        // When
        handler.onAuthenticationFailure(request, response, exception);

        // Then
        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(response).setContentType(MediaType.APPLICATION_JSON_VALUE);
        verify(response).setCharacterEncoding("UTF-8");
        
        String responseJson = responseWriter.toString();
        assertThat(responseJson).contains("\"error\":\"unauthorized\"");
        assertThat(responseJson).contains("\"error_description\":\"用户名或密码错误\"");
        assertThat(responseJson).contains("timestamp");
    }

    @Test
    void shouldHandleUsernameNotFoundException() throws Exception {
        // Given
        AuthenticationException exception = new UsernameNotFoundException("User not found");

        // When
        handler.onAuthenticationFailure(request, response, exception);

        // Then
        String responseJson = responseWriter.toString();
        assertThat(responseJson).contains("\"error_description\":\"用户不存在\"");
    }

    @Test
    void shouldHandleDisabledException() throws Exception {
        // Given
        AuthenticationException exception = new DisabledException("Account disabled");

        // When
        handler.onAuthenticationFailure(request, response, exception);

        // Then
        String responseJson = responseWriter.toString();
        assertThat(responseJson).contains("\"error_description\":\"账户已被禁用\"");
    }

    @Test
    void shouldHandleGenericAuthenticationException() throws Exception {
        // Given
        AuthenticationException exception = new AuthenticationException("Generic error") {};

        // When
        handler.onAuthenticationFailure(request, response, exception);

        // Then
        String responseJson = responseWriter.toString();
        assertThat(responseJson).contains("\"error_description\":\"认证失败：Generic error\"");
    }

    @Test
    void shouldIncludeTimestampInResponse() throws Exception {
        // Given
        AuthenticationException exception = new BadCredentialsException("Bad credentials");

        // When
        handler.onAuthenticationFailure(request, response, exception);

        // Then
        String responseJson = responseWriter.toString();
        assertThat(responseJson).contains("timestamp");
        assertThat(responseJson).matches(".*\"timestamp\":\"\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.*");
    }
}