package com.developerchen.core.security;

import com.developerchen.core.config.AppConfig;
import com.developerchen.core.domain.entity.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.ResourceUtils;

import javax.crypto.SecretKey;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;

/**
 * JWT 工具类
 *
 * @author syc
 */
public final class JwtTokenUtil {
    private static final Logger logger = LoggerFactory.getLogger(JwtTokenUtil.class);

    private static SecretKey SECRET_KEY;

    public static Long EXPIRE_TIME;

    public static String SECRET_KEY_PATH;


    public static void initSecretKey() {
        SecretKey tempSecretKey;
        try {
            if (StringUtils.isBlank(SECRET_KEY_PATH)) {
                SECRET_KEY_PATH = AppConfig.HOME_PATH + File.separator + "jwtSecretKey";
            }

            File secretKeyFile = ResourceUtils.getFile(SECRET_KEY_PATH);
            StringBuilder secretKey = new StringBuilder();
            for (String line : Files.readAllLines(secretKeyFile.toPath())) {
                secretKey.append(line);
            }
            tempSecretKey = Keys.hmacShaKeyFor(secretKey.toString()
                    .getBytes(StandardCharsets.UTF_8));

        } catch (Exception e) {
            // 随机一个
            tempSecretKey = Keys.secretKeyFor(SignatureAlgorithm.HS256);
            logger.error(e.toString());
            logger.info("使用随机密钥");
        }
        JwtTokenUtil.SECRET_KEY = tempSecretKey;
    }

    /**
     * 获取用户名
     *
     * @param token 用户token
     * @return 用户名
     */
    public static String getUsernameFromToken(String token) {
        return getClaimsFromToken(token).getSubject();
    }

    /**
     * 获取签发时间
     *
     * @param token 用户token
     * @return token的签发日期
     */
    public static Date getIssuedAtDateFromToken(String token) {
        return getClaimsFromToken(token).getIssuedAt();
    }

    /**
     * 获取过期时间
     *
     * @param token 用户token
     * @return token的过期时间
     */
    public static Date getExpirationDateFromToken(String token) {
        return getClaimsFromToken(token).getExpiration();
    }

    /**
     * 获取email
     *
     * @param token 用户token
     * @return 用户email
     */
    public static String getEmailFromToken(String token) {
        return getClaimsFromToken(token).get("email").toString();
    }

    /**
     * 获取用户状态
     *
     * @param token 用户token
     * @return 用户状态
     */
    public static String getStatusFromToken(String token) {
        return getClaimsFromToken(token).get("status").toString();
    }

    /**
     * 获取昵称
     *
     * @param token 用户token
     * @return 用户昵称
     */
    public static String getNicknameFromToken(String token) {
        return getClaimsFromToken(token).get("nickname").toString();
    }

    /**
     * 获取GrantedAuthority实例的集合
     * 根据token中的authorities构造GrantedAuthority实例的集合并返回
     *
     * @param token 用户token
     * @return token中包含的权限的集合
     */
    @SuppressWarnings("unchecked")
    public static Set<? extends GrantedAuthority> getAuthoritiesFromToken(String token) {
        List<String> authorityList = (List<String>) getClaimsFromToken(token).get("authorities");
        Set<SimpleGrantedAuthority> authorities = new HashSet<>();
        authorityList.forEach(role -> authorities.add(new SimpleGrantedAuthority(role)));
        return authorities;
    }

    /**
     * 取出token中指定key所对应的值对象
     *
     * @param token 用户token
     * @param key
     * @return key对应的值对象
     */
    public static Object getObjectFromTokenByKey(String token, String key) {
        return getClaimsFromToken(token).get(key);
    }

    /**
     * 解析token中的Claims并返回
     *
     * @param token 用户token
     * @return Claims
     */
    private static Claims getClaimsFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(SECRET_KEY)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * 通过JwtUser实例生产token
     *
     * @return token
     */
    public static String generateToken(JwtUser jwtUser) {
        Map<String, Object> claims = new LinkedHashMap<>(16);
        Collection<? extends GrantedAuthority> authorities = jwtUser.getAuthorities();
        Set<String> roles = new HashSet<>();
        authorities.forEach(o -> roles.add(o.getAuthority()));

        claims.put("id", jwtUser.getId());
        claims.put("nickname", jwtUser.getNickname());
        claims.put("status", jwtUser.getStatus());
        claims.put("email", jwtUser.getEmail());
        claims.put("authorities", roles);
        return generateToken(claims, jwtUser.getUsername());
    }

    /**
     * 通过User实例生产token
     */
    public static String generateToken(User user) {
        JwtUser jwtUser = JwtUser.JwtUserBuilder.build(user);
        return generateToken(jwtUser);
    }

    private static String generateToken(Map<String, Object> claims, String subject) {
        Date createdDate = new Date();
        Date expirationDate = calculateExpirationDate(createdDate);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(createdDate)
                .setExpiration(expirationDate)
                .signWith(SECRET_KEY)
                .compact();
    }

    /**
     * 根据已有token生成一个新的token, 相当于续签
     *
     * @param token 用户token
     * @return token
     */
    public static String refreshToken(String token) {
        Date createdDate = new Date();
        Date expirationDate = calculateExpirationDate(createdDate);

        final Claims claims = getClaimsFromToken(token);
        claims.setIssuedAt(createdDate);
        claims.setExpiration(expirationDate);

        return Jwts.builder()
                .setClaims(claims)
                .signWith(SECRET_KEY)
                .compact();
    }

    /**
     * 根据token中记录的到期时间来验证token是否失效
     *
     * @param token 用户token
     * @return true 失效, false 未失效
     */
    private static Boolean isTokenExpired(String token) {
        Date expiration = getExpirationDateFromToken(token);
        return expiration.before(new Date());
    }

    /**
     * 验证token是否合法, 如果用户在签发token之后修改了任何该用户的数据都将导致相应的token失效
     * (updateTime属性记录了最后一次更新user数据的时间)
     *
     * @param token       用户token
     * @param userDetails 用户信息
     * @return true 通过, false 失败
     */
    public static Boolean validateToken(String token, UserDetails userDetails) {
        Date updateTime = ((JwtUser) userDetails).getUpdateTime();
        // 数据库中日期精确到ms, token中只精确到s, 比对前将数据库中日期毫秒位值0
        updateTime.setTime(updateTime.getTime() / 1000);
        return !updateTime.after(getIssuedAtDateFromToken(token));
    }

    /**
     * 利用解析方法实现验证token
     *
     * @param token 用户token
     * @return true 通过验证, 验证失败则抛出异常
     */
    public static boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(SECRET_KEY).build().parseClaimsJws(token);
            return true;
        } catch (UnsupportedJwtException
                 | MalformedJwtException
                 | IllegalArgumentException e) {
            if (logger.isDebugEnabled()) {
                logger.debug("Invalid JWT Token: {}", token);
            }
            throw new BadCredentialsException("Invalid JWT token: {}", e);
        } catch (ExpiredJwtException e) {
            if (logger.isDebugEnabled()) {
                logger.debug("Invalid JWT Token: {}", token);
            }
            throw new CredentialsExpiredException("JWT Token expired", e);
        }
    }

    /**
     * 生成token到期日期
     *
     * @param createdDate 起始日期
     * @return 到期日期
     */
    private static Date calculateExpirationDate(Date createdDate) {
        return new Date(createdDate.getTime() + EXPIRE_TIME);
    }

    public static JwtUser parseTokenToJwtUser(String token) {
        Claims claims = getClaimsFromToken(token);
        return new JwtUser(
                claims.get("id", Long.class),
                claims.getSubject(),
                claims.get("nickname", String.class),
                null,
                claims.get("status", String.class),
                claims.get("email", String.class),
                null,
                getAuthoritiesFromToken(token));
    }

}

