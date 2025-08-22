package com.developerchen.core.auth.entrypoint;

import jakarta.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.AuthenticationException;

import java.io.IOException;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class DelegatingAuthenticationEntryPointTest {

    @Mock
    private ApiAuthenticationEntryPoint apiEntryPoint;

    @Mock
    private FormAuthenticationEntryPoint formEntryPoint;

    @Mock
    private AuthenticationException authException;

    private DelegatingAuthenticationEntryPoint entryPoint;

    @BeforeEach
    void setUp() {
        entryPoint = new DelegatingAuthenticationEntryPoint(apiEntryPoint, formEntryPoint);
    }

    @Test
    void shouldDelegateToApiEntryPointForApiPath() throws IOException, ServletException {
        // Given
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/users");
        MockHttpServletResponse response = new MockHttpServletResponse();

        // When
        entryPoint.commence(request, response, authException);

        // Then
        verify(apiEntryPoint).commence(request, response, authException);
        verifyNoInteractions(formEntryPoint);
    }

    @Test
    void shouldDelegateToApiEntryPointForJsonAcceptHeader() throws IOException, ServletException {
        // Given
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/protected");
        request.addHeader("Accept", MediaType.APPLICATION_JSON_VALUE);
        MockHttpServletResponse response = new MockHttpServletResponse();

        // When
        entryPoint.commence(request, response, authException);

        // Then
        verify(apiEntryPoint).commence(request, response, authException);
        verifyNoInteractions(formEntryPoint);
    }

    @Test
    void shouldDelegateToApiEntryPointForJsonContentType() throws IOException, ServletException {
        // Given
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/protected");
        request.setContentType(MediaType.APPLICATION_JSON_VALUE);
        MockHttpServletResponse response = new MockHttpServletResponse();

        // When
        entryPoint.commence(request, response, authException);

        // Then
        verify(apiEntryPoint).commence(request, response, authException);
        verifyNoInteractions(formEntryPoint);
    }

    @Test
    void shouldDelegateToFormEntryPointForFormRequest() throws IOException, ServletException {
        // Given
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/protected");
        request.addHeader("Accept", "text/html");
        MockHttpServletResponse response = new MockHttpServletResponse();

        // When
        entryPoint.commence(request, response, authException);

        // Then
        verify(formEntryPoint).commence(request, response, authException);
        verifyNoInteractions(apiEntryPoint);
    }

    @Test
    void shouldDelegateToFormEntryPointForDefaultRequest() throws IOException, ServletException {
        // Given
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/protected");
        MockHttpServletResponse response = new MockHttpServletResponse();

        // When
        entryPoint.commence(request, response, authException);

        // Then
        verify(formEntryPoint).commence(request, response, authException);
        verifyNoInteractions(apiEntryPoint);
    }

    @Test
    void shouldDelegateToApiEntryPointForMixedAcceptHeader() throws IOException, ServletException {
        // Given
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/protected");
        request.addHeader("Accept", "text/html,application/json,*/*");
        MockHttpServletResponse response = new MockHttpServletResponse();

        // When
        entryPoint.commence(request, response, authException);

        // Then
        verify(apiEntryPoint).commence(request, response, authException);
        verifyNoInteractions(formEntryPoint);
    }
}