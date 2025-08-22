package com.developerchen.core.auth.integration;

import com.developerchen.core.auth.config.SecurityAutoConfiguration;
import com.developerchen.core.auth.service.InMemoryRefreshTokenService;
import com.developerchen.core.auth.service.InMemoryTokenService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * 端到端认证流程集成测试
 * 测试表单登录、API登录、令牌认证和刷新的完整流程
 * 
 * 需求覆盖:
 * - 1.1: 表单登录完整流程
 * - 1.2: API登录完整流程  
 * - 2.1: 令牌生成和验证
 * - 3.2: 令牌刷新流程
 * 
 * @author syc
 */
@SpringBootTest(classes = {
    SecurityAutoConfiguration.class,
    EndToEndAuthenticationFlowTest.TestConfig.class
})
@AutoConfigureWebMvc
@TestPropertySource(properties = {
    "security.token.jwt-secret=test-secret-key-for-end-to-end-authentication-flow-integration-test-with-sufficient-length",
    "security.token.access-token-expiration=PT30M",
    "security.token.refresh-token-expiration=P7D",
    "security.session.stateful=true",
    "security.remember-me.key=test-remember-me-key"
})
class EndToEndAuthenticationFlowTest {

    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Autowired
    private InMemoryTokenService tokenService;
    
    @Autowired
    private InMemoryRefreshTokenService refreshTokenService;

    private static final String TEST_USERNAME = "testuser";
    private static final String TEST_PASSWORD = "password";
    private static final String INVALID_USERNAME = "invaliduser";
    private static final String INVALID_PASSWORD = "wrongpassword";

    @BeforeEach
    void setUp() {
        // 清理之前的测试数据
        tokenService.cleanupExpiredTokens();
        refreshTokenService.cleanupExpiredRefreshTokens();
    }

