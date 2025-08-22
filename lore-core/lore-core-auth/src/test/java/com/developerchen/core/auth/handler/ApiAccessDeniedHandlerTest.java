package com.developerchen.core.auth.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;

import java.io.IOException;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ApiAccessDeniedHandlerTest {

    private ApiAccessDeniedHandler handler;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        handler = new ApiAccessDeniedHandler(objectMapper);
    }

    @Test
    void shouldReturnJsonErrorResponseWithCorrectStatus() throws IOException, ServletException {
        // Given
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/protected");
        MockHttpServletResponse response = new MockHttpServletResponse();
        AccessDeniedException accessDeniedException = new AccessDeniedException("Access denied");

        // When
        handler.handle(request, response, accessDeniedException);

        // Then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.FORBIDDEN.value());
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
        AccessDeniedException accessDeniedException = new AccessDeniedException("Access denied");

        // When
        handler.handle(request, response, accessDeniedException);

        // Then
        String responseContent = response.getContentAsString();
        Map<String, Object> errorResponse = objectMapper.readValue(responseContent, Map.class);

        assertThat(errorResponse).containsKey("error");
        assertThat(errorResponse).containsKey("error_description");
        assertThat(errorResponse).containsKey("timestamp");
        assertThat(errorResponse).containsKey("path");

        assertThat(errorResponse.get("error")).isEqualTo("access_denied");
        assertThat(errorResponse.get("error_description")).isEqualTo("Access denied");
        assertThat(errorResponse.get("path")).isEqualTo("/api/protected");
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldReturnDefaultErrorDescriptionWhenExceptionMessageIsNull() throws IOException, ServletException {
        // Given
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/protected");
        MockHttpServletResponse response = new MockHttpServletResponse();
        AccessDeniedException accessDeniedException = new AccessDeniedException(null);

        // When
        handler.handle(request, response, accessDeniedException);

        // Then
        String responseContent = response.getContentAsString();
        Map<String, Object> errorResponse = objectMapper.readValue(responseContent, Map.class);

        assertThat(errorResponse.get("error_description")).isEqualTo("访问被拒绝，您没有足够的权限访问此资源");
    }
}