package com.developerchen.core.auth.provider;

import com.developerchen.core.auth.converter.OpaqueTokenAuthenticationToken;
import com.developerchen.core.auth.service.TokenService;
import com.developerchen.core.auth.service.TokenValidationResult;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.util.Assert;

/**
 * 不透明令牌认证提供者
 * 验证 UUID 类型的不透明令牌并获取用户信息
 * 
 * @author syc
 */
public class OpaqueTokenAuthenticationProvider implements AuthenticationProvider {
    
    private final TokenService tokenService;
    private final UserDetailsService userDetailsService;
    
    public OpaqueTokenAuthenticationProvider(TokenService tokenService, UserDetailsService userDetailsService) {
        Assert.notNull(tokenService, "TokenService cannot be null");
        Assert.notNull(userDetailsService, "UserDetailsService cannot be null");
        this.tokenService = tokenService;
        this.userDetailsService = userDetailsService;
    }
    
    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        OpaqueTokenAuthenticationToken token = (OpaqueTokenAuthenticationToken) authentication;
        String opaqueToken = token.getToken();
        
        // 验证令牌
        TokenValidationResult validationResult = tokenService.validateToken(opaqueToken);
        
        if (!validationResult.valid()) {
            throw new BadCredentialsException("Invalid token: " + validationResult.errorMessage());
        }
        
        // 获取用户详情
        UserDetails userDetails;
        try {
            userDetails = userDetailsService.loadUserByUsername(validationResult.username());
        } catch (UsernameNotFoundException ex) {
            throw new BadCredentialsException("User not found: " + validationResult.username(), ex);
        }
        
        // 创建已认证的令牌对象
        return new OpaqueTokenAuthenticationToken(
            opaqueToken,
            userDetails,
            userDetails.getAuthorities()
        );
    }
    
    @Override
    public boolean supports(Class<?> authentication) {
        return OpaqueTokenAuthenticationToken.class.isAssignableFrom(authentication);
    }
}