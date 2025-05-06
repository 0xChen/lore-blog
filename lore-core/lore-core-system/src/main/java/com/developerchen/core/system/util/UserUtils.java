package com.developerchen.core.system.util;

import com.developerchen.core.domain.entity.User;
import com.developerchen.core.system.security.UserAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

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
        if (authentication instanceof UserAuthenticationToken) {
            return ((UserAuthenticationToken) ctx.getAuthentication()).getUser();
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
}
