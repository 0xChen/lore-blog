package com.developerchen.core.util;

import com.developerchen.core.domain.entity.User;
import com.developerchen.core.security.JwtAuthenticationToken;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

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
        request = getRequest(request);
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
        request = getRequest(request);
        return request != null ? request.getHeader("User-Agent") : null;
    }

    /**
     * 获取request中的URL
     *
     * @return URL String
     */
    public static String getRequestURI() {
        return getRequestURI(null);
    }

    /**
     * 获取request中的URL
     *
     * @param request the current request
     * @return URL
     */
    public static String getRequestURI(HttpServletRequest request) {
        request = getRequest(request);
        return request != null ? request.getRequestURI() : null;
    }

    /**
     * 获取request中URL的查询参数
     *
     * @return URL中的查询参数
     */
    public static String getRequestQueryString() {
        return getRequestQueryString(null);
    }

    /**
     * 获取request中URL的查询参数
     *
     * @param request the current request
     * @return URL中的查询参数
     */
    public static String getRequestQueryString(HttpServletRequest request) {
        request = getRequest(request);
        return request != null ? request.getQueryString() : null;
    }

    /**
     * 获取request请求方法(get, post, ...)
     *
     * @return 请求方法
     */
    public static String getRequestMethod() {
        return getRequestMethod(null);
    }

    /**
     * 获取request请求方法(get, post, ...)
     *
     * @param request the current request
     * @return 请求方法
     */
    public static String getRequestMethod(HttpServletRequest request) {
        request = getRequest(request);
        return request != null ? request.getMethod() : null;
    }

    public static HttpServletRequest getRequest(HttpServletRequest request) {
        if (request == null) {
            ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (requestAttributes != null) {
                request = requestAttributes.getRequest();
            }
        }
        return request;
    }
}
