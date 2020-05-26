package com.developerchen.core.security;

import com.developerchen.core.config.AppConfig;
import com.developerchen.core.constant.Const;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationDetailsSource;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserCache;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.cache.NullUserCache;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Processes a HTTP request's JWT authorization headers, putting the result into the
 * <code>SecurityContextHolder</code>.
 * <p>
 * If authentication is successful, the resulting
 * {@link org.springframework.security.core.Authentication Authentication} object will be
 * placed into the <code>SecurityContextHolder</code>.
 * <p>
 *
 * @author syc
 */
public class JwtAuthorizationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthorizationFilter.class);

    private final AntPathRequestMatcher staticRequestPattern =
            new AntPathRequestMatcher(AppConfig.staticPathPattern);


    /**
     * The default key under which the JwtToken be stored in the http header.
     */
    public static final String TOKEN_HEADER = "Authorization";
    public static final String TOKEN_PREFIX = "Bearer ";

    private final UserDetailsService userDetailsServiceImpl;

    public JwtAuthorizationFilter(UserDetailsService userDetailsServiceImpl) {
        this.userDetailsServiceImpl = userDetailsServiceImpl;
    }

    private AuthenticationDetailsSource<HttpServletRequest, ?> authenticationDetailsSource = new WebAuthenticationDetailsSource();
    private UserCache userCache = new NullUserCache();

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        if (staticRequestPattern.matches(request)) {
            // 静态资源
            chain.doFilter(request, response);
            return;
        }

        String header = request.getHeader(TOKEN_HEADER);
        String cookieToken = null;

        if (header == null
                || !header.startsWith(TOKEN_PREFIX)
                || header.length() == TOKEN_PREFIX.length()) {

            // request无token后判断cookie中是否有token
            Cookie[] cookies = request.getCookies();
            Map<String, String> cookieMap = cookieToMap(cookies);
            cookieToken = cookieMap.get(Const.COOKIE_ACCESS_TOKEN);

            if (cookieToken == null) {
                chain.doFilter(request, response);
                return;
            }
        }

        String token = cookieToken != null ? cookieToken : header.replaceFirst(TOKEN_PREFIX, "");

        if (logger.isDebugEnabled()) {
            logger.debug("Jwt Authorization token received from user agent: {}", token);
        }

        try {
            JwtTokenUtil.validateToken(token);
        } catch (Exception e) {
            chain.doFilter(request, response);
            return;
        }

        String username = JwtTokenUtil.getUsernameFromToken(token);
        UserDetails userDetails = userCache.getUserFromCache(username);
        if (userDetails == null) {
            // 如果想真正的stateless, 则直接将token转换成UserDetails
            userDetails = this.userDetailsServiceImpl.loadUserByUsername(username);
        }
        if (!JwtTokenUtil.validateToken(token, userDetails)) {
            // 再次验证token是否失效, 如果数据库中该user的信息在签发token后修改过则会导致token失效
            chain.doFilter(request, response);
            return;
        }
        JwtAuthenticationToken authentication = new JwtAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        authentication.setDetails(this.authenticationDetailsSource.buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        Long userId = ((JwtUser) userDetails).getId();
        request.setAttribute(Const.REQUEST_USER_ID, userId);
        request.setAttribute(Const.REQUEST_USER_NAME, username);
        chain.doFilter(request, response);
    }

    private Map<String, String> cookieToMap(Cookie[] cookies) {
        Map<String, String> cookieMap = new HashMap<>(16);
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                cookieMap.put(cookie.getName(), cookie.getValue());
            }
        }
        return cookieMap;
    }
}
