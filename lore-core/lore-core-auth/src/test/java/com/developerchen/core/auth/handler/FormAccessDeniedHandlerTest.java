package com.developerchen.core.auth.handler;

import jakarta.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

class FormAccessDeniedHandlerTest {

    private FormAccessDeniedHandler handler;

    @BeforeEach
    void setUp() {
        handler = new FormAccessDeniedHandler();
    }

    @Test
    void shouldReturnForbiddenStatus() throws IOException, ServletException {
        // Given
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/protected");
        MockHttpServletResponse response = new MockHttpServletResponse();
        AccessDeniedException accessDeniedException = new AccessDeniedException("Access denied");

        // When
        handler.handle(request, response, accessDeniedException);

        // Then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.FORBIDDEN.value());
    }

    @Test
    void shouldHandleAccessDeniedExceptionWithCustomMessage() throws IOException, ServletException {
        // Given
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/admin/users");
        MockHttpServletResponse response = new MockHttpServletResponse();
        AccessDeniedException accessDeniedException = new AccessDeniedException("Insufficient privileges");

        // When
        handler.handle(request, response, accessDeniedException);

        // Then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.FORBIDDEN.value());
    }
}