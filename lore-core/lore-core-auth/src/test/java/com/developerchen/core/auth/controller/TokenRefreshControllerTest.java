package com.developerchen.core.auth.controller;

import com.developerchen.core.auth.service.RefreshTokenService;
import com.developerchen.core.auth.service.RefreshTokenValidationResult;
import com.developerchen.core.auth.service.TokenPair;
import com.developerchen.core.auth.service.TokenType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.Instant;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * TokenRefreshController 单元测试
 * 
 * @author syc
 */
@ExtendWith(MockitoExtension.class)
class TokenRefreshControllerTest {

    @Mock
    private RefreshTokenService refreshTokenService;

    private ObjectMapper objectMapper;
    private TokenRefreshController controller;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        controller = new TokenRefreshController(refreshTokenService, objectMapper);
    }

    @Test
    void refreshToken_WithValidJsonRequest_ShouldReturnNewTokens() throws IOException {
        // Given
        String refreshToken = "valid-refresh-token";
        String username = "testuser";
        
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setContentType(MediaType.APPLICATION_JSON_VALUE);
        String jsonBody = "{\"refresh_token\":\"" + refreshToken + "\"}";
        request.setContent(jsonBody.getBytes());

        RefreshTokenValidationResult validationResult = RefreshTokenValidationResult.valid(username, Instant.now().plusSeconds(3600));
        TokenPair tokenPair = new TokenPair("new-access-token", "new-refresh-token", TokenType.UUID, 1800);

        when(refreshTokenService.validateRefreshToken(refreshToken)).thenReturn(validationResult);
        when(refreshTokenService.refreshAccessToken(eq(refreshToken), any(TokenType.class))).thenReturn(tokenPair);

        // When
        ResponseEntity<Map<String, Object>> response = controller.refreshToken(request);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("access_token")).isEqualTo("new-access-token");
        assertThat(response.getBody().get("refresh_token")).isEqualTo("new-refresh-token");
        assertThat(response.getBody().get("token_type")).isEqualTo("uuid");
        assertThat(response.getBody().get("expires_in")).isEqualTo(1800L);
        assertThat(response.getBody()).containsKey("timestamp");
    }

    @Test
    void refreshToken_WithValidFormRequest_ShouldReturnNewTokens() {
        // Given
        String refreshToken = "valid-refresh-token";
        String username = "testuser";
        
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setContentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE);
        request.setParameter("refresh_token", refreshToken);

        RefreshTokenValidationResult validationResult = RefreshTokenValidationResult.valid(username, Instant.now().plusSeconds(3600));
        TokenPair tokenPair = new TokenPair("new-access-token", "new-refresh-token", TokenType.UUID, 1800);

        when(refreshTokenService.validateRefreshToken(refreshToken)).thenReturn(validationResult);
        when(refreshTokenService.refreshAccessToken(eq(refreshToken), any(TokenType.class))).thenReturn(tokenPair);

        // When
        ResponseEntity<Map<String, Object>> response = controller.refreshToken(request);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("access_token")).isEqualTo("new-access-token");
        assertThat(response.getBody().get("refresh_token")).isEqualTo("new-refresh-token");
        assertThat(response.getBody().get("token_type")).isEqualTo("uuid");
        assertThat(response.getBody().get("expires_in")).isEqualTo(1800L);
    }

    @Test
    void refreshToken_WithJwtTokenTypePreference_ShouldReturnJwtTokens() throws IOException {
        // Given
        String refreshToken = "valid-refresh-token";
        String username = "testuser";
        
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setContentType(MediaType.APPLICATION_JSON_VALUE);
        request.addHeader("X-Token-Type", "jwt");
        String jsonBody = "{\"refresh_token\":\"" + refreshToken + "\"}";
        request.setContent(jsonBody.getBytes());

        RefreshTokenValidationResult validationResult = RefreshTokenValidationResult.valid(username, Instant.now().plusSeconds(3600));
        TokenPair tokenPair = new TokenPair("jwt-access-token", "new-refresh-token", TokenType.JWT, 1800);

        when(refreshTokenService.validateRefreshToken(refreshToken)).thenReturn(validationResult);
        when(refreshTokenService.refreshAccessToken(eq(refreshToken), eq(TokenType.JWT))).thenReturn(tokenPair);

        // When
        ResponseEntity<Map<String, Object>> response = controller.refreshToken(request);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("token_type")).isEqualTo("jwt");
    }

    @Test
    void refreshToken_WithMissingRefreshToken_ShouldReturnBadRequest() {
        // Given
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setContentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE);

        // When
        ResponseEntity<Map<String, Object>> response = controller.refreshToken(request);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("error")).isEqualTo("missing_refresh_token");
        assertThat(response.getBody().get("error_description")).isEqualTo("刷新令牌不能为空");
    }

    @Test
    void refreshToken_WithInvalidRefreshToken_ShouldReturnUnauthorized() {
        // Given
        String refreshToken = "invalid-refresh-token";
        
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setContentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE);
        request.setParameter("refresh_token", refreshToken);

        RefreshTokenValidationResult validationResult = RefreshTokenValidationResult.invalid("刷新令牌已过期");

        when(refreshTokenService.validateRefreshToken(refreshToken)).thenReturn(validationResult);

        // When
        ResponseEntity<Map<String, Object>> response = controller.refreshToken(request);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("error")).isEqualTo("invalid_refresh_token");
        assertThat(response.getBody().get("error_description")).isEqualTo("刷新令牌已过期");
    }

    @Test
    void refreshToken_WithServiceException_ShouldReturnUnauthorized() {
        // Given
        String refreshToken = "valid-refresh-token";
        String username = "testuser";
        
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setContentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE);
        request.setParameter("refresh_token", refreshToken);

        RefreshTokenValidationResult validationResult = RefreshTokenValidationResult.valid(username, Instant.now().plusSeconds(3600));

        when(refreshTokenService.validateRefreshToken(refreshToken)).thenReturn(validationResult);
        when(refreshTokenService.refreshAccessToken(eq(refreshToken), any(TokenType.class)))
            .thenThrow(new IllegalArgumentException("令牌刷新失败"));

        // When
        ResponseEntity<Map<String, Object>> response = controller.refreshToken(request);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("error")).isEqualTo("invalid_refresh_token");
        assertThat(response.getBody().get("error_description")).isEqualTo("令牌刷新失败");
    }

    @Test
    void refreshToken_WithUnexpectedException_ShouldReturnInternalServerError() {
        // Given
        String refreshToken = "valid-refresh-token";
        
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setContentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE);
        request.setParameter("refresh_token", refreshToken);

        when(refreshTokenService.validateRefreshToken(refreshToken))
            .thenThrow(new RuntimeException("Unexpected error"));

        // When
        ResponseEntity<Map<String, Object>> response = controller.refreshToken(request);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("error")).isEqualTo("server_error");
        assertThat(response.getBody().get("error_description")).isEqualTo("服务器内部错误");
    }

    @Test
    void refreshToken_WithInvalidJsonRequest_ShouldReturnBadRequest() throws IOException {
        // Given
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setContentType(MediaType.APPLICATION_JSON_VALUE);
        String invalidJsonBody = "{invalid json}";
        request.setContent(invalidJsonBody.getBytes());

        // When
        ResponseEntity<Map<String, Object>> response = controller.refreshToken(request);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("error")).isEqualTo("missing_refresh_token");
    }

    @Test
    void refreshToken_WithAcceptJwtHeader_ShouldPreferJwtTokenType() throws IOException {
        // Given
        String refreshToken = "valid-refresh-token";
        String username = "testuser";
        
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setContentType(MediaType.APPLICATION_JSON_VALUE);
        request.addHeader("Accept", "application/jwt");
        String jsonBody = "{\"refresh_token\":\"" + refreshToken + "\"}";
        request.setContent(jsonBody.getBytes());

        RefreshTokenValidationResult validationResult = RefreshTokenValidationResult.valid(username, Instant.now().plusSeconds(3600));
        TokenPair tokenPair = new TokenPair("jwt-access-token", "new-refresh-token", TokenType.JWT, 1800);

        when(refreshTokenService.validateRefreshToken(refreshToken)).thenReturn(validationResult);
        when(refreshTokenService.refreshAccessToken(eq(refreshToken), eq(TokenType.JWT))).thenReturn(tokenPair);

        // When
        ResponseEntity<Map<String, Object>> response = controller.refreshToken(request);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("token_type")).isEqualTo("jwt");
    }

    @Test
    void refreshToken_WithTokenTypeParameter_ShouldPreferJwtTokenType() {
        // Given
        String refreshToken = "valid-refresh-token";
        String username = "testuser";
        
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setContentType(MediaType.APPLICATION_FORM_URLENCODED_VALUE);
        request.setParameter("refresh_token", refreshToken);
        request.setParameter("token_type", "jwt");

        RefreshTokenValidationResult validationResult = RefreshTokenValidationResult.valid(username, Instant.now().plusSeconds(3600));
        TokenPair tokenPair = new TokenPair("jwt-access-token", "new-refresh-token", TokenType.JWT, 1800);

        when(refreshTokenService.validateRefreshToken(refreshToken)).thenReturn(validationResult);
        when(refreshTokenService.refreshAccessToken(eq(refreshToken), eq(TokenType.JWT))).thenReturn(tokenPair);

        // When
        ResponseEntity<Map<String, Object>> response = controller.refreshToken(request);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("token_type")).isEqualTo("jwt");
    }
}