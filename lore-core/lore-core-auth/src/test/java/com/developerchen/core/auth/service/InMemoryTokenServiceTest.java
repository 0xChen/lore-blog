package com.developerchen.core.auth.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * InMemoryTokenService 单元测试
 * 
 * @author syc
 */
class InMemoryTokenServiceTest {
    
    private InMemoryTokenService tokenService;
    
    @BeforeEach
    void setUp() {
        tokenService = new InMemoryTokenService();
    }
    
    @Test
    void testStoreAndValidateToken() {
        String token = "test-token";
        String username = "testuser";
        Instant expiration = Instant.now().plus(1, ChronoUnit.HOURS);
        
        // 存储令牌
        tokenService.storeToken(token, username, expiration);
        
        // 验证令牌
        TokenValidationResult result = tokenService.validateToken(token);
        assertTrue(result.valid());
        assertEquals(username, result.username());
        assertEquals(expiration, result.expiration());
        assertNull(result.errorMessage());
    }
    
    @Test
    void testValidateNonExistentToken() {
        TokenValidationResult result = tokenService.validateToken("non-existent-token");
        assertFalse(result.valid());
        assertNull(result.username());
        assertNull(result.expiration());
        assertEquals("令牌不存在", result.errorMessage());
    }
    
    @Test
    void testValidateNullToken() {
        TokenValidationResult result = tokenService.validateToken(null);
        assertFalse(result.valid());
        assertEquals("令牌不能为空", result.errorMessage());
    }
    
    @Test
    void testValidateEmptyToken() {
        TokenValidationResult result = tokenService.validateToken("  ");
        assertFalse(result.valid());
        assertEquals("令牌不能为空", result.errorMessage());
    }
    
    @Test
    void testValidateExpiredToken() {
        String token = "expired-token";
        String username = "testuser";
        Instant expiration = Instant.now().minus(1, ChronoUnit.HOURS);
        
        // 存储过期令牌
        tokenService.storeToken(token, username, expiration);
        
        // 验证过期令牌
        TokenValidationResult result = tokenService.validateToken(token);
        assertFalse(result.valid());
        assertEquals("令牌已过期", result.errorMessage());
        
        // 验证过期令牌已被清理
        assertEquals(0, tokenService.getTokenCount());
    }
    
    @Test
    void testDeleteToken() {
        String token = "test-token";
        String username = "testuser";
        Instant expiration = Instant.now().plus(1, ChronoUnit.HOURS);
        
        // 存储令牌
        tokenService.storeToken(token, username, expiration);
        assertEquals(1, tokenService.getTokenCount());
        
        // 删除令牌
        tokenService.deleteToken(token);
        assertEquals(0, tokenService.getTokenCount());
        
        // 验证令牌已被删除
        TokenValidationResult result = tokenService.validateToken(token);
        assertFalse(result.valid());
    }
    
    @Test
    void testGenerateUUIDAccessToken() {
        String token = tokenService.generateAccessToken("testuser", TokenType.UUID);
        assertNotNull(token);
        assertFalse(token.isEmpty());
        // UUID 格式验证
        assertTrue(token.matches("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}"));
    }
    
    @Test
    void testGenerateJWTAccessToken() {
        String token = tokenService.generateAccessToken("testuser", TokenType.JWT);
        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertTrue(token.startsWith("jwt-placeholder-"));
    }
    
    @Test
    void testDeleteAllTokensForUser() {
        String username1 = "user1";
        String username2 = "user2";
        Instant expiration = Instant.now().plus(1, ChronoUnit.HOURS);
        
        // 为两个用户存储令牌
        tokenService.storeToken("token1", username1, expiration);
        tokenService.storeToken("token2", username1, expiration);
        tokenService.storeToken("token3", username2, expiration);
        
        assertEquals(3, tokenService.getTokenCount());
        
        // 删除 user1 的所有令牌
        tokenService.deleteAllTokensForUser(username1);
        assertEquals(1, tokenService.getTokenCount());
        
        // 验证只有 user2 的令牌还存在
        TokenValidationResult result = tokenService.validateToken("token3");
        assertTrue(result.valid());
        assertEquals(username2, result.username());
    }
    
    @Test
    void testCleanupExpiredTokens() {
        Instant pastExpiration = Instant.now().minus(1, ChronoUnit.HOURS);
        Instant futureExpiration = Instant.now().plus(1, ChronoUnit.HOURS);
        
        // 存储过期和未过期的令牌
        tokenService.storeToken("expired-token", "user1", pastExpiration);
        tokenService.storeToken("valid-token", "user2", futureExpiration);
        
        assertEquals(2, tokenService.getTokenCount());
        
        // 清理过期令牌
        tokenService.cleanupExpiredTokens();
        assertEquals(1, tokenService.getTokenCount());
        
        // 验证只有有效令牌还存在
        TokenValidationResult result = tokenService.validateToken("valid-token");
        assertTrue(result.valid());
    }
}