    /**
     * 测试表单登录完整流程
     * 需求 1.1: 表单登录应该使用标准HTML表单提交进行认证，并启用CSRF保护
     */
    @Test
    void testFormLoginCompleteFlow() throws Exception {
        // 1. 访问受保护资源，应该重定向到登录页面
        mockMvc.perform(get("/protected"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));

        // 2. 获取登录页面和CSRF令牌
        MvcResult loginPageResult = mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andReturn();

        // 3. 提交表单登录（成功）
        mockMvc.perform(post("/login")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("username", TEST_USERNAME)
                .param("password", TEST_PASSWORD)
                .with(request -> {
                    // 模拟CSRF令牌
                    request.setAttribute("_csrf", "test-csrf-token");
                    return request;
                }))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));

        // 4. 登录后访问受保护资源应该成功
        mockMvc.perform(get("/protected")
                .sessionAttr("SPRING_SECURITY_CONTEXT", "authenticated"))
                .andExpect(status().isOk());
    }

    /**
     * 测试表单登录失败流程
     */
    @Test
    void testFormLoginFailureFlow() throws Exception {
        // 提交错误的登录凭据
        mockMvc.perform(post("/login")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("username", INVALID_USERNAME)
                .param("password", INVALID_PASSWORD)
                .with(request -> {
                    request.setAttribute("_csrf", "test-csrf-token");
                    return request;
                }))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login?error"));
    }

    /**
     * 测试API登录完整流程 - UUID令牌
     * 需求 1.2: API登录应该使用JSON载荷进行认证，并禁用CSRF保护
     * 需求 2.1: 认证成功时应该生成令牌
     */
    @Test
    void testApiLoginCompleteFlowWithUuidToken() throws Exception {
        // 1. API登录请求（成功）
        String loginRequest = objectMapper.writeValueAsString(
            java.util.Map.of(
                "username", TEST_USERNAME,
                "password", TEST_PASSWORD
            )
        );

        MvcResult loginResult = mockMvc.perform(post("/api/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginRequest))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.access_token").exists())
                .andExpect(jsonPath("$.refresh_token").exists())
                .andExpect(jsonPath("$.token_type").value("uuid"))
                .andExpect(jsonPath("$.expires_in").exists())
                .andExpect(jsonPath("$.timestamp").exists())
                .andReturn();

        // 2. 解析登录响应获取令牌
        String responseContent = loginResult.getResponse().getContentAsString();
        JsonNode responseJson = objectMapper.readTree(responseContent);
        String accessToken = responseJson.get("access_token").asText();
        String refreshToken = responseJson.get("refresh_token").asText();

        assertNotNull(accessToken);
        assertNotNull(refreshToken);
        assertTrue(accessToken.length() > 0);
        assertTrue(refreshToken.length() > 0);

        // 3. 使用访问令牌访问受保护资源（Header方式）
        mockMvc.perform(get("/api/protected")
                .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk());

        // 4. 使用访问令牌访问受保护资源（URL参数方式）
        mockMvc.perform(get("/api/protected")
                .param("access_token", accessToken))
                .andExpect(status().isOk());

        // 5. 验证令牌在服务中的存储
        var validationResult = tokenService.validateToken(accessToken);
        assertTrue(validationResult.valid());
        assertEquals(TEST_USERNAME, validationResult.username());
    }

    /**
     * 测试API登录完整流程 - JWT令牌
     * 需求 2.2: 当请求JWT令牌类型时，系统应该使用JwtEncoder生成标准JWT访问令牌
     */
    @Test
    void testApiLoginCompleteFlowWithJwtToken() throws Exception {
        // 1. API登录请求JWT令牌（通过Accept头）
        String loginRequest = objectMapper.writeValueAsString(
            java.util.Map.of(
                "username", TEST_USERNAME,
                "password", TEST_PASSWORD
            )
        );

        MvcResult loginResult = mockMvc.perform(post("/api/login")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Accept", MediaType.APPLICATION_JSON_VALUE + ", application/jwt")
                .content(loginRequest))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.access_token").exists())
                .andExpect(jsonPath("$.refresh_token").exists())
                .andExpect(jsonPath("$.token_type").value("jwt"))
                .andExpect(jsonPath("$.expires_in").exists())
                .andExpect(jsonPath("$.timestamp").exists())
                .andReturn();

        // 2. 解析JWT令牌
        String responseContent = loginResult.getResponse().getContentAsString();
        JsonNode responseJson = objectMapper.readTree(responseContent);
        String jwtToken = responseJson.get("access_token").asText();
        String refreshToken = responseJson.get("refresh_token").asText();

        assertNotNull(jwtToken);
        assertNotNull(refreshToken);
        // JWT令牌应该包含三个部分（header.payload.signature）
        assertEquals(3, jwtToken.split("\\.").length);

        // 3. 使用JWT令牌访问受保护资源
        mockMvc.perform(get("/api/protected")
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk());
    }

    /**
     * 测试API登录失败流程
     */
    @Test
    void testApiLoginFailureFlow() throws Exception {
        // 1. 使用错误凭据登录
        String loginRequest = objectMapper.writeValueAsString(
            java.util.Map.of(
                "username", INVALID_USERNAME,
                "password", INVALID_PASSWORD
            )
        );

        mockMvc.perform(post("/api/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginRequest))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").exists())
                .andExpect(jsonPath("$.error_description").exists())
                .andExpect(jsonPath("$.timestamp").exists());

        // 2. 缺少用户名
        String incompleteRequest = objectMapper.writeValueAsString(
            java.util.Map.of("password", TEST_PASSWORD)
        );

        mockMvc.perform(post("/api/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(incompleteRequest))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").exists());

        // 3. 无效的JSON格式
        mockMvc.perform(post("/api/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{invalid json}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").exists());
    }

    /**
     * 测试令牌认证和刷新完整流程
     * 需求 3.2: 使用刷新令牌来刷新过期的访问令牌
     */
    @Test
    void testTokenAuthenticationAndRefreshFlow() throws Exception {
        // 1. 首先通过API登录获取令牌
        String loginRequest = objectMapper.writeValueAsString(
            java.util.Map.of(
                "username", TEST_USERNAME,
                "password", TEST_PASSWORD
            )
        );

        MvcResult loginResult = mockMvc.perform(post("/api/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginRequest))
                .andExpect(status().isOk())
                .andReturn();

        String responseContent = loginResult.getResponse().getContentAsString();
        JsonNode responseJson = objectMapper.readTree(responseContent);
        String originalAccessToken = responseJson.get("access_token").asText();
        String originalRefreshToken = responseJson.get("refresh_token").asText();

        // 2. 使用访问令牌访问受保护资源
        mockMvc.perform(get("/api/protected")
                .header("Authorization", "Bearer " + originalAccessToken))
                .andExpect(status().isOk());

        // 3. 使用刷新令牌获取新的访问令牌
        String refreshRequest = objectMapper.writeValueAsString(
            java.util.Map.of("refresh_token", originalRefreshToken)
        );

        MvcResult refreshResult = mockMvc.perform(post("/api/token/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(refreshRequest))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.access_token").exists())
                .andExpect(jsonPath("$.refresh_token").exists())
                .andExpect(jsonPath("$.token_type").exists())
                .andExpect(jsonPath("$.expires_in").exists())
                .andReturn();

        // 4. 解析新令牌
        String refreshResponseContent = refreshResult.getResponse().getContentAsString();
        JsonNode refreshResponseJson = objectMapper.readTree(refreshResponseContent);
        String newAccessToken = refreshResponseJson.get("access_token").asText();
        String newRefreshToken = refreshResponseJson.get("refresh_token").asText();

        // 5. 验证新令牌与原令牌不同
        assertNotEquals(originalAccessToken, newAccessToken);
        assertNotEquals(originalRefreshToken, newRefreshToken);

        // 6. 使用新访问令牌访问受保护资源
        mockMvc.perform(get("/api/protected")
                .header("Authorization", "Bearer " + newAccessToken))
                .andExpect(status().isOk());

        // 7. 验证原刷新令牌已失效（令牌轮换）
        mockMvc.perform(post("/api/token/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(refreshRequest))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("invalid_refresh_token"));
    }

    /**
     * 测试不同认证方式的响应格式验证
     * 验证表单和API认证返回不同格式的响应
     */
    @Test
    void testDifferentAuthenticationResponseFormats() throws Exception {
        // 1. 表单登录成功 - 应该重定向
        mockMvc.perform(post("/login")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("username", TEST_USERNAME)
                .param("password", TEST_PASSWORD)
                .with(request -> {
                    request.setAttribute("_csrf", "test-csrf-token");
                    return request;
                }))
                .andExpect(status().is3xxRedirection())
                .andExpect(header().exists("Location"));

        // 2. API登录成功 - 应该返回JSON
        String apiLoginRequest = objectMapper.writeValueAsString(
            java.util.Map.of(
                "username", TEST_USERNAME,
                "password", TEST_PASSWORD
            )
        );

        mockMvc.perform(post("/api/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(apiLoginRequest))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.access_token").exists())
                .andExpect(jsonPath("$.refresh_token").exists())
                .andExpect(jsonPath("$.token_type").exists())
                .andExpect(jsonPath("$.expires_in").isNumber())
                .andExpect(jsonPath("$.timestamp").exists());

        // 3. 表单登录失败 - 应该重定向到错误页面
        mockMvc.perform(post("/login")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("username", INVALID_USERNAME)
                .param("password", INVALID_PASSWORD)
                .with(request -> {
                    request.setAttribute("_csrf", "test-csrf-token");
                    return request;
                }))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login?error"));

        // 4. API登录失败 - 应该返回JSON错误
        String apiFailureRequest = objectMapper.writeValueAsString(
            java.util.Map.of(
                "username", INVALID_USERNAME,
                "password", INVALID_PASSWORD
            )
        );

        mockMvc.perform(post("/api/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(apiFailureRequest))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").exists())
                .andExpect(jsonPath("$.error_description").exists())
                .andExpect(jsonPath("$.timestamp").exists());
    }

    /**
     * 测试无效令牌访问
     */
    @Test
    void testInvalidTokenAccess() throws Exception {
        // 1. 使用无效令牌访问受保护资源
        mockMvc.perform(get("/api/protected")
                .header("Authorization", "Bearer invalid-token"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").exists())
                .andExpect(jsonPath("$.timestamp").exists());

        // 2. 使用空令牌访问
        mockMvc.perform(get("/api/protected")
                .header("Authorization", "Bearer "))
                .andExpect(status().isUnauthorized());

        // 3. 不提供令牌访问API端点
        mockMvc.perform(get("/api/protected"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").exists());
    }

    /**
     * 测试配置类
     */
    @Configuration
    static class TestConfig {
        
        @Bean
        @Primary
        public UserDetailsService userDetailsService(PasswordEncoder passwordEncoder) {
            UserDetails user = User.builder()
                    .username(TEST_USERNAME)
                    .password(passwordEncoder.encode(TEST_PASSWORD))
                    .roles("USER")
                    .build();
            
            return new InMemoryUserDetailsManager(user);
        }
    }
}