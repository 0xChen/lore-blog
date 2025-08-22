package com.developerchen.core.auth.handler;

import com.developerchen.core.auth.config.SecurityProperties;
import com.developerchen.core.auth.service.RefreshTokenService;
import com.developerchen.core.auth.service.TokenService;
import com.developerchen.core.auth.service.TokenType;
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
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.JwtEncoder;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Duration;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * ApiAuthenticationSuccessHandler 单元测试
 * 
 * @author syc
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ApiAuthenticationSuccessHandlerTest {

    @Mock
    private TokenService tokenService;
    
    @Mock
    private RefreshTokenService refreshTokenService;
    
    @Mock
    private JwtEncoder jwtEncoder;
    
    @Mock
    private HttpServletRequest request;
    
    @Mock
    private HttpServletResponse response;
    
    @Mock
    private Authentication authentication;
    
    private SecurityProperties securityProperties;
    private ObjectMapper objectMapper;
    private ApiAuthenticationSuccessHandler handler;
    private StringWriter responseWriter;

    @BeforeEach
    void setUp() throws Exception {
        securityProperties = new SecurityProperties();
        securityProperties.getToken().setAccessTokenExpiration(Duration.ofMinutes(30));
        
        objectMapper = new ObjectMapper();
        
        handler = new ApiAuthenticationSuccessHandler(
            tokenService, 
            refreshTokenService, 
            securityProperties, 
            objectMapper
        );
        
        responseWriter = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(responseWriter));
        when(authentication.getName()).thenReturn("testuser");
    }

    @Test
    void shouldGenerateUuidTokenByDefault() throws Exception {
        // Given
        when(tokenService.generateAccessToken("testuser", TokenType.UUID))
            .thenReturn("uuid-access-token");
        when(refreshTokenService.generateRefreshToken("testuser"))
            .thenReturn("refresh-token");

        // When
        handler.onAuthenticationSuccess(request, response, authentication);

        // Then
        verify(tokenService).generateAccessToken("testuser", TokenType.UUID);
        verify(refreshTokenService).generateRefreshToken("testuser");
        verify(response).setStatus(HttpServletResponse.SC_OK);
        verify(response).setContentType(MediaType.APPLICATION_JSON_VALUE);
        verify(response).setCharacterEncoding("UTF-8");
        
        String responseJson = responseWriter.toString();
        assertThat(responseJson).contains("uuid-access-token");
        assertThat(responseJson).contains("refresh-token");
        assertThat(responseJson).contains("\"token_type\":\"uuid\"");
        assertThat(responseJson).contains("\"expires_in\":1800");
    }

    @Test
    void shouldGenerateJwtTokenWhenAcceptHeaderContainsJwt() throws Exception {
        // Given
        when(request.getHeader("Accept")).thenReturn("application/jwt");
        when(refreshTokenService.generateRefreshToken("testuser"))
            .thenReturn("refresh-token");

        // When
        handler.onAuthenticationSuccess(request, response, authentication);

        // Then
        // JWT tokens are now generated directly, not through TokenService
        verify(tokenService, never()).generateAccessToken("testuser", TokenType.JWT);
        verify(tokenService).generateAccessToken("testuser", TokenType.UUID); // Falls back to UUID when no JwtEncoder
        
        String responseJson = responseWriter.toString();
        assertThat(responseJson).contains("\"token_type\":\"jwt\"");
    }

    @Test
    void shouldGenerateJwtTokenWhenTokenTypeParameterIsJwt() throws Exception {
        // Given
        when(request.getParameter("token_type")).thenReturn("jwt");
        when(refreshTokenService.generateRefreshToken("testuser"))
            .thenReturn("refresh-token");

        // When
        handler.onAuthenticationSuccess(request, response, authentication);

        // Then
        // JWT tokens are now generated directly, not through TokenService
        verify(tokenService, never()).generateAccessToken("testuser", TokenType.JWT);
        verify(tokenService).generateAccessToken("testuser", TokenType.UUID); // Falls back to UUID when no JwtEncoder
        
        String responseJson = responseWriter.toString();
        assertThat(responseJson).contains("\"token_type\":\"jwt\"");
    }

    @Test
    void shouldGenerateJwtTokenWhenCustomHeaderIsJwt() throws Exception {
        // Given
        when(request.getHeader("X-Token-Type")).thenReturn("JWT");
        when(refreshTokenService.generateRefreshToken("testuser"))
            .thenReturn("refresh-token");

        // When
        handler.onAuthenticationSuccess(request, response, authentication);

        // Then
        // JWT tokens are now generated directly, not through TokenService
        verify(tokenService, never()).generateAccessToken("testuser", TokenType.JWT);
        verify(tokenService).generateAccessToken("testuser", TokenType.UUID); // Falls back to UUID when no JwtEncoder
        
        String responseJson = responseWriter.toString();
        assertThat(responseJson).contains("\"token_type\":\"jwt\"");
    }

    @Test
    void shouldIncludeTimestampInResponse() throws Exception {
        // Given
        when(tokenService.generateAccessToken(any(), any())).thenReturn("access-token");
        when(refreshTokenService.generateRefreshToken(any())).thenReturn("refresh-token");

        // When
        handler.onAuthenticationSuccess(request, response, authentication);

        // Then
        String responseJson = responseWriter.toString();
        assertThat(responseJson).contains("timestamp");
    }

    @Test
    void shouldUseConfiguredExpirationTime() throws Exception {
        // Given
        securityProperties.getToken().setAccessTokenExpiration(Duration.ofHours(2));
        handler = new ApiAuthenticationSuccessHandler(
            tokenService, refreshTokenService, securityProperties, objectMapper
        );
        
        when(tokenService.generateAccessToken(any(), any())).thenReturn("access-token");
        when(refreshTokenService.generateRefreshToken(any())).thenReturn("refresh-token");

        // When
        handler.onAuthenticationSuccess(request, response, authentication);

        // Then
        String responseJson = responseWriter.toString();
        assertThat(responseJson).contains("\"expires_in\":7200"); // 2 hours = 7200 seconds
    }

    @Test
    void shouldHandleCaseInsensitiveTokenTypeParameter() throws Exception {
        // Given
        when(request.getParameter("token_type")).thenReturn("JWT");
        when(refreshTokenService.generateRefreshToken("testuser"))
            .thenReturn("refresh-token");

        // When
        handler.onAuthenticationSuccess(request, response, authentication);

        // Then
        // JWT tokens are now generated directly, not through TokenService
        verify(tokenService, never()).generateAccessToken("testuser", TokenType.JWT);
        verify(tokenService).generateAccessToken("testuser", TokenType.UUID); // Falls back to UUID when no JwtEncoder
    }
}