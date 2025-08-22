package com.developerchen.core.auth.service;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 内存版本的令牌服务实现
 * 用于演示和测试目的
 * 
 * @author syc
 */
@Service
public class InMemoryTokenService implements TokenService {
    
    /**
     * 令牌存储信息
     */
    private static class TokenInfo {
        private final String username;
        private final Instant expiration;
        
        public TokenInfo(String username, Instant expiration) {
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
    
    private final Map<String, TokenInfo> tokenStore = new ConcurrentHashMap<>();
    
    @Override
    public void storeToken(String token, String username, Instant expiration) {
        tokenStore.put(token, new TokenInfo(username, expiration));
    }
    
    @Override
    public TokenValidationResult validateToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            return TokenValidationResult.invalid("令牌不能为空");
        }
        
        TokenInfo tokenInfo = tokenStore.get(token);
        if (tokenInfo == null) {
            return TokenValidationResult.invalid("令牌不存在");
        }
        
        if (tokenInfo.isExpired()) {
            // 清理过期令牌
            tokenStore.remove(token);
            return TokenValidationResult.invalid("令牌已过期");
        }
        
        return TokenValidationResult.valid(tokenInfo.getUsername(), tokenInfo.getExpiration());
    }
    
    @Override
    public void deleteToken(String token) {
        tokenStore.remove(token);
    }
    
    @Override
    public String generateAccessToken(String username, TokenType type) {
        if (type == TokenType.UUID) {
            return UUID.randomUUID().toString();
        } else {
            // 对于 JWT 类型，这里返回一个占位符
            // 实际的 JWT 生成将在后续任务中通过 JwtEncoder 实现
            return "jwt-placeholder-" + UUID.randomUUID().toString();
        }
    }
    
    @Override
    public void deleteAllTokensForUser(String username) {
        tokenStore.entrySet().removeIf(entry -> 
            username.equals(entry.getValue().getUsername()));
    }
    
    /**
     * 清理所有过期令牌
     */
    public void cleanupExpiredTokens() {
        Instant now = Instant.now();
        tokenStore.entrySet().removeIf(entry -> 
            now.isAfter(entry.getValue().getExpiration()));
    }
    
    /**
     * 获取当前存储的令牌数量（用于测试）
     */
    public int getTokenCount() {
        return tokenStore.size();
    }
}