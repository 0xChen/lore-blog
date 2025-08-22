package com.developerchen.core.auth.service;

import com.developerchen.core.auth.config.SecurityProperties;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 内存版本的刷新令牌服务实现
 * 用于演示和测试目的
 * 
 * @author syc
 */
@Service
public class InMemoryRefreshTokenService implements RefreshTokenService {
    
    /**
     * 刷新令牌存储信息
     */
    private static class RefreshTokenInfo {
        private final String username;
        private final Instant expiration;
        
        public RefreshTokenInfo(String username, Instant expiration) {
            this.username = username;
            this.expiration = expiration;
        }
        
        public String getUsername() {
            return username;
        }
        
        public Instant getExpiration() {
            return expiration;
        }
        
        public boolean isExpired() {
            return Instant.now().isAfter(expiration);
        }
    }
    
    private final Map<String, RefreshTokenInfo> refreshTokenStore = new ConcurrentHashMap<>();
    private final TokenService tokenService;
    private final SecurityProperties securityProperties;
    
    public InMemoryRefreshTokenService(TokenService tokenService, SecurityProperties securityProperties) {
        this.tokenService = tokenService;
        this.securityProperties = securityProperties;
    }
    
    @Override
    public String generateRefreshToken(String username) {
        String refreshToken = UUID.randomUUID().toString();
        Instant expiration = Instant.now().plus(securityProperties.getToken().getRefreshTokenExpiration());
        refreshTokenStore.put(refreshToken, new RefreshTokenInfo(username, expiration));
        return refreshToken;
    }
    
    @Override
    public RefreshTokenValidationResult validateRefreshToken(String refreshToken) {
        if (refreshToken == null || refreshToken.trim().isEmpty()) {
            return RefreshTokenValidationResult.invalid("刷新令牌不能为空");
        }
        
        RefreshTokenInfo tokenInfo = refreshTokenStore.get(refreshToken);
        if (tokenInfo == null) {
            return RefreshTokenValidationResult.invalid("刷新令牌不存在");
        }
        
        if (tokenInfo.isExpired()) {
            // 清理过期令牌
            refreshTokenStore.remove(refreshToken);
            return RefreshTokenValidationResult.invalid("刷新令牌已过期");
        }
        
        return RefreshTokenValidationResult.valid(tokenInfo.getUsername(), tokenInfo.getExpiration());
    }
    
    @Override
    public TokenPair refreshAccessToken(String refreshToken, TokenType preferredType) {
        RefreshTokenValidationResult validationResult = validateRefreshToken(refreshToken);
        if (!validationResult.valid()) {
            throw new IllegalArgumentException("无效的刷新令牌: " + validationResult.errorMessage());
        }
        
        String username = validationResult.username();
        
        // 生成新的访问令牌
        String newAccessToken = tokenService.generateAccessToken(username, preferredType);
        Instant accessTokenExpiration = Instant.now().plus(securityProperties.getToken().getAccessTokenExpiration());
        tokenService.storeToken(newAccessToken, username, accessTokenExpiration);
        
        // 生成新的刷新令牌（令牌轮换）
        revokeRefreshToken(refreshToken);
        String newRefreshToken = generateRefreshToken(username);
        
        long expiresIn = securityProperties.getToken().getAccessTokenExpiration().toSeconds();
        
        return new TokenPair(newAccessToken, newRefreshToken, preferredType, expiresIn);
    }
    
    @Override
    public void revokeRefreshToken(String refreshToken) {
        refreshTokenStore.remove(refreshToken);
    }
    
    @Override
    public void revokeAllRefreshTokensForUser(String username) {
        refreshTokenStore.entrySet().removeIf(entry -> 
            username.equals(entry.getValue().getUsername()));
    }
    
    /**
     * 清理所有过期刷新令牌
     */
    public void cleanupExpiredRefreshTokens() {
        Instant now = Instant.now();
        refreshTokenStore.entrySet().removeIf(entry -> 
            now.isAfter(entry.getValue().getExpiration()));
    }
    
    /**
     * 获取当前存储的刷新令牌数量（用于测试）
     */
    public int getRefreshTokenCount() {
        return refreshTokenStore.size();
    }
}