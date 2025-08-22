package com.developerchen.core.system.config;

import com.developerchen.core.system.security.CustomTokenUtil;
import com.developerchen.core.system.security.JwtTokenUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;

/**
 * 令牌配置类
 * 用于初始化JWT和自定义令牌的配置
 *
 * @author syc
 */
@Configuration
public class TokenConfig {

    /**
     * JWT令牌过期时间（毫秒）
     */
    @Value("${jwt.expire-time:86400000}")
    private Long jwtExpireTime;

    /**
     * JWT密钥字符串，当无法从文件读取时使用
     */
    @Value("${jwt.secret-key:WoQu-@Nian~Mai$Le%#GeDa^Jin#Biao}")
    private String jwtSecretKeyStr;

    /**
     * JWT密钥文件路径
     */
    @Value("${jwt.secret-key-path:}")
    private String jwtSecretKeyPath;

    /**
     * 自定义令牌过期时间（毫秒）
     */
    @Value("${custom.token.expire-time:86400000}")
    private Long customTokenExpireTime;

    /**
     * 自定义令牌密钥
     */
    @Value("${custom.token.secret-key:CustomTokenSecretKey-@#$%^&*()}")
    private String customTokenSecretKey;

    /**
     * 初始化令牌配置
     */
    @PostConstruct
    public void init() {
        // 初始化JWT配置
        JwtTokenUtil.EXPIRE_TIME = jwtExpireTime;
        JwtTokenUtil.SECRET_KEY_PATH = jwtSecretKeyPath;
        JwtTokenUtil.SECRET_KEY_STR = jwtSecretKeyStr;
        JwtTokenUtil.initSecretKey();

        // 初始化自定义令牌配置
        CustomTokenUtil.EXPIRE_TIME = customTokenExpireTime;
        CustomTokenUtil.initSecretKey(customTokenSecretKey);
    }
}