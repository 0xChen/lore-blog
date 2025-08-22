package com.developerchen.core.auth.service;

import com.developerchen.core.auth.config.SecurityProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

/**
 * InMemoryRefreshTokenService 单元测试
 * 
 * @author syc
 */
@ExtendWith(MockitoExtension.class)
class InMemoryRefreshTokenServiceTest {
    
    @Mock
    private TokenService tokenService;
    
    @Mock
    private SecurityProperties securityProperties;
    
    @Mock
    private SecurityProperties.Token tokenProperties;
    
    private InMemoryRefreshTokenService refreshTokenService;
    
    @BeforeEach
    void setUp() {
        lenient().when(securityProperties.getToken()).thenReturn(tokenProperties);
        lenient().when(tokenProperties.getRefreshTokenExpiration()).thenReturn(Duration.ofDays(7));
        lenient().when(tokenProperties.getAccessTokenExpiration()).thenReturn(Duration.ofMinutes(30));
        
        refreshTokenService = new InMemoryRefreshTokenService(tokenService, securityProperties);
    }
    
    @Test
    void testGenerateRefreshToken() {
        String username = "testuser";
        
        String refreshToken = refreshTokenService.generateRefreshToken(username);
        
        assertNotNull(refreshToken);
        assertFalse(refreshToken.isEmpty());
        assertEquals(1, refreshTokenService.getRefreshTokenCount());
        
        // 验证生成的刷新令牌
        RefreshTokenValidationResult result = refreshTokenService.validateRefreshToken(refreshToken);
        assertTrue(result.valid());
        assertEquals(username, result.username());
    }
    
    @Test
    void testValidateRefreshToken() {
        String username = "testuser";
        String refreshToken = refreshTokenService.generateRefreshToken(username);
        
        RefreshTokenValidationResult result = refreshTokenService.validateRefreshToken(refreshToken);
        
        assertTrue(result.valid());
        assertEquals(username, result.username());
        assertNotNull(result.expiration());
        assertNull(result.errorMessage());
    }
    
    @Test
    void testValidateNonExistentRefreshToken() {
        RefreshTokenValidationResult result = refreshTokenService.validateRefreshToken("non-existent-token");
        
        assertFalse(result.valid());
        assertNull(result.username());
        assertNull(result.expiration());
        assertEquals("刷新令牌不存在", result.errorMessage());
    }
    
    @Test
    void testValidateNullRefreshToken() {
        RefreshTokenValidationResult result = refreshTokenService.validateRefreshToken(null);
        
        assertFalse(result.valid());
        assertEquals("刷新令牌不能为空", result.errorMessage());
    }
    
    @Test
    void testValidateEmptyRefreshToken() {
        RefreshTokenValidationResult result = refreshTokenService.validateRefreshToken("  ");
        
        assertFalse(result.valid());
        assertEquals("刷新令牌不能为空", result.errorMessage());
    }
    
    @Test
    void testRefreshAccessToken() {
        String username = "testuser";
        String refreshToken = refreshTokenService.generateRefreshToken(username);
        String newAccessToken = "new-access-token";
        
        when(tokenService.generateAccessToken(username, TokenType.JWT)).thenReturn(newAccessToken);
        
        TokenPair tokenPair = refreshTokenService.refreshAccessToken(refreshToken, TokenType.JWT);
        
        assertNotNull(tokenPair);
        assertEquals(newAccessToken, tokenPair.accessToken());
        assertNotNull(tokenPair.refreshToken());
        assertNotEquals(refreshToken, tokenPair.refreshToken()); // 新的刷新令牌
        assertEquals(TokenType.JWT, tokenPair.tokenType());
        assertEquals(1800L, tokenPair.expiresIn()); // 30分钟 = 1800秒
        
        // 验证旧的刷新令牌已被撤销
        RefreshTokenValidationResult result = refreshTokenService.validateRefreshToken(refreshToken);
        assertFalse(result.valid());
        
        // 验证新的访问令牌已被存储
        verify(tokenService).storeToken(eq(newAccessToken), eq(username), any(Instant.class));
    }
    
    @Test
    void testRefreshAccessTokenWithInvalidRefreshToken() {
        assertThrows(IllegalArgumentException.class, () -> {
            refreshTokenService.refreshAccessToken("invalid-token", TokenType.JWT);
        });
    }
    
    @Test
    void testRevokeRefreshToken() {
        String username = "testuser";
        String refreshToken = refreshTokenService.generateRefreshToken(username);
        
        assertEquals(1, refreshTokenService.getRefreshTokenCount());
        
        refreshTokenService.revokeRefreshToken(refreshToken);
        
        assertEquals(0, refreshTokenService.getRefreshTokenCount());
        
        // 验证令牌已被撤销
        RefreshTokenValidationResult result = refreshTokenService.validateRefreshToken(refreshToken);
        assertFalse(result.valid());
    }
    
    @Test
    void testRevokeAllRefreshTokensForUser() {
        String username1 = "user1";
        String username2 = "user2";
        
        String token1 = refreshTokenService.generateRefreshToken(username1);
        String token2 = refreshTokenService.generateRefreshToken(username1);
        String token3 = refreshTokenService.generateRefreshToken(username2);
        
        assertEquals(3, refreshTokenService.getRefreshTokenCount());
        
        refreshTokenService.revokeAllRefreshTokensForUser(username1);
        
        assertEquals(1, refreshTokenService.getRefreshTokenCount());
        
        // 验证 user1 的令牌已被撤销
        RefreshTokenValidationResult result1 = refreshTokenService.validateRefreshToken(token1);
        assertFalse(result1.valid());
        
        RefreshTokenValidationResult result2 = refreshTokenService.validateRefreshToken(token2);
        assertFalse(result2.valid());
        
        // 验证 user2 的令牌仍然有效
        RefreshTokenValidationResult result3 = refreshTokenService.validateRefreshToken(token3);
        assertTrue(result3.valid());
        assertEquals(username2, result3.username());
    }
    
    @Test
    void testCleanupExpiredRefreshTokens() {
        // 模拟过期的刷新令牌
        when(tokenProperties.getRefreshTokenExpiration()).thenReturn(Duration.ofMillis(-1));
        
        String username = "testuser";
        refreshTokenService.generateRefreshToken(username);
        
        assertEquals(1, refreshTokenService.getRefreshTokenCount());
        
        // 重置为正常的过期时间，然后生成新令牌
        when(tokenProperties.getRefreshTokenExpiration()).thenReturn(Duration.ofDays(7));
        refreshTokenService.generateRefreshToken(username);
        
        assertEquals(2, refreshTokenService.getRefreshTokenCount());
        
        // 清理过期令牌
        refreshTokenService.cleanupExpiredRefreshTokens();
        
        assertEquals(1, refreshTokenService.getRefreshTokenCount());
    }
}