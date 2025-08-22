package com.developerchen.core.auth.handler;

import jakarta.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;

import java.io.IOException;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class DelegatingAccessDeniedHandlerTest {

    @Mock
    private ApiAccessDeniedHandler apiAccessDeniedHandler;

    @Mock
    private FormAccessDeniedHandler formAccessDeniedHandler;

    @Mock
    private AccessDeniedException accessDeniedException;

    private DelegatingAccessDeniedHandler handler;

    @BeforeEach
    void setUp() {
        handler = new DelegatingAccessDeniedHandler(apiAccessDeniedHandler, formAccessDeniedHandler);
    }

    @Test
    void shouldDelegateToApiHandlerForApiPath() throws IOException, ServletException {
        // Given
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/users");
        MockHttpServletResponse response = new MockHttpServletResponse();

        // When
        handler.handle(request, response, accessDeniedException);

        // Then
        verify(apiAccessDeniedHandler).handle(request, response, accessDeniedException);
        verifyNoInteractions(formAccessDeniedHandler);
    }

    @Test
    void shouldDelegateToApiHandlerForJsonAcceptHeader() throws IOException, ServletException {
        // Given
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/protected");
        request.addHeader("Accept", MediaType.APPLICATION_JSON_VALUE);
        MockHttpServletResponse response = new MockHttpServletResponse();

        // When
        handler.handle(request, response, accessDeniedException);

        // Then
        verify(apiAccessDeniedHandler).handle(request, response, accessDeniedException);
        verifyNoInteractions(formAccessDeniedHandler);
    }

    @Test
    void shouldDelegateToApiHandlerForJsonContentType() throws IOException, ServletException {
        // Given
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/protected");
        request.setContentType(MediaType.APPLICATION_JSON_VALUE);
        MockHttpServletResponse response = new MockHttpServletResponse();

        // When
        handler.handle(request, response, accessDeniedException);

        // Then
        verify(apiAccessDeniedHandler).handle(request, response, accessDeniedException);
        verifyNoInteractions(formAccessDeniedHandler);
    }

    @Test
    void shouldDelegateToFormHandlerForFormRequest() throws IOException, ServletException {
        // Given
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/protected");
        request.addHeader("Accept", "text/html");
        MockHttpServletResponse response = new MockHttpServletResponse();

        // When
        handler.handle(request, response, accessDeniedException);

        // Then
        verify(formAccessDeniedHandler).handle(request, response, accessDeniedException);
        verifyNoInteractions(apiAccessDeniedHandler);
    }

    @Test
    void shouldDelegateToFormHandlerForDefaultRequest() throws IOException, ServletException {
        // Given
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/protected");
        MockHttpServletResponse response = new MockHttpServletResponse();

        // When
        handler.handle(request, response, accessDeniedException);

        // Then
        verify(formAccessDeniedHandler).handle(request, response, accessDeniedException);
        verifyNoInteractions(apiAccessDeniedHandler);
    }

    @Test
    void shouldDelegateToApiHandlerForMixedAcceptHeader() throws IOException, ServletException {
        // Given
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/protected");
        request.addHeader("Accept", "text/html,application/json,*/*");
        MockHttpServletResponse response = new MockHttpServletResponse();

        // When
        handler.handle(request, response, accessDeniedException);

        // Then
        verify(apiAccessDeniedHandler).handle(request, response, accessDeniedException);
        verifyNoInteractions(formAccessDeniedHandler);
    }
}