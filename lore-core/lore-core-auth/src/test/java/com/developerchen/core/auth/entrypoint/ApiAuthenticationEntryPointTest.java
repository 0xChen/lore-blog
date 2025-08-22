package com.developerchen.core.auth.entrypoint;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;

import java.io.IOException;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ApiAuthenticationEntryPointTest {

    private ApiAuthenticationEntryPoint entryPoint;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        entryPoint = new ApiAuthenticationEntryPoint(objectMapper);
    }

    @Test
    void shouldReturnJsonErrorResponseWithCorrectStatus() throws IOException, ServletException {
        // Given
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/protected");
        MockHttpServletResponse response = new MockHttpServletResponse();
        AuthenticationException authException = new BadCredentialsException("Invalid credentials");

        // When
        entryPoint.commence(request, response, authException);

        // Then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
        assertThat(response.getContentType()).startsWith(MediaType.APPLICATION_JSON_VALUE);
        assertThat(response.getCharacterEncoding()).isEqualTo("UTF-8");
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldReturnCorrectJsonErrorContent() throws IOException, ServletException {
        // Given
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/protected");
        MockHttpServletResponse response = new MockHttpServletResponse();
        AuthenticationException authException = new BadCredentialsException("Invalid credentials");

        // When
        entryPoint.commence(request, response, authException);

        // Then
        String responseContent = response.getContentAsString();
        Map<String, Object> errorResponse = objectMapper.readValue(responseContent, Map.class);

        assertThat(errorResponse).containsKey("error");
        assertThat(errorResponse).containsKey("error_description");
        assertThat(errorResponse).containsKey("timestamp");
        assertThat(errorResponse).containsKey("path");

        assertThat(errorResponse.get("error")).isEqualTo("unauthorized");
        assertThat(errorResponse.get("error_description")).isEqualTo("Invalid credentials");
        assertThat(errorResponse.get("path")).isEqualTo("/api/protected");
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldReturnDefaultErrorDescriptionWhenExceptionMessageIsNull() throws IOException, ServletException {
        // Given
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/protected");
        MockHttpServletResponse response = new MockHttpServletResponse();
        AuthenticationException authException = new AuthenticationException(null) {};

        // When
        entryPoint.commence(request, response, authException);

        // Then
        String responseContent = response.getContentAsString();
        Map<String, Object> errorResponse = objectMapper.readValue(responseContent, Map.class);

        assertThat(errorResponse.get("error_description")).isEqualTo("认证失败，请提供有效的凭据");
    }
}