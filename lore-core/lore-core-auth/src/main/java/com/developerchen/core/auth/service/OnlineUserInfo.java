package com.developerchen.core.auth.service;

import java.time.Instant;

/**
 * 在线用户信息
 * 
 * @author syc
 */
public record OnlineUserInfo(
    String sessionId,
    String username,
    Instant loginTime,
    Instant lastAccessTime,
    String ipAddress,
    String userAgent,
    boolean active
) {
    
    /**
     * 创建在线用户信息
     */
    public static OnlineUserInfo create(String sessionId, String username, Instant loginTime, 
                                      String ipAddress, String userAgent) {
        return new OnlineUserInfo(sessionId, username, loginTime, loginTime, ipAddress, userAgent, true);
    }
    
    /**
     * 更新最后访问时间
     */
    public OnlineUserInfo updateLastAccessTime(Instant lastAccessTime) {
        return new OnlineUserInfo(sessionId, username, loginTime, lastAccessTime, ipAddress, userAgent, active);
    }
    
    /**
     * 标记为非活跃状态
     */
    public OnlineUserInfo markInactive() {
        return new OnlineUserInfo(sessionId, username, loginTime, lastAccessTime, ipAddress, userAgent, false);
    }
}