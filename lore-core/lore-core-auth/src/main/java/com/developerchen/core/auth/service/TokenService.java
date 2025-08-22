package com.developerchen.core.auth.service;

import java.time.Instant;

/**
 * 令牌服务接口
 * 提供令牌的存储、验证和删除功能
 * 
 * @author syc
 */
public interface TokenService {
    
    /**
     * 存储令牌及其关联信息
     * 
     * @param token 令牌值
     * @param username 关联的用户名
     * @param expiration 过期时间
     */
    void storeToken(String token, String username, Instant expiration);
    
    /**
     * 验证令牌有效性
     * 
     * @param token 要验证的令牌
     * @return 验证结果
     */
    TokenValidationResult validateToken(String token);
    
    /**
     * 删除令牌
     * 
     * @param token 要删除的令牌
     */
    void deleteToken(String token);
    
    /**
     * 生成新的访问令牌
     * 
     * @param username 用户名
     * @param type 令牌类型
     * @return 生成的令牌
     */
    String generateAccessToken(String username, TokenType type);
    
    /**
     * 删除用户的所有令牌
     * 
     * @param username 用户名
     */
    void deleteAllTokensForUser(String username);
}