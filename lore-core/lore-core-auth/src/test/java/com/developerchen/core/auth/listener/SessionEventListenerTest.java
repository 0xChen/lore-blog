package com.developerchen.core.auth.listener;

import com.developerchen.core.auth.service.OnlineUserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.session.events.SessionCreatedEvent;
import org.springframework.session.events.SessionDeletedEvent;
import org.springframework.session.events.SessionExpiredEvent;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * SessionEventListener 单元测试
 * 
 * @author syc
 */
@ExtendWith(MockitoExtension.class)
class SessionEventListenerTest {

    @Mock
    private OnlineUserService onlineUserService;
    
    @Mock
    private HttpServletRequest request;
    
    @Mock
    private HttpSession session;
    
    @Mock
    private ServletRequestAttributes requestAttributes;
    
    private SessionEventListener sessionEventListener;
    
    @BeforeEach
    void setUp() {
        sessionEventListener = new SessionEventListener(onlineUserService);
    }
    
    @Test
    void handleAuthenticationSuccess_ShouldRecordUserOnline() {
        // Given
        String username = "testuser";
        String sessionId = "session123";
        String ipAddress = "192.168.1.1";
        String userAgent = "Mozilla/5.0";
        
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(username, "password");
        WebAuthenticationDetails details = new WebAuthenticationDetails(request);
        authentication.setDetails(details);
        
        when(request.getSession()).thenReturn(session);
        when(session.getId()).thenReturn(sessionId);
        when(request.getRemoteAddr()).thenReturn(ipAddress);
        when(request.getHeader("User-Agent")).thenReturn(userAgent);
        when(requestAttributes.getRequest()).thenReturn(request);
        
        AuthenticationSuccessEvent event = new AuthenticationSuccessEvent(authentication);
        
        try (MockedStatic<RequestContextHolder> mockedRequestContextHolder = mockStatic(RequestContextHolder.class)) {
            mockedRequestContextHolder.when(RequestContextHolder::getRequestAttributes)
                                    .thenReturn(requestAttributes);
            
            // When
            sessionEventListener.handleAuthenticationSuccess(event);
            
            // Then
            verify(onlineUserService).userOnline(eq(sessionId), eq(username), any(Instant.class), 
                                                eq(ipAddress), eq(userAgent));
        }
    }
    
    @Test
    void handleAuthenticationSuccess_WhenNoSession_ShouldNotRecordUserOnline() {
        // Given
        String username = "testuser";
        Authentication authentication = new UsernamePasswordAuthenticationToken(username, "password");
        AuthenticationSuccessEvent event = new AuthenticationSuccessEvent(authentication);
        
        try (MockedStatic<RequestContextHolder> mockedRequestContextHolder = mockStatic(RequestContextHolder.class)) {
            mockedRequestContextHolder.when(RequestContextHolder::getRequestAttributes)
                                    .thenReturn(null);
            
            // When
            sessionEventListener.handleAuthenticationSuccess(event);
            
            // Then
            verify(onlineUserService, never()).userOnline(anyString(), anyString(), any(Instant.class), 
                                                        anyString(), anyString());
        }
    }
    
    @Test
    void handleAuthenticationSuccess_WithWebAuthenticationDetails_ShouldUseDetailsIpAddress() {
        // Given
        String username = "testuser";
        String sessionId = "session123";
        String detailsIpAddress = "10.0.0.1";
        String userAgent = "Mozilla/5.0";
        
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(username, "password");
        WebAuthenticationDetails details = mock(WebAuthenticationDetails.class);
        when(details.getRemoteAddress()).thenReturn(detailsIpAddress);
        authentication.setDetails(details);
        
        when(request.getSession()).thenReturn(session);
        when(session.getId()).thenReturn(sessionId);
        when(request.getHeader("User-Agent")).thenReturn(userAgent);
        when(requestAttributes.getRequest()).thenReturn(request);
        
        AuthenticationSuccessEvent event = new AuthenticationSuccessEvent(authentication);
        
        try (MockedStatic<RequestContextHolder> mockedRequestContextHolder = mockStatic(RequestContextHolder.class)) {
            mockedRequestContextHolder.when(RequestContextHolder::getRequestAttributes)
                                    .thenReturn(requestAttributes);
            
            // When
            sessionEventListener.handleAuthenticationSuccess(event);
            
            // Then
            verify(onlineUserService).userOnline(eq(sessionId), eq(username), any(Instant.class), 
                                                eq(detailsIpAddress), eq(userAgent));
        }
    }
    
    @Test
    void handleSessionCreated_ShouldLogSessionCreation() {
        // Given
        String sessionId = "session123";
        SessionCreatedEvent event = mock(SessionCreatedEvent.class);
        when(event.getSessionId()).thenReturn(sessionId);
        
        // When
        sessionEventListener.handleSessionCreated(event);
        
        // Then
        // 验证日志记录（这里只是确保方法执行不抛异常）
        verify(event).getSessionId();
    }
    
    @Test
    void handleSessionDeleted_ShouldCallUserOffline() {
        // Given
        String sessionId = "session123";
        SessionDeletedEvent event = mock(SessionDeletedEvent.class);
        when(event.getSessionId()).thenReturn(sessionId);
        
        // When
        sessionEventListener.handleSessionDeleted(event);
        
        // Then
        verify(onlineUserService).userOffline(sessionId);
    }
    
    @Test
    void handleSessionExpired_ShouldCallUserOffline() {
        // Given
        String sessionId = "session123";
        SessionExpiredEvent event = mock(SessionExpiredEvent.class);
        when(event.getSessionId()).thenReturn(sessionId);
        
        // When
        sessionEventListener.handleSessionExpired(event);
        
        // Then
        verify(onlineUserService).userOffline(sessionId);
    }
    
    @Test
    void handleAuthenticationSuccess_WithXForwardedForHeader_ShouldUseForwardedIp() {
        // Given
        String username = "testuser";
        String sessionId = "session123";
        String forwardedIp = "203.0.113.1";
        String userAgent = "Mozilla/5.0";
        
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(username, "password");
        
        when(request.getSession()).thenReturn(session);
        when(session.getId()).thenReturn(sessionId);
        when(request.getHeader("X-Forwarded-For")).thenReturn(forwardedIp + ", 192.168.1.1");
        when(request.getHeader("User-Agent")).thenReturn(userAgent);
        when(requestAttributes.getRequest()).thenReturn(request);
        
        AuthenticationSuccessEvent event = new AuthenticationSuccessEvent(authentication);
        
        try (MockedStatic<RequestContextHolder> mockedRequestContextHolder = mockStatic(RequestContextHolder.class)) {
            mockedRequestContextHolder.when(RequestContextHolder::getRequestAttributes)
                                    .thenReturn(requestAttributes);
            
            // When
            sessionEventListener.handleAuthenticationSuccess(event);
            
            // Then
            verify(onlineUserService).userOnline(eq(sessionId), eq(username), any(Instant.class), 
                                                eq(forwardedIp), eq(userAgent));
        }
    }
}