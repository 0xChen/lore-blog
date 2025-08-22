package com.developerchen.core.auth.listener;

import com.developerchen.core.auth.service.OnlineUserService;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.session.events.SessionCreatedEvent;
import org.springframework.session.events.SessionDeletedEvent;
import org.springframework.session.events.SessionExpiredEvent;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.Instant;

/**
 * 会话事件监听器
 * 监听会话创建、删除、过期等事件，维护在线用户状态
 * 
 * @author syc
 */
@Component
public class SessionEventListener {

    private static final Logger logger = LoggerFactory.getLogger(SessionEventListener.class);
    private static final String SPRING_SECURITY_CONTEXT = "SPRING_SECURITY_CONTEXT";

    private final OnlineUserService onlineUserService;

    public SessionEventListener(OnlineUserService onlineUserService) {
        this.onlineUserService = onlineUserService;
    }

    /**
     * 监听认证成功事件
     * 当用户成功认证后，记录用户上线信息
     */
    @EventListener
    public void handleAuthenticationSuccess(AuthenticationSuccessEvent event) {
        try {
            Authentication authentication = event.getAuthentication();
            String username = authentication.getName();
            
            // 获取会话ID和请求信息
            String sessionId = getSessionId();
            if (sessionId == null) {
                logger.warn("无法获取会话ID，跳过记录用户上线: username={}", username);
                return;
            }
            
            // 获取IP地址和User-Agent
            String ipAddress = getClientIpAddress(authentication);
            String userAgent = getUserAgent();
            
            // 记录用户上线
            onlineUserService.userOnline(sessionId, username, Instant.now(), ipAddress, userAgent);
            
            logger.debug("用户认证成功，记录上线信息: username={}, sessionId={}", username, sessionId);
        } catch (Exception e) {
            logger.error("处理认证成功事件失败", e);
        }
    }

    /**
     * 监听会话创建事件
     */
    @EventListener
    public void handleSessionCreated(SessionCreatedEvent event) {
        String sessionId = event.getSessionId();
        logger.debug("会话创建: sessionId={}", sessionId);
    }

    /**
     * 监听会话删除事件
     */
    @EventListener
    public void handleSessionDeleted(SessionDeletedEvent event) {
        String sessionId = event.getSessionId();
        logger.debug("会话删除: sessionId={}", sessionId);
        
        // 用户下线
        onlineUserService.userOffline(sessionId);
    }

    /**
     * 监听会话过期事件
     */
    @EventListener
    public void handleSessionExpired(SessionExpiredEvent event) {
        String sessionId = event.getSessionId();
        logger.debug("会话过期: sessionId={}", sessionId);
        
        // 用户下线
        onlineUserService.userOffline(sessionId);
    }

    /**
     * 获取当前会话ID
     */
    private String getSessionId() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                return request.getSession(false) != null ? request.getSession().getId() : null;
            }
        } catch (Exception e) {
            logger.debug("获取会话ID失败", e);
        }
        return null;
    }

    /**
     * 获取客户端IP地址
     */
    private String getClientIpAddress(Authentication authentication) {
        try {
            if (authentication.getDetails() instanceof WebAuthenticationDetails details) {
                return details.getRemoteAddress();
            }
            
            // 尝试从请求中获取
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                return getClientIpFromRequest(request);
            }
        } catch (Exception e) {
            logger.debug("获取客户端IP地址失败", e);
        }
        return "unknown";
    }

    /**
     * 从请求中获取客户端真实IP地址
     */
    private String getClientIpFromRequest(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        
        // 处理多个IP的情况，取第一个
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        
        return ip != null ? ip : "unknown";
    }

    /**
     * 获取User-Agent
     */
    private String getUserAgent() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                String userAgent = request.getHeader("User-Agent");
                return userAgent != null ? userAgent : "unknown";
            }
        } catch (Exception e) {
            logger.debug("获取User-Agent失败", e);
        }
        return "unknown";
    }
}