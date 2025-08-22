package com.developerchen.core.auth.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * Spring Security 认证授权模块配置属性
 * 
 * @author syc
 */
@ConfigurationProperties(prefix = "security")
public class SecurityProperties {

    private Token token = new Token();
    private Session session = new Session();
    private RememberMe rememberMe = new RememberMe();

    public Token getToken() {
        return token;
    }

    public void setToken(Token token) {
        this.token = token;
    }

    public Session getSession() {
        return session;
    }

    public void setSession(Session session) {
        this.session = session;
    }

    public RememberMe getRememberMe() {
        return rememberMe;
    }

    public void setRememberMe(RememberMe rememberMe) {
        this.rememberMe = rememberMe;
    }

    /**
     * 令牌相关配置
     */
    public static class Token {
        /**
         * 访问令牌过期时间，默认30分钟
         */
        private Duration accessTokenExpiration = Duration.ofMinutes(30);
        
        /**
         * 刷新令牌过期时间，默认7天
         */
        private Duration refreshTokenExpiration = Duration.ofDays(7);
        
        /**
         * JWT 签名密钥
         */
        private String jwtSecret;
        
        /**
         * JWT 发行者，默认为 lore-core
         */
        private String jwtIssuer = "lore-core";

        public Duration getAccessTokenExpiration() {
            return accessTokenExpiration;
        }

        public void setAccessTokenExpiration(Duration accessTokenExpiration) {
            this.accessTokenExpiration = accessTokenExpiration;
        }

        public Duration getRefreshTokenExpiration() {
            return refreshTokenExpiration;
        }

        public void setRefreshTokenExpiration(Duration refreshTokenExpiration) {
            this.refreshTokenExpiration = refreshTokenExpiration;
        }

        public String getJwtSecret() {
            return jwtSecret;
        }

        public void setJwtSecret(String jwtSecret) {
            this.jwtSecret = jwtSecret;
        }

        public String getJwtIssuer() {
            return jwtIssuer;
        }

        public void setJwtIssuer(String jwtIssuer) {
            this.jwtIssuer = jwtIssuer;
        }
    }

    /**
     * 会话管理配置
     */
    public static class Session {
        /**
         * 是否启用有状态会话管理，默认启用
         */
        private boolean stateful = true;
        
        /**
         * 是否启用混合会话策略，默认禁用
         */
        private boolean hybrid = false;
        
        /**
         * Redis 会话命名空间
         */
        private String redisNamespace = "lore:session";

        public boolean isStateful() {
            return stateful;
        }

        public void setStateful(boolean stateful) {
            this.stateful = stateful;
        }

        public boolean isHybrid() {
            return hybrid;
        }

        public void setHybrid(boolean hybrid) {
            this.hybrid = hybrid;
        }

        public String getRedisNamespace() {
            return redisNamespace;
        }

        public void setRedisNamespace(String redisNamespace) {
            this.redisNamespace = redisNamespace;
        }
    }

    /**
     * 记住我功能配置
     */
    public static class RememberMe {
        /**
         * 记住我功能密钥
         */
        private String key = "lore-remember-me";
        
        /**
         * 记住我令牌有效期，默认14天
         */
        private Duration tokenValiditySeconds = Duration.ofDays(14);

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public Duration getTokenValiditySeconds() {
            return tokenValiditySeconds;
        }

        public void setTokenValiditySeconds(Duration tokenValiditySeconds) {
            this.tokenValiditySeconds = tokenValiditySeconds;
        }
    }
}