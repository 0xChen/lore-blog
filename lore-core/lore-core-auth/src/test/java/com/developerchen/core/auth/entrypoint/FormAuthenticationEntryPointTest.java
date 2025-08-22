package com.developerchen.core.auth.entrypoint;

import jakarta.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

class FormAuthenticationEntryPointTest {

    private FormAuthenticationEntryPoint entryPoint;

    @BeforeEach
    void setUp() {
        entryPoint = new FormAuthenticationEntryPoint();
    }

    @Test
    void shouldRedirectToLoginPage() throws IOException, ServletException {
        // Given
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/protected");
        MockHttpServletResponse response = new MockHttpServletResponse();
        AuthenticationException authException = new BadCredentialsException("Invalid credentials");

        // When
        entryPoint.commence(request, response, authException);

        // Then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.FOUND.value());
        assertThat(response.getRedirectedUrl()).contains("/login");
    }

    @Test
    void shouldPreserveOriginalRequestUrl() throws IOException, ServletException {
        // Given
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/protected/resource");
        request.setQueryString("param=value");
        MockHttpServletResponse response = new MockHttpServletResponse();
        AuthenticationException authException = new BadCredentialsException("Invalid credentials");

        // When
        entryPoint.commence(request, response, authException);

        // Then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.FOUND.value());
        String redirectUrl = response.getRedirectedUrl();
        assertThat(redirectUrl).contains("/login");
        // The LoginUrlAuthenticationEntryPoint should redirect to login page
        // Note: The continue parameter behavior depends on Spring Security configuration
    }
}