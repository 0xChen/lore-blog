package com.developerchen.core.util;

import com.developerchen.core.domain.entity.User;
import com.developerchen.core.security.JwtAuthenticationToken;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 用户工具类
 * 获取登陆用户信息, 客户端IP, User-Agent等...
 *
 * @author syc
 */
public class UserUtils {

    /**
     * 通过Spring Security获取当前登陆用户信息
     *
     * @return 用户信息
     * @author syc
     */
    public static User getUser() {
        SecurityContext ctx = SecurityContextHolder.getContext();
        Authentication authentication = ctx.getAuthentication();
        if (authentication instanceof JwtAuthenticationToken) {
            return ((JwtAuthenticationToken) ctx.getAuthentication()).getUser();
        }
        return null;
    }

    /**
     * 获取当前登陆用户ID
     */
    public static Long getUserId() {
        User user = getUser();
        return user != null ? user.getId() : null;
    }

    public static String getRemoteIp() {
        return getRemoteIp(null);
    }

    /**
     * 通过request获取调用者的IP
     * 会以请求头中的key "X-Forwarded-For"判断是否有反向代理以获取真实IP
     *
     * @param request the current request
     * @return real ip
     */
    public static String getRemoteIp(HttpServletRequest request) {
        String separatorChar = ",";
        request = RequestUtils.getRequest(request);
        if (request == null) {
            return null;
        }
        String ip = request.getHeader("X-Forwarded-For");
        if (StringUtils.isNotBlank(ip)) {
            if (StringUtils.contains(ip, separatorChar)) {
                ip = ip.split(separatorChar)[0];
            }
        } else {
            ip = request.getRemoteAddr();
        }
        return ip;
    }

    public static String getUserAgent() {
        return getUserAgent(null);
    }

    /**
     * 通过request获取User-Agent
     *
     * @param request the current request
     * @return agent
     */
    public static String getUserAgent(HttpServletRequest request) {
        request = RequestUtils.getRequest(request);
        return request != null ? request.getHeader("User-Agent") : null;
    }
}
