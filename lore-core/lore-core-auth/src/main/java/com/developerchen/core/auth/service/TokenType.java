package com.developerchen.core.auth.service;

/**
 * 令牌类型枚举
 * 
 * @author syc
 */
public enum TokenType {
    /**
     * JWT 访问令牌
     */
    JWT,
    
    /**
     * UUID 不透明令牌
     */
    UUID
}