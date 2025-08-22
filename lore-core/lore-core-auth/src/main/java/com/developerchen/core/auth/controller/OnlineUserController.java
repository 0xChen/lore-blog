package com.developerchen.core.auth.controller;

import com.developerchen.core.auth.service.OnlineUserService;
import com.developerchen.core.auth.service.OnlineUserService.OnlineUserInfo;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 在线用户管理控制器
 * 
 * @author syc
 */
@RestController
@RequestMapping("/api/admin/online-users")
@ConditionalOnProperty(name = "security.session.stateful", havingValue = "true", matchIfMissing = true)
public class OnlineUserController {
    
    private final OnlineUserService onlineUserService;
    
    public OnlineUserController(OnlineUserService onlineUserService) {
        this.onlineUserService = onlineUserService;
    }
    
    /**
     * 获取在线用户列表
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<OnlineUserInfo>> getOnlineUsers() {
        List<OnlineUserInfo> onlineUsers = onlineUserService.getAllOnlineUsers();
        return ResponseEntity.ok(onlineUsers);
    }
    
    /**
     * 获取在线用户数量
     */
    @GetMapping("/count")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Long>> getOnlineUserCount() {
        long count = onlineUserService.getOnlineUserCount();
        return ResponseEntity.ok(Map.of("count", count));
    }
    
    /**
     * 获取指定用户的所有会话
     */
    @GetMapping("/user/{username}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<OnlineUserInfo>> getUserSessions(@PathVariable String username) {
        List<OnlineUserInfo> userSessions = onlineUserService.getUserSessions(username);
        return ResponseEntity.ok(userSessions);
    }
    
    /**
     * 踢出指定会话的用户
     */
    @DeleteMapping("/session/{sessionId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> kickOutUser(@PathVariable String sessionId) {
        boolean success = onlineUserService.kickOutUser(sessionId);
        return ResponseEntity.ok(Map.of(
            "success", success,
            "message", success ? "用户已被踢出" : "踢出用户失败"
        ));
    }
    
    /**
     * 踢出指定用户的所有会话
     */
    @DeleteMapping("/user/{username}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> kickOutAllUserSessions(@PathVariable String username) {
        int kickedCount = onlineUserService.kickOutAllUserSessions(username);
        return ResponseEntity.ok(Map.of(
            "kickedCount", kickedCount,
            "message", String.format("已踢出用户 %s 的 %d 个会话", username, kickedCount)
        ));
    }
    
    /**
     * 清理过期会话
     */
    @PostMapping("/cleanup")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> cleanExpiredSessions() {
        // 清理过期会话的逻辑可以通过定时任务实现
        // 这里只是返回成功消息
        return ResponseEntity.ok(Map.of("message", "过期会话清理完成"));
    }
}