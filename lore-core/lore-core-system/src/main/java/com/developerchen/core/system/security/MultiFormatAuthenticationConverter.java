package com.developerchen.core.system.security;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimNames;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.Assert;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * 多格式认证转换器，支持JWT和自定义令牌格式
 * 根据令牌类型选择适当的转换方式
 *
 * @author syc
 */
public class MultiFormatAuthenticationConverter implements Converter<Object, AbstractAuthenticationToken> {

    private static final Logger logger = LoggerFactory.getLogger(MultiFormatAuthenticationConverter.class);
    
    private final MultiFormatTokenResolver tokenResolver;
    private Converter<Jwt, Collection<GrantedAuthority>> jwtGrantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
    private String principalClaimName = JwtClaimNames.SUB;
    private final WebAuthenticationDetailsSource authenticationDetailsSource = new WebAuthenticationDetailsSource();

    public MultiFormatAuthenticationConverter(MultiFormatTokenResolver tokenResolver) {
        Assert.notNull(tokenResolver, "tokenResolver cannot be null");
        this.tokenResolver = tokenResolver;
    }

    @Override
    public AbstractAuthenticationToken convert(Object source) {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String token = null;
        
        if (source instanceof String) {
            token = (String) source;
        } else if (source instanceof Jwt) {
            return convertJwt((Jwt) source);
        } else {
            throw new IllegalArgumentException("Unsupported token type: " + source.getClass().getName());
        }
        
        String tokenType = tokenResolver.resolveTokenType(request, token);
        
        if ("jwt".equals(tokenType)) {
            // 处理JWT令牌
            try {
                // 这里应该使用JwtDecoder解析JWT，但为了简化示例，我们直接使用现有的JwtTokenUtil
                String username = JwtTokenUtil.getUsernameFromToken(token);
                Set<GrantedAuthority> authorities = (Set<GrantedAuthority>) JwtTokenUtil.getAuthoritiesFromToken(token);
                
                UserAuthenticationToken authentication = new UserAuthenticationToken(username, null, authorities);
                authentication.setDetails(authenticationDetailsSource.buildDetails(request));
                return authentication;
            } catch (Exception e) {
                logger.error("JWT令牌解析失败", e);
                throw e;
            }
        } else {
            // 处理自定义令牌
            try {
                // 这里应该实现自定义令牌的解析逻辑
                // 示例实现，实际项目中需要根据自定义令牌的格式进行解析
                String username = extractUsernameFromCustomToken(token);
                Set<GrantedAuthority> authorities = extractAuthoritiesFromCustomToken(token);
                
                UserAuthenticationToken authentication = new UserAuthenticationToken(username, null, authorities);
                authentication.setDetails(authenticationDetailsSource.buildDetails(request));
                return authentication;
            } catch (Exception e) {
                logger.error("自定义令牌解析失败", e);
                throw e;
            }
        }
    }
    
    private AbstractAuthenticationToken convertJwt(Jwt jwt) {
        Collection<GrantedAuthority> authorities = this.jwtGrantedAuthoritiesConverter.convert(jwt);
        String principalClaimValue = jwt.getClaimAsString(this.principalClaimName);
        return new JwtAuthenticationToken(jwt, authorities, principalClaimValue);
    }
    
    /**
     * 从自定义令牌中提取用户名
     * 实际项目中需要根据自定义令牌的格式实现
     */
    private String extractUsernameFromCustomToken(String token) {
        // 示例实现，实际项目中需要根据自定义令牌的格式进行解析
        // 这里假设自定义令牌格式为：username:timestamp:signature
        String[] parts = token.split(":");
        if (parts.length >= 1) {
            return parts[0];
        }
        throw new IllegalArgumentException("Invalid custom token format");
    }
    
    /**
     * 从自定义令牌中提取权限
     * 实际项目中需要根据自定义令牌的格式实现
     */
    private Set<GrantedAuthority> extractAuthoritiesFromCustomToken(String token) {
        // 示例实现，实际项目中需要根据自定义令牌的格式进行解析
        // 这里简单返回一个基本角色
        Set<GrantedAuthority> authorities = new HashSet<>();
        authorities.add(new SimpleGrantedAuthority("USER"));
        return authorities;
    }

    public void setJwtGrantedAuthoritiesConverter(Converter<Jwt, Collection<GrantedAuthority>> jwtGrantedAuthoritiesConverter) {
        Assert.notNull(jwtGrantedAuthoritiesConverter, "jwtGrantedAuthoritiesConverter cannot be null");
        this.jwtGrantedAuthoritiesConverter = jwtGrantedAuthoritiesConverter;
    }

    public void setPrincipalClaimName(String principalClaimName) {
        Assert.hasText(principalClaimName, "principalClaimName cannot be empty");
        this.principalClaimName = principalClaimName;
    }
}