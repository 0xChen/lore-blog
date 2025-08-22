package com.developerchen.core.auth.controller;

import com.developerchen.core.auth.config.SecurityProperties;
import com.developerchen.core.auth.service.InMemoryRefreshTokenService;
import com.developerchen.core.auth.service.InMemoryTokenService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * TokenRefreshController 集成测试
 * 
 * @author syc
 */
class TokenRefreshControllerIntegrationTest {

    private MockMvc mockMvc;
    private InMemoryRefreshTokenService refreshTokenService;
    private ObjectMapper objectMapper;
    private String validRefreshToken;

    @BeforeEach
    void setUp() {
        // 手动创建依赖
        SecurityProperties securityProperties = new SecurityProperties();
        InMemoryTokenService tokenService = new InMemoryTokenService();
        refreshTokenService = new InMemoryRefreshTokenService(tokenService, securityProperties);
        objectMapper = new ObjectMapper();
        
        // 创建控制器并设置 MockMvc
        TokenRefreshController controller = new TokenRefreshController(refreshTokenService, objectMapper);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
        
        // 生成一个有效的刷新令牌用于测试
        validRefreshToken = refreshTokenService.generateRefreshToken("testuser");
    }

    @Test
    void refreshToken_WithValidJsonRequest_ShouldReturnNewTokens() throws Exception {
        String requestBody = objectMapper.writeValueAsString(
            java.util.Map.of("refresh_token", validRefreshToken)
        );

        mockMvc.perform(post("/api/token/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.access_token").exists())
                .andExpect(jsonPath("$.refresh_token").exists())
                .andExpect(jsonPath("$.token_type").value("uuid"))
                .andExpect(jsonPath("$.expires_in").exists())
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void refreshToken_WithValidFormRequest_ShouldReturnNewTokens() throws Exception {
        mockMvc.perform(post("/api/token/refresh")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("refresh_token", validRefreshToken))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.access_token").exists())
                .andExpect(jsonPath("$.refresh_token").exists())
                .andExpect(jsonPath("$.token_type").value("uuid"))
                .andExpect(jsonPath("$.expires_in").exists())
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void refreshToken_WithJwtTokenTypeHeader_ShouldReturnJwtTokens() throws Exception {
        String requestBody = objectMapper.writeValueAsString(
            java.util.Map.of("refresh_token", validRefreshToken)
        );

        mockMvc.perform(post("/api/token/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-Token-Type", "jwt")
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.access_token").exists())
                .andExpect(jsonPath("$.refresh_token").exists())
                .andExpect(jsonPath("$.token_type").value("jwt"))
                .andExpect(jsonPath("$.expires_in").exists())
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void refreshToken_WithAcceptJwtHeader_ShouldReturnJwtTokens() throws Exception {
        String requestBody = objectMapper.writeValueAsString(
            java.util.Map.of("refresh_token", validRefreshToken)
        );

        mockMvc.perform(post("/api/token/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Accept", MediaType.APPLICATION_JSON_VALUE + ", application/jwt")
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.access_token").exists())
                .andExpect(jsonPath("$.refresh_token").exists())
                .andExpect(jsonPath("$.token_type").value("jwt"))
                .andExpect(jsonPath("$.expires_in").exists())
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void refreshToken_WithTokenTypeParameter_ShouldReturnJwtTokens() throws Exception {
        mockMvc.perform(post("/api/token/refresh")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("refresh_token", validRefreshToken)
                .param("token_type", "jwt"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.access_token").exists())
                .andExpect(jsonPath("$.refresh_token").exists())
                .andExpect(jsonPath("$.token_type").value("jwt"))
                .andExpect(jsonPath("$.expires_in").exists())
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void refreshToken_WithMissingRefreshToken_ShouldReturnBadRequest() throws Exception {
        String requestBody = objectMapper.writeValueAsString(
            java.util.Map.of("other_field", "value")
        );

        mockMvc.perform(post("/api/token/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("missing_refresh_token"))
                .andExpect(jsonPath("$.error_description").value("刷新令牌不能为空"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void refreshToken_WithInvalidRefreshToken_ShouldReturnUnauthorized() throws Exception {
        String requestBody = objectMapper.writeValueAsString(
            java.util.Map.of("refresh_token", "invalid-token")
        );

        mockMvc.perform(post("/api/token/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("invalid_refresh_token"))
                .andExpect(jsonPath("$.error_description").exists())
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void refreshToken_WithExpiredRefreshToken_ShouldReturnUnauthorized() throws Exception {
        // 使用一个已过期的刷新令牌（通过直接操作服务来模拟）
        String expiredToken = "expired-token";
        
        String requestBody = objectMapper.writeValueAsString(
            java.util.Map.of("refresh_token", expiredToken)
        );

        mockMvc.perform(post("/api/token/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("invalid_refresh_token"))
                .andExpect(jsonPath("$.error_description").exists())
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void refreshToken_WithEmptyRefreshTokenInForm_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(post("/api/token/refresh")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("refresh_token", ""))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("missing_refresh_token"))
                .andExpect(jsonPath("$.error_description").value("刷新令牌不能为空"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void refreshToken_WithInvalidJsonFormat_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(post("/api/token/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{invalid json}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").value("missing_refresh_token"))
                .andExpect(jsonPath("$.error_description").value("刷新令牌不能为空"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void refreshToken_TokenRotation_ShouldInvalidateOldRefreshToken() throws Exception {
        // 第一次刷新
        String requestBody = objectMapper.writeValueAsString(
            java.util.Map.of("refresh_token", validRefreshToken)
        );

        mockMvc.perform(post("/api/token/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk());

        // 尝试再次使用相同的刷新令牌应该失败
        mockMvc.perform(post("/api/token/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("invalid_refresh_token"));
    }
}