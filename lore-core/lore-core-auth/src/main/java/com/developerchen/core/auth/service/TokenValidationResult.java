package com.developerchen.core.auth.service;

import java.time.Instant;

/**
 * 令牌验证结果
 * 
 * @param valid 令牌是否有效
 * @param username 关联的用户名
 * @param expiration 过期时间
 * @param errorMessage 错误信息（当令牌无效时）
 * @author syc
 */
public record TokenValidationResult(
    boolean valid,
    String username,
    Instant expiration,
    String errorMessage
) {
    
    /**
     * 创建有效的令牌验证结果
     */
    public static TokenValidationResult valid(String username, Instant expiration) {
        return new TokenValidationResult(true, username, expiration, null);
    }
    
    /**
     * 创建无效的令牌验证结果
     */
    public static TokenValidationResult invalid(String errorMessage) {
        return new TokenValidationResult(false, null, null, errorMessage);
    }
}