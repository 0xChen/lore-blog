package com.developerchen.core.auth.service;

/**
 * 令牌对，包含访问令牌和刷新令牌
 * 
 * @param accessToken 访问令牌
 * @param refreshToken 刷新令牌
 * @param tokenType 令牌类型
 * @param expiresIn 过期时间（秒）
 * @author syc
 */
public record TokenPair(
    String accessToken,
    String refreshToken,
    TokenType tokenType,
    long expiresIn
) {}