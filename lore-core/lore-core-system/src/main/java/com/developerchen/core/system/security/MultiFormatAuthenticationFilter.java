package com.developerchen.core.system.security;

import com.developerchen.core.system.config.SystemConfig;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationDetailsSource;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserCache;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.cache.NullUserCache;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * 多格式认证过滤器
 * 支持JWT和自定义令牌格式的认证
 *
 * @author syc
 */
public class MultiFormatAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(MultiFormatAuthenticationFilter.class);

    private final AntPathRequestMatcher staticRequestPattern =
            new AntPathRequestMatcher(SystemConfig.staticPathPattern);

    private final MultiFormatAuthenticationConverter authenticationConverter;
    private final MultiFormatTokenResolver tokenResolver;
    private UserDetailsService userDetailsService;

    private AuthenticationDetailsSource<HttpServletRequest, ?> authenticationDetailsSource = new WebAuthenticationDetailsSource();
    private final UserCache userCache = new NullUserCache();

    public MultiFormatAuthenticationFilter(MultiFormatAuthenticationConverter authenticationConverter,
                                          MultiFormatTokenResolver tokenResolver) {
        this.authenticationConverter = authenticationConverter;
        this.tokenResolver = tokenResolver;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        // 跳过静态资源
        if (staticRequestPattern.matches(request)) {
            chain.doFilter(request, response);
            return;
        }

        // 解析令牌
        String token = tokenResolver.resolve(request);
        if (token == null) {
            chain.doFilter(request, response);
            return;
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Token received from user agent: {}", token);
        }

        try {
            // 获取令牌类型
            String tokenType = tokenResolver.resolveTokenType(request, token);
            
            // 根据令牌类型进行验证
            if ("jwt".equals(tokenType)) {
                processJwtToken(request, token);
            } else {
                processCustomToken(request, token);
            }
        } catch (Exception e) {
            logger.error("Token authentication failed", e);
        }

        chain.doFilter(request, response);
    }

    /**
     * 处理JWT令牌
     */
    private void processJwtToken(HttpServletRequest request, String token) {
        try {
            // 验证JWT令牌
            JwtTokenUtil.validateToken(token);
            
            // 获取用户信息
            String username = JwtTokenUtil.getUsernameFromToken(token);
            UserDetails userDetails = userCache.getUserFromCache(username);
            if (userDetails == null && userDetailsService != null) {
                userDetails = userDetailsService.loadUserByUsername(username);
            }
            
            // 再次验证令牌
            if (userDetails != null && JwtTokenUtil.validateToken(token, userDetails)) {
                // 创建认证对象并设置到安全上下文
                Authentication authentication = authenticationConverter.convert(token);
                if (authentication != null) {
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        } catch (Exception e) {
            logger.error("JWT token processing failed", e);
        }
    }

    /**
     * 处理自定义令牌
     */
    private void processCustomToken(HttpServletRequest request, String token) {
        try {
            // 验证自定义令牌
            CustomTokenUtil.validateToken(token);
            
            // 获取用户信息
            String username = CustomTokenUtil.getUsernameFromToken(token);
            UserDetails userDetails = userCache.getUserFromCache(username);
            if (userDetails == null && userDetailsService != null) {
                userDetails = userDetailsService.loadUserByUsername(username);
            }
            
            // 再次验证令牌
            if (userDetails != null && CustomTokenUtil.validateToken(token, userDetails)) {
                // 创建认证对象并设置到安全上下文
                Authentication authentication = authenticationConverter.convert(token);
                if (authentication != null) {
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        } catch (Exception e) {
            logger.error("Custom token processing failed", e);
        }
    }

    public void setUserDetailsService(UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    public void setAuthenticationDetailsSource(AuthenticationDetailsSource<HttpServletRequest, ?> authenticationDetailsSource) {
        this.authenticationDetailsSource = authenticationDetailsSource;
    }
}