package com.developerchen.core.auth.handler;

import com.developerchen.core.auth.config.SecurityProperties;
import com.developerchen.core.auth.service.RefreshTokenService;
import com.developerchen.core.auth.service.TokenPair;
import com.developerchen.core.auth.service.TokenService;
import com.developerchen.core.auth.service.TokenType;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * API 认证成功处理器
 * 处理 API 登录成功后的令牌生成和 JSON 响应
 * 
 * @author syc
 */
@Component
public class ApiAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final TokenService tokenService;
    private final RefreshTokenService refreshTokenService;
    private final SecurityProperties securityProperties;
    private final ObjectMapper objectMapper;
    
    @Autowired(required = false)
    private JwtEncoder jwtEncoder;

    public ApiAuthenticationSuccessHandler(
            TokenService tokenService,
            RefreshTokenService refreshTokenService,
            SecurityProperties securityProperties,
            ObjectMapper objectMapper) {
        this.tokenService = tokenService;
        this.refreshTokenService = refreshTokenService;
        this.securityProperties = securityProperties;
        this.objectMapper = objectMapper;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                      Authentication authentication) throws IOException, ServletException {
        
        String username = authentication.getName();
        
        // 确定令牌类型偏好
        TokenType preferredType = determineTokenType(request);
        
        // 生成访问令牌
        String accessToken = generateAccessToken(username, preferredType, authentication);
        
        // 生成刷新令牌
        String refreshToken = refreshTokenService.generateRefreshToken(username);
        
        // 计算过期时间（秒）
        long expiresIn = securityProperties.getToken().getAccessTokenExpiration().toSeconds();
        
        // 构建响应数据
        Map<String, Object> responseData = new HashMap<>();
        responseData.put("access_token", accessToken);
        responseData.put("refresh_token", refreshToken);
        responseData.put("token_type", preferredType.name().toLowerCase());
        responseData.put("expires_in", expiresIn);
        responseData.put("timestamp", Instant.now().toString());
        
        // 设置响应头
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        
        // 写入 JSON 响应
        objectMapper.writeValue(response.getWriter(), responseData);
    }

    /**
     * 生成访问令牌
     * 
     * @param username 用户名
     * @param tokenType 令牌类型
     * @param authentication 认证信息
     * @return 生成的访问令牌
     */
    private String generateAccessToken(String username, TokenType tokenType, Authentication authentication) {
        if (tokenType == TokenType.JWT && jwtEncoder != null) {
            return generateJwtToken(username, authentication);
        } else {
            // 对于 UUID 类型或 JWT 编码器不可用时，使用 TokenService
            return tokenService.generateAccessToken(username, TokenType.UUID);
        }
    }
    
    /**
     * 生成 JWT 令牌
     * 
     * @param username 用户名
     * @param authentication 认证信息
     * @return JWT 令牌字符串
     */
    private String generateJwtToken(String username, Authentication authentication) {
        Instant now = Instant.now();
        Instant expiration = now.plus(securityProperties.getToken().getAccessTokenExpiration());
        
        // 提取用户权限
        String authorities = authentication.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .collect(Collectors.joining(","));
        
        // 构建 JWT 声明
        JwtClaimsSet.Builder claimsBuilder = JwtClaimsSet.builder()
            .issuer(securityProperties.getToken().getJwtIssuer())
            .subject(username)
            .issuedAt(now)
            .expiresAt(expiration)
            .claim("authorities", authorities);
        
        // 添加自定义声明
        addCustomClaims(claimsBuilder, authentication);
        
        JwtClaimsSet claims = claimsBuilder.build();
        
        // 创建 JWT 编码参数
        JwtEncoderParameters parameters = JwtEncoderParameters.from(
            JwsHeader.with(SignatureAlgorithm.RS256).build(),
            claims
        );
        
        // 编码 JWT
        Jwt jwt = jwtEncoder.encode(parameters);
        
        // 将 JWT 令牌存储到 TokenService 中以支持令牌验证和管理
        tokenService.storeToken(jwt.getTokenValue(), username, expiration);
        
        return jwt.getTokenValue();
    }
    
    /**
     * 添加自定义 JWT 声明
     * 
     * @param claimsBuilder 声明构建器
     * @param authentication 认证信息
     */
    private void addCustomClaims(JwtClaimsSet.Builder claimsBuilder, Authentication authentication) {
        // 添加用户类型信息（如果可用）
        if (authentication.getPrincipal() instanceof org.springframework.security.core.userdetails.UserDetails userDetails) {
            claimsBuilder.claim("username", userDetails.getUsername());
            claimsBuilder.claim("enabled", userDetails.isEnabled());
            claimsBuilder.claim("account_non_expired", userDetails.isAccountNonExpired());
            claimsBuilder.claim("account_non_locked", userDetails.isAccountNonLocked());
            claimsBuilder.claim("credentials_non_expired", userDetails.isCredentialsNonExpired());
        }
        
        // 添加认证时间
        claimsBuilder.claim("auth_time", Instant.now().getEpochSecond());
        
        // 添加令牌类型标识
        claimsBuilder.claim("token_type", "access_token");
    }

    /**
     * 根据请求确定令牌类型偏好
     * 
     * @param request HTTP 请求
     * @return 令牌类型
     */
    private TokenType determineTokenType(HttpServletRequest request) {
        // 检查 Accept 头
        String acceptHeader = request.getHeader("Accept");
        if (acceptHeader != null && acceptHeader.contains("application/jwt")) {
            return TokenType.JWT;
        }
        
        // 检查自定义参数
        String tokenTypeParam = request.getParameter("token_type");
        if ("jwt".equalsIgnoreCase(tokenTypeParam)) {
            return TokenType.JWT;
        }
        
        // 检查自定义头
        String tokenTypeHeader = request.getHeader("X-Token-Type");
        if ("jwt".equalsIgnoreCase(tokenTypeHeader)) {
            return TokenType.JWT;
        }
        
        // 默认返回 UUID 类型
        return TokenType.UUID;
    }
}