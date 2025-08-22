package com.developerchen.core.auth.service;

import java.time.Instant;

/**
 * 刷新令牌验证结果
 * 
 * @param valid 刷新令牌是否有效
 * @param username 关联的用户名
 * @param expiration 过期时间
 * @param errorMessage 错误信息（当令牌无效时）
 * @author syc
 */
public record RefreshTokenValidationResult(
    boolean valid,
    String username,
    Instant expiration,
    String errorMessage
) {
    
    /**
     * 创建有效的刷新令牌验证结果
     */
    public static RefreshTokenValidationResult valid(String username, Instant expiration) {
        return new RefreshTokenValidationResult(true, username, expiration, null);
    }
    
    /**
     * 创建无效的刷新令牌验证结果
     */
    public static RefreshTokenValidationResult invalid(String errorMessage) {
        return new RefreshTokenValidationResult(false, null, null, errorMessage);
    }
}