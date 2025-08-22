package com.developerchen.core.auth.service;

import com.developerchen.core.auth.config.SecurityProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.session.SessionRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * RedisOnlineUserService 单元测试
 * 
 * @author syc
 */
@ExtendWith(MockitoExtension.class)
class RedisOnlineUserServiceTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;
    
    @Mock
    private SessionRepository<?> sessionRepository;
    
    @Mock
    private ValueOperations<String, Object> valueOperations;
    
    @Mock
    private SetOperations<String, Object> setOperations;
    
    private SecurityProperties securityProperties;
    private RedisOnlineUserService onlineUserService;
    
    @BeforeEach
    void setUp() {
        securityProperties = new SecurityProperties();
        securityProperties.getSession().setRedisNamespace("test:session");
        
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(redisTemplate.opsForSet()).thenReturn(setOperations);
        
        onlineUserService = new RedisOnlineUserService(redisTemplate, sessionRepository, securityProperties);
    }
    
    @Test
    void userOnline_ShouldStoreUserInfo() {
        // Given
        String sessionId = "session123";
        String username = "testuser";
        Instant loginTime = Instant.now();
        String ipAddress = "192.168.1.1";
        String userAgent = "Mozilla/5.0";
        
        // When
        onlineUserService.userOnline(sessionId, username, loginTime, ipAddress, userAgent);
        
        // Then
        verify(valueOperations).set(eq("test:session:online:user:session123"), 
                                   any(OnlineUserService.OnlineUserInfo.class), 
                                   eq(30L), eq(TimeUnit.MINUTES));
        verify(setOperations).add("test:session:user:sessions:testuser", sessionId);
        verify(redisTemplate).expire("test:session:user:sessions:testuser", 30, TimeUnit.MINUTES);
        verify(valueOperations).set("test:session:session:user:session123", username, 30, TimeUnit.MINUTES);
    }
    
    @Test
    void userOffline_ShouldRemoveUserInfo() {
        // Given
        String sessionId = "session123";
        String username = "testuser";
        when(valueOperations.get("test:session:session:user:session123")).thenReturn(username);
        
        // When
        onlineUserService.userOffline(sessionId);
        
        // Then
        verify(setOperations).remove("test:session:user:sessions:testuser", sessionId);
        verify(redisTemplate).delete("test:session:online:user:session123");
        verify(redisTemplate).delete("test:session:session:user:session123");
    }
    
    @Test
    void getOnlineUserCount_ShouldReturnCorrectCount() {
        // Given
        Set<String> keys = Set.of("key1", "key2", "key3");
        when(redisTemplate.keys("test:session:online:user:*")).thenReturn(keys);
        
        // When
        long count = onlineUserService.getOnlineUserCount();
        
        // Then
        assertThat(count).isEqualTo(3);
    }
    
    @Test
    void getOnlineUserCount_WhenNoKeys_ShouldReturnZero() {
        // Given
        when(redisTemplate.keys("test:session:online:user:*")).thenReturn(null);
        
        // When
        long count = onlineUserService.getOnlineUserCount();
        
        // Then
        assertThat(count).isEqualTo(0);
    }
    
    @Test
    void getUserSessions_ShouldReturnUserSessions() {
        // Given
        String username = "testuser";
        Set<Object> sessionIds = Set.of("session1", "session2");
        when(setOperations.members("test:session:user:sessions:testuser")).thenReturn(sessionIds);
        
        OnlineUserService.OnlineUserInfo userInfo1 = new OnlineUserService.OnlineUserInfo(
            "session1", username, Instant.now(), Instant.now(), "192.168.1.1", "Mozilla/5.0");
        OnlineUserService.OnlineUserInfo userInfo2 = new OnlineUserService.OnlineUserInfo(
            "session2", username, Instant.now(), Instant.now(), "192.168.1.2", "Chrome/90.0");
            
        when(valueOperations.get("test:session:online:user:session1")).thenReturn(userInfo1);
        when(valueOperations.get("test:session:online:user:session2")).thenReturn(userInfo2);
        
        // When
        List<OnlineUserService.OnlineUserInfo> sessions = onlineUserService.getUserSessions(username);
        
        // Then
        assertThat(sessions).hasSize(2);
        assertThat(sessions).extracting(OnlineUserService.OnlineUserInfo::sessionId)
                           .containsExactlyInAnyOrder("session1", "session2");
    }
    
    @Test
    void getUserSessions_WhenNoSessions_ShouldReturnEmptyList() {
        // Given
        String username = "testuser";
        when(setOperations.members("test:session:user:sessions:testuser")).thenReturn(null);
        
        // When
        List<OnlineUserService.OnlineUserInfo> sessions = onlineUserService.getUserSessions(username);
        
        // Then
        assertThat(sessions).isEmpty();
    }
    
    @Test
    void getOnlineUser_ShouldReturnUserInfo() {
        // Given
        String sessionId = "session123";
        OnlineUserService.OnlineUserInfo userInfo = new OnlineUserService.OnlineUserInfo(
            sessionId, "testuser", Instant.now(), Instant.now(), "192.168.1.1", "Mozilla/5.0");
        when(valueOperations.get("test:session:online:user:session123")).thenReturn(userInfo);
        
        // When
        Optional<OnlineUserService.OnlineUserInfo> result = onlineUserService.getOnlineUser(sessionId);
        
        // Then
        assertThat(result).isPresent();
        assertThat(result.get().sessionId()).isEqualTo(sessionId);
        assertThat(result.get().username()).isEqualTo("testuser");
    }
    
    @Test
    void getOnlineUser_WhenNotFound_ShouldReturnEmpty() {
        // Given
        String sessionId = "session123";
        when(valueOperations.get("test:session:online:user:session123")).thenReturn(null);
        
        // When
        Optional<OnlineUserService.OnlineUserInfo> result = onlineUserService.getOnlineUser(sessionId);
        
        // Then
        assertThat(result).isEmpty();
    }
    
    @Test
    void kickOutUser_ShouldDeleteSessionAndCleanup() {
        // Given
        String sessionId = "session123";
        String username = "testuser";
        when(valueOperations.get("test:session:session:user:session123")).thenReturn(username);
        
        // When
        boolean result = onlineUserService.kickOutUser(sessionId);
        
        // Then
        assertThat(result).isTrue();
        verify(sessionRepository).deleteById(sessionId);
        verify(setOperations).remove("test:session:user:sessions:testuser", sessionId);
        verify(redisTemplate).delete("test:session:online:user:session123");
        verify(redisTemplate).delete("test:session:session:user:session123");
    }
    
    @Test
    void kickOutAllUserSessions_ShouldKickOutAllSessions() {
        // Given
        String username = "testuser";
        Set<Object> sessionIds = Set.of("session1", "session2");
        when(setOperations.members("test:session:user:sessions:testuser")).thenReturn(sessionIds);
        
        OnlineUserService.OnlineUserInfo userInfo1 = new OnlineUserService.OnlineUserInfo(
            "session1", username, Instant.now(), Instant.now(), "192.168.1.1", "Mozilla/5.0");
        OnlineUserService.OnlineUserInfo userInfo2 = new OnlineUserService.OnlineUserInfo(
            "session2", username, Instant.now(), Instant.now(), "192.168.1.2", "Chrome/90.0");
            
        when(valueOperations.get("test:session:online:user:session1")).thenReturn(userInfo1);
        when(valueOperations.get("test:session:online:user:session2")).thenReturn(userInfo2);
        when(valueOperations.get("test:session:session:user:session1")).thenReturn(username);
        when(valueOperations.get("test:session:session:user:session2")).thenReturn(username);
        
        // When
        int kickedCount = onlineUserService.kickOutAllUserSessions(username);
        
        // Then
        assertThat(kickedCount).isEqualTo(2);
        verify(sessionRepository).deleteById("session1");
        verify(sessionRepository).deleteById("session2");
    }
}