package com.developerchen.core.auth.converter;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

/**
 * 不透明令牌认证对象
 * 用于不透明令牌（UUID 类型）的认证
 * 
 * @author syc
 */
public class OpaqueTokenAuthenticationToken extends AbstractAuthenticationToken {
    
    private final String token;
    private final Object principal;
    
    /**
     * 创建未认证的令牌对象
     */
    public OpaqueTokenAuthenticationToken(String token) {
        super(null);
        this.token = token;
        this.principal = null;
        setAuthenticated(false);
    }
    
    /**
     * 创建已认证的令牌对象
     */
    public OpaqueTokenAuthenticationToken(String token, Object principal, Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.token = token;
        this.principal = principal;
        setAuthenticated(true);
    }
    
    @Override
    public Object getCredentials() {
        return token;
    }
    
    @Override
    public Object getPrincipal() {
        return principal;
    }
    
    /**
     * 获取令牌
     */
    public String getToken() {
        return token;
    }
}