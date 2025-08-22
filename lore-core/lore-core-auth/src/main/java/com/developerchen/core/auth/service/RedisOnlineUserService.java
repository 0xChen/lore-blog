package com.developerchen.core.auth.service;

import com.developerchen.core.auth.config.SecurityProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.session.SessionRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 基于 Redis 的在线用户管理服务实现
 * 
 * @author syc
 */
@Service
public class RedisOnlineUserService implements OnlineUserService {

    private static final Logger logger = LoggerFactory.getLogger(RedisOnlineUserService.class);
    
    private static final String ONLINE_USER_KEY_PREFIX = "online:user:";
    private static final String USER_SESSIONS_KEY_PREFIX = "user:sessions:";
    private static final String SESSION_USER_KEY_PREFIX = "session:user:";
    
    private final RedisTemplate<String, Object> redisTemplate;
    private final SessionRepository<?> sessionRepository;
    private final SecurityProperties securityProperties;

    public RedisOnlineUserService(RedisTemplate<String, Object> redisTemplate,
                                SessionRepository<?> sessionRepository,
                                SecurityProperties securityProperties) {
        this.redisTemplate = redisTemplate;
        this.sessionRepository = sessionRepository;
        this.securityProperties = securityProperties;
    }

    @Override
    public void userOnline(String sessionId, String username, Instant loginTime, String ipAddress, String userAgent) {
        try {
            String namespace = securityProperties.getSession().getRedisNamespace();
            
            // 存储在线用户信息
            OnlineUserInfo userInfo = new OnlineUserInfo(sessionId, username, loginTime, Instant.now(), ipAddress, userAgent);
            String userKey = namespace + ":" + ONLINE_USER_KEY_PREFIX + sessionId;
            redisTemplate.opsForValue().set(userKey, userInfo, 30, TimeUnit.MINUTES);
            
            // 添加到用户会话集合
            String userSessionsKey = namespace + ":" + USER_SESSIONS_KEY_PREFIX + username;
            redisTemplate.opsForSet().add(userSessionsKey, sessionId);
            redisTemplate.expire(userSessionsKey, 30, TimeUnit.MINUTES);
            
            // 建立会话到用户的映射
            String sessionUserKey = namespace + ":" + SESSION_USER_KEY_PREFIX + sessionId;
            redisTemplate.opsForValue().set(sessionUserKey, username, 30, TimeUnit.MINUTES);
            
            logger.debug("用户 {} 上线，会话ID: {}", username, sessionId);
        } catch (Exception e) {
            logger.error("记录用户上线失败: sessionId={}, username={}", sessionId, username, e);
        }
    }

    @Override
    public void userOffline(String sessionId) {
        try {
            String namespace = securityProperties.getSession().getRedisNamespace();
            
            // 获取用户名
            String sessionUserKey = namespace + ":" + SESSION_USER_KEY_PREFIX + sessionId;
            String username = (String) redisTemplate.opsForValue().get(sessionUserKey);
            
            if (username != null) {
                // 从用户会话集合中移除
                String userSessionsKey = namespace + ":" + USER_SESSIONS_KEY_PREFIX + username;
                redisTemplate.opsForSet().remove(userSessionsKey, sessionId);
                
                logger.debug("用户 {} 下线，会话ID: {}", username, sessionId);
            }
            
            // 删除在线用户信息
            String userKey = namespace + ":" + ONLINE_USER_KEY_PREFIX + sessionId;
            redisTemplate.delete(userKey);
            
            // 删除会话到用户的映射
            redisTemplate.delete(sessionUserKey);
            
        } catch (Exception e) {
            logger.error("记录用户下线失败: sessionId={}", sessionId, e);
        }
    }

    @Override
    public long getOnlineUserCount() {
        try {
            String namespace = securityProperties.getSession().getRedisNamespace();
            String pattern = namespace + ":" + ONLINE_USER_KEY_PREFIX + "*";
            Set<String> keys = redisTemplate.keys(pattern);
            return keys != null ? keys.size() : 0;
        } catch (Exception e) {
            logger.error("获取在线用户数量失败", e);
            return 0;
        }
    }

    @Override
    public List<OnlineUserInfo> getUserSessions(String username) {
        try {
            String namespace = securityProperties.getSession().getRedisNamespace();
            String userSessionsKey = namespace + ":" + USER_SESSIONS_KEY_PREFIX + username;
            Set<Object> sessionIds = redisTemplate.opsForSet().members(userSessionsKey);
            
            if (sessionIds == null || sessionIds.isEmpty()) {
                return List.of();
            }
            
            return sessionIds.stream()
                .map(sessionId -> getOnlineUser((String) sessionId))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("获取用户会话失败: username={}", username, e);
            return List.of();
        }
    }

    @Override
    public List<OnlineUserInfo> getAllOnlineUsers() {
        try {
            String namespace = securityProperties.getSession().getRedisNamespace();
            String pattern = namespace + ":" + ONLINE_USER_KEY_PREFIX + "*";
            Set<String> keys = redisTemplate.keys(pattern);
            
            if (keys == null || keys.isEmpty()) {
                return List.of();
            }
            
            return keys.stream()
                .map(key -> {
                    try {
                        return (OnlineUserInfo) redisTemplate.opsForValue().get(key);
                    } catch (Exception e) {
                        logger.warn("获取在线用户信息失败: key={}", key, e);
                        return null;
                    }
                })
                .filter(userInfo -> userInfo != null)
                .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("获取所有在线用户失败", e);
            return List.of();
        }
    }

    @Override
    public Optional<OnlineUserInfo> getOnlineUser(String sessionId) {
        try {
            String namespace = securityProperties.getSession().getRedisNamespace();
            String userKey = namespace + ":" + ONLINE_USER_KEY_PREFIX + sessionId;
            OnlineUserInfo userInfo = (OnlineUserInfo) redisTemplate.opsForValue().get(userKey);
            return Optional.ofNullable(userInfo);
        } catch (Exception e) {
            logger.error("获取在线用户信息失败: sessionId={}", sessionId, e);
            return Optional.empty();
        }
    }

    @Override
    public boolean kickOutUser(String sessionId) {
        try {
            // 删除 Spring Session
            sessionRepository.deleteById(sessionId);
            
            // 清理在线用户信息
            userOffline(sessionId);
            
            logger.info("成功踢出用户会话: {}", sessionId);
            return true;
        } catch (Exception e) {
            logger.error("踢出用户失败: sessionId={}", sessionId, e);
            return false;
        }
    }

    @Override
    public int kickOutAllUserSessions(String username) {
        try {
            List<OnlineUserInfo> userSessions = getUserSessions(username);
            int kickedCount = 0;
            
            for (OnlineUserInfo userInfo : userSessions) {
                if (kickOutUser(userInfo.sessionId())) {
                    kickedCount++;
                }
            }
            
            logger.info("成功踢出用户 {} 的 {} 个会话", username, kickedCount);
            return kickedCount;
        } catch (Exception e) {
            logger.error("踢出用户所有会话失败: username={}", username, e);
            return 0;
        }
    }
}