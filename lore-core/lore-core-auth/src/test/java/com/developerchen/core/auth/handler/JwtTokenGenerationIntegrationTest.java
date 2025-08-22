package com.developerchen.core.auth.handler;

import com.developerchen.core.auth.config.SecurityAutoConfiguration;
import com.developerchen.core.auth.service.RefreshTokenService;
import com.developerchen.core.auth.service.TokenService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.test.context.TestPropertySource;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * JWT 令牌生成集成测试
 * 
 * @author syc
 */
@SpringBootTest(classes = {SecurityAutoConfiguration.class, JwtTokenGenerationIntegrationTest.TestConfig.class})
@TestPropertySource(properties = {
    "security.token.jwt-secret=test-secret-key-for-jwt-token-generation-integration-test",
    "security.token.jwt-issuer=lore-core-test",
    "security.token.access-token-expiration=PT30M"
})
class JwtTokenGenerationIntegrationTest {
    
    @Autowired
    private ApiAuthenticationSuccessHandler handler;
    
    @Autowired
    private JwtEncoder jwtEncoder;
    
    @Autowired
    private JwtDecoder jwtDecoder;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Test
    void shouldGenerateValidJwtTokenWhenRequested() throws Exception {
        // Given
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Token-Type", "jwt");
        
        MockHttpServletResponse response = new MockHttpServletResponse();
        
        UserDetails userDetails = User.builder()
            .username("testuser")
            .password("password")
            .authorities("ROLE_USER", "ROLE_ADMIN")
            .build();
        
        Authentication authentication = new PreAuthenticatedAuthenticationToken(
            userDetails, null, userDetails.getAuthorities()
        );
        
        // When
        handler.onAuthenticationSuccess(request, response, authentication);
        
        // Then
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.getContentType()).isEqualTo("application/json");
        
        String responseBody = response.getContentAsString();
        JsonNode responseJson = objectMapper.readTree(responseBody);
        
        assertThat(responseJson.get("token_type").asText()).isEqualTo("jwt");
        assertThat(responseJson.get("access_token")).isNotNull();
        assertThat(responseJson.get("refresh_token")).isNotNull();
        assertThat(responseJson.get("expires_in").asLong()).isEqualTo(1800); // 30 minutes
        
        String accessToken = responseJson.get("access_token").asText();
        
        // Verify JWT can be decoded
        Jwt decodedJwt = jwtDecoder.decode(accessToken);
        assertThat(decodedJwt.getSubject()).isEqualTo("testuser");
        assertThat(decodedJwt.getClaimAsString("iss")).isEqualTo("lore-core-test");
        assertThat(decodedJwt.getClaimAsString("authorities")).isEqualTo("ROLE_USER,ROLE_ADMIN");
        assertThat(decodedJwt.getClaimAsString("username")).isEqualTo("testuser");
        assertThat(decodedJwt.getClaimAsString("token_type")).isEqualTo("access_token");
        assertThat(decodedJwt.getClaimAsBoolean("enabled")).isTrue();
        assertThat(decodedJwt.getClaimAsBoolean("account_non_expired")).isTrue();
        assertThat(decodedJwt.getClaimAsBoolean("account_non_locked")).isTrue();
        assertThat(decodedJwt.getClaimAsBoolean("credentials_non_expired")).isTrue();
        assertThat((Object) decodedJwt.getClaim("auth_time")).isNotNull();
        
        // Verify expiration time
        Instant expectedExpiration = Instant.now().plusSeconds(1800);
        assertThat(decodedJwt.getExpiresAt()).isBetween(
            expectedExpiration.minusSeconds(5), 
            expectedExpiration.plusSeconds(5)
        );
    }
    
    @Test
    void shouldGenerateUuidTokenWhenJwtNotRequested() throws Exception {
        // Given
        MockHttpServletRequest request = new MockHttpServletRequest();
        // No JWT token type specified
        
        MockHttpServletResponse response = new MockHttpServletResponse();
        
        UserDetails userDetails = User.builder()
            .username("testuser")
            .password("password")
            .authorities("ROLE_USER")
            .build();
        
        Authentication authentication = new PreAuthenticatedAuthenticationToken(
            userDetails, null, userDetails.getAuthorities()
        );
        
        // When
        handler.onAuthenticationSuccess(request, response, authentication);
        
        // Then
        String responseBody = response.getContentAsString();
        JsonNode responseJson = objectMapper.readTree(responseBody);
        
        assertThat(responseJson.get("token_type").asText()).isEqualTo("uuid");
        
        String accessToken = responseJson.get("access_token").asText();
        
        // UUID token should not be a valid JWT
        assertThat(accessToken).doesNotContain(".");
        assertThat(accessToken).hasSize(36); // Standard UUID length
    }
    
    @Test
    void shouldGenerateJwtTokenWithAcceptHeader() throws Exception {
        // Given
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Accept", "application/jwt");
        
        MockHttpServletResponse response = new MockHttpServletResponse();
        
        UserDetails userDetails = User.builder()
            .username("jwtuser")
            .password("password")
            .authorities("ROLE_JWT_USER")
            .build();
        
        Authentication authentication = new PreAuthenticatedAuthenticationToken(
            userDetails, null, userDetails.getAuthorities()
        );
        
        // When
        handler.onAuthenticationSuccess(request, response, authentication);
        
        // Then
        String responseBody = response.getContentAsString();
        JsonNode responseJson = objectMapper.readTree(responseBody);
        
        assertThat(responseJson.get("token_type").asText()).isEqualTo("jwt");
        
        String accessToken = responseJson.get("access_token").asText();
        Jwt decodedJwt = jwtDecoder.decode(accessToken);
        
        assertThat(decodedJwt.getSubject()).isEqualTo("jwtuser");
        assertThat(decodedJwt.getClaimAsString("authorities")).isEqualTo("ROLE_JWT_USER");
    }
    
    @Test
    void shouldGenerateJwtTokenWithParameterRequest() throws Exception {
        // Given
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setParameter("token_type", "JWT");
        
        MockHttpServletResponse response = new MockHttpServletResponse();
        
        UserDetails userDetails = User.builder()
            .username("paramuser")
            .password("password")
            .authorities("ROLE_PARAM_USER")
            .build();
        
        Authentication authentication = new PreAuthenticatedAuthenticationToken(
            userDetails, null, userDetails.getAuthorities()
        );
        
        // When
        handler.onAuthenticationSuccess(request, response, authentication);
        
        // Then
        String responseBody = response.getContentAsString();
        JsonNode responseJson = objectMapper.readTree(responseBody);
        
        assertThat(responseJson.get("token_type").asText()).isEqualTo("jwt");
        
        String accessToken = responseJson.get("access_token").asText();
        Jwt decodedJwt = jwtDecoder.decode(accessToken);
        
        assertThat(decodedJwt.getSubject()).isEqualTo("paramuser");
        assertThat(decodedJwt.getClaimAsString("authorities")).isEqualTo("ROLE_PARAM_USER");
    }
    
    @TestConfiguration
    static class TestConfig {
        
        @Bean
        public UserDetailsService userDetailsService() {
            UserDetails user = User.builder()
                .username("testuser")
                .password("password")
                .authorities("ROLE_USER")
                .build();
            
            return username -> {
                if ("testuser".equals(username)) {
                    return user;
                }
                throw new RuntimeException("User not found: " + username);
            };
        }
        
        @Bean
        public ObjectMapper objectMapper() {
            return new ObjectMapper();
        }
    }
}