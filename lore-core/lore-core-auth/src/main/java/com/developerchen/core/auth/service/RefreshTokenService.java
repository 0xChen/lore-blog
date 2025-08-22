package com.developerchen.core.auth.service;

/**
 * 刷新令牌服务接口
 * 提供刷新令牌的完整生命周期管理
 * 
 * @author syc
 */
public interface RefreshTokenService {
    
    /**
     * 生成刷新令牌
     * 
     * @param username 用户名
     * @return 生成的刷新令牌
     */
    String generateRefreshToken(String username);
    
    /**
     * 验证刷新令牌
     * 
     * @param refreshToken 要验证的刷新令牌
     * @return 验证结果
     */
    RefreshTokenValidationResult validateRefreshToken(String refreshToken);
    
    /**
     * 刷新访问令牌
     * 
     * @param refreshToken 刷新令牌
     * @param preferredType 首选的访问令牌类型
     * @return 新的令牌对
     */
    TokenPair refreshAccessToken(String refreshToken, TokenType preferredType);
    
    /**
     * 撤销刷新令牌
     * 
     * @param refreshToken 要撤销的刷新令牌
     */
    void revokeRefreshToken(String refreshToken);
    
    /**
     * 撤销用户的所有刷新令牌
     * 
     * @param username 用户名
     */
    void revokeAllRefreshTokensForUser(String username);
}