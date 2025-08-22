package com.developerchen.core.auth.service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * 在线用户管理服务接口
 * 
 * @author syc
 */
public interface OnlineUserService {

    /**
     * 用户上线
     * 
     * @param sessionId 会话ID
     * @param username 用户名
     * @param loginTime 登录时间
     * @param ipAddress IP地址
     * @param userAgent 用户代理
     */
    void userOnline(String sessionId, String username, Instant loginTime, String ipAddress, String userAgent);

    /**
     * 用户下线
     * 
     * @param sessionId 会话ID
     */
    void userOffline(String sessionId);

    /**
     * 获取在线用户总数
     * 
     * @return 在线用户数量
     */
    long getOnlineUserCount();

    /**
     * 获取指定用户的在线会话
     * 
     * @param username 用户名
     * @return 在线会话列表
     */
    List<OnlineUserInfo> getUserSessions(String username);

    /**
     * 获取所有在线用户
     * 
     * @return 在线用户列表
     */
    List<OnlineUserInfo> getAllOnlineUsers();

    /**
     * 根据会话ID获取在线用户信息
     * 
     * @param sessionId 会话ID
     * @return 在线用户信息
     */
    Optional<OnlineUserInfo> getOnlineUser(String sessionId);

    /**
     * 踢出用户（强制下线）
     * 
     * @param sessionId 会话ID
     * @return 是否成功踢出
     */
    boolean kickOutUser(String sessionId);

    /**
     * 踢出用户的所有会话
     * 
     * @param username 用户名
     * @return 踢出的会话数量
     */
    int kickOutAllUserSessions(String username);

    /**
     * 在线用户信息
     */
    record OnlineUserInfo(
        String sessionId,
        String username,
        Instant loginTime,
        Instant lastAccessTime,
        String ipAddress,
        String userAgent
    ) {}
}