package com.developerchen.core.system.security;

import com.developerchen.core.domain.entity.User;
import org.apache.commons.codec.digest.HmacUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * 自定义令牌工具类
 * 实现简单的自定义令牌格式：username:expireTime:authorities:signature
 *
 * @author syc
 */
public final class CustomTokenUtil {
    private static final Logger logger = LoggerFactory.getLogger(CustomTokenUtil.class);

    private static String SECRET_KEY;

    public static Long EXPIRE_TIME;

    /**
     * 初始化密钥
     */
    public static void initSecretKey(String secretKey) {
        CustomTokenUtil.SECRET_KEY = secretKey;
    }

    /**
     * 获取用户名
     *
     * @param token 用户token
     * @return 用户名
     */
    public static String getUsernameFromToken(String token) {
        String[] parts = token.split(":");
        if (parts.length < 4) {
            throw new BadCredentialsException("Invalid token format");
        }
        return parts[0];
    }

    /**
     * 获取过期时间
     *
     * @param token 用户token
     * @return token的过期时间
     */
    public static Date getExpirationDateFromToken(String token) {
        String[] parts = token.split(":");
        if (parts.length < 4) {
            throw new BadCredentialsException("Invalid token format");
        }
        try {
            long expireTime = Long.parseLong(parts[1]);
            return Date.from(Instant.ofEpochMilli(expireTime));
        } catch (NumberFormatException e) {
            throw new BadCredentialsException("Invalid expiration time in token");
        }
    }

    /**
     * 获取GrantedAuthority实例的集合
     *
     * @param token 用户token
     * @return token中包含的权限的集合
     */
    public static Set<? extends GrantedAuthority> getAuthoritiesFromToken(String token) {
        String[] parts = token.split(":");
        if (parts.length < 4) {
            throw new BadCredentialsException("Invalid token format");
        }
        String authoritiesStr = parts[2];
        Set<SimpleGrantedAuthority> authorities = new HashSet<>();
        if (StringUtils.isNotEmpty(authoritiesStr)) {
            String[] authorityArray = authoritiesStr.split(",");
            for (String authority : authorityArray) {
                authorities.add(new SimpleGrantedAuthority(authority));
            }
        }
        return authorities;
    }

    /**
     * 验证令牌
     *
     * @param token 用户token
     */
    public static void validateToken(String token) {
        if (StringUtils.isEmpty(token)) {
            throw new BadCredentialsException("Empty token");
        }

        String[] parts = token.split(":");
        if (parts.length < 4) {
            throw new BadCredentialsException("Invalid token format");
        }

        // 验证签名
        String username = parts[0];
        String expireTimeStr = parts[1];
        String authoritiesStr = parts[2];
        String signature = parts[3];

        String expectedSignature = generateSignature(username, expireTimeStr, authoritiesStr);
        if (!expectedSignature.equals(signature)) {
            throw new BadCredentialsException("Invalid token signature");
        }

        // 验证过期时间
        try {
            long expireTime = Long.parseLong(expireTimeStr);
            if (System.currentTimeMillis() > expireTime) {
                throw new CredentialsExpiredException("Token has expired");
            }
        } catch (NumberFormatException e) {
            throw new BadCredentialsException("Invalid expiration time in token");
        }
    }

    /**
     * 验证令牌是否有效
     *
     * @param token       用户token
     * @param userDetails 用户详情
     * @return 是否有效
     */
    public static boolean validateToken(String token, UserDetails userDetails) {
        try {
            validateToken(token);
            String username = getUsernameFromToken(token);
            return username.equals(userDetails.getUsername());
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 通过ApiUser实例生成token
     *
     * @return token
     */
    public static String generateToken(ApiUser apiUser) {
        Collection<? extends GrantedAuthority> authorities = apiUser.getAuthorities();
        Set<String> roles = new HashSet<>();
        authorities.forEach(o -> roles.add(o.getAuthority()));

        return generateToken(apiUser.getUsername(), roles);
    }

    /**
     * 通过User实例生成token
     */
    public static String generateToken(User user) {
        ApiUser apiUser = ApiUser.ApiUserBuilder.build(user);
        return generateToken(apiUser);
    }

    /**
     * 生成自定义令牌
     *
     * @param username    用户名
     * @param authorities 权限集合
     * @return 令牌字符串
     */
    private static String generateToken(String username, Set<String> authorities) {
        // 计算过期时间
        long expireTime = System.currentTimeMillis() + (EXPIRE_TIME != null ? EXPIRE_TIME : 86400000); // 默认24小时

        // 拼接权限字符串
        String authoritiesStr = String.join(",", authorities);

        // 生成签名
        String expireTimeStr = String.valueOf(expireTime);
        String signature = generateSignature(username, expireTimeStr, authoritiesStr);

        // 拼接令牌
        return username + ":" + expireTimeStr + ":" + authoritiesStr + ":" + signature;
    }

    /**
     * 生成签名
     *
     * @param username       用户名
     * @param expireTimeStr  过期时间字符串
     * @param authoritiesStr 权限字符串
     * @return 签名
     */
    private static String generateSignature(String username, String expireTimeStr, String authoritiesStr) {
        String data = username + ":" + expireTimeStr + ":" + authoritiesStr;
        return HmacUtils.hmacSha256Hex(SECRET_KEY.getBytes(StandardCharsets.UTF_8), data.getBytes(StandardCharsets.UTF_8));
    }
}