package com.developerchen.core.auth.filter;

import com.developerchen.core.auth.config.SessionStrategySelector;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * 混合会话创建过滤器
 * 根据请求类型动态设置会话创建策略
 * 
 * @author syc
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class HybridSessionCreationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(HybridSessionCreationFilter.class);
    private static final String SESSION_STRATEGY_ATTRIBUTE = "SESSION_STRATEGY";
    
    private final SessionStrategySelector sessionStrategySelector;

    public HybridSessionCreationFilter(SessionStrategySelector sessionStrategySelector) {
        this.sessionStrategySelector = sessionStrategySelector;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                  HttpServletResponse response, 
                                  FilterChain filterChain) throws ServletException, IOException {
        
        // 确定会话策略
        boolean useStateless = sessionStrategySelector.shouldUseStatelessSession(request);
        
        // 在请求属性中标记会话策略，供后续过滤器使用
        request.setAttribute(SESSION_STRATEGY_ATTRIBUTE, useStateless ? "STATELESS" : "STATEFUL");
        
        if (useStateless) {
            logger.debug("使用无状态会话策略处理请求: {}", request.getRequestURI());
            // 对于无状态请求，确保不创建会话
            processStatelessRequest(request, response, filterChain);
        } else {
            logger.debug("使用有状态会话策略处理请求: {}", request.getRequestURI());
            // 对于有状态请求，允许正常的会话管理
            filterChain.doFilter(request, response);
        }
    }

    /**
     * 处理无状态请求
     * 确保不会创建HTTP会话
     */
    private void processStatelessRequest(HttpServletRequest request, 
                                       HttpServletResponse response, 
                                       FilterChain filterChain) throws ServletException, IOException {
        
        // 包装请求，阻止会话创建
        StatelessHttpServletRequestWrapper wrappedRequest = new StatelessHttpServletRequestWrapper(request);
        
        try {
            filterChain.doFilter(wrappedRequest, response);
        } catch (Exception e) {
            logger.error("处理无状态请求时发生错误: {}", request.getRequestURI(), e);
            throw e;
        }
    }

    /**
     * 无状态HTTP请求包装器
     * 阻止会话创建
     */
    private static class StatelessHttpServletRequestWrapper extends jakarta.servlet.http.HttpServletRequestWrapper {
        
        public StatelessHttpServletRequestWrapper(HttpServletRequest request) {
            super(request);
        }

        @Override
        public jakarta.servlet.http.HttpSession getSession() {
            // 无状态模式下不创建会话
            return null;
        }

        @Override
        public jakarta.servlet.http.HttpSession getSession(boolean create) {
            if (create) {
                // 无状态模式下即使要求创建会话也返回null
                return null;
            }
            return super.getSession(false);
        }

        @Override
        public String getRequestedSessionId() {
            // 无状态模式下没有会话ID
            return null;
        }

        @Override
        public boolean isRequestedSessionIdValid() {
            // 无状态模式下会话ID总是无效的
            return false;
        }

        @Override
        public boolean isRequestedSessionIdFromCookie() {
            return false;
        }

        @Override
        public boolean isRequestedSessionIdFromURL() {
            return false;
        }
    }
}