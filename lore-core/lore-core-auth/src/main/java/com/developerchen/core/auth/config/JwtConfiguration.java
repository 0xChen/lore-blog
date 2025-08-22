package com.developerchen.core.auth.config;

import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.concurrent.ConcurrentHashMap;

/**
 * JWT 编码解码器配置类
 * 
 * @author syc
 */
@Configuration
@ConditionalOnProperty(name = "security.token.jwt-secret")
public class JwtConfiguration {
    
    private static final ConcurrentHashMap<String, KeyPair> keyPairCache = new ConcurrentHashMap<>();
    
    /**
     * JWT 编码器配置
     * 使用 RSA 算法进行签名
     */
    @Bean
    public JwtEncoder jwtEncoder(SecurityProperties securityProperties) {
        JWKSource<SecurityContext> jwkSource = jwkSource(securityProperties);
        return new NimbusJwtEncoder(jwkSource);
    }
    
    /**
     * JWT 解码器配置
     * 使用相同的 RSA 公钥进行验证
     */
    @Bean
    public JwtDecoder jwtDecoder(SecurityProperties securityProperties) {
        KeyPair keyPair = getOrCreateKeyPair(securityProperties.getToken().getJwtSecret());
        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
        
        return NimbusJwtDecoder.withPublicKey(publicKey)
            .signatureAlgorithm(SignatureAlgorithm.RS256)
            .build();
    }
    
    /**
     * JWK 源配置
     * 提供用于 JWT 签名的密钥集合
     */
    private JWKSource<SecurityContext> jwkSource(SecurityProperties securityProperties) {
        KeyPair keyPair = getOrCreateKeyPair(securityProperties.getToken().getJwtSecret());
        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
        RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
        
        JWK jwk = new RSAKey.Builder(publicKey)
            .privateKey(privateKey)
            .keyID(generateKeyId(securityProperties.getToken().getJwtSecret()))
            .algorithm(com.nimbusds.jose.JWSAlgorithm.RS256)
            .build();
        
        JWKSet jwkSet = new JWKSet(jwk);
        return new ImmutableJWKSet<>(jwkSet);
    }
    
    /**
     * 基于密钥生成或获取缓存的 RSA 密钥对
     * 使用密钥的哈希值作为种子，确保相同密钥生成相同的密钥对
     */
    private KeyPair getOrCreateKeyPair(String secret) {
        return keyPairCache.computeIfAbsent(secret, this::generateKeyPair);
    }
    
    /**
     * 生成 RSA 密钥对
     * 使用密钥的哈希值作为随机种子，确保可重现性
     */
    private KeyPair generateKeyPair(String secret) {
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            
            // 使用密钥的哈希值作为种子，确保相同密钥生成相同的密钥对
            byte[] seed = MessageDigest.getInstance("SHA-256")
                .digest(secret.getBytes(StandardCharsets.UTF_8));
            
            // 注意：在生产环境中，建议使用预生成的密钥对或从安全存储中加载
            // 这里为了演示和测试目的使用确定性生成
            keyPairGenerator.initialize(2048, new java.security.SecureRandom(seed));
            
            return keyPairGenerator.generateKeyPair();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Failed to generate RSA key pair", e);
        }
    }
    
    /**
     * 生成密钥 ID
     * 基于密钥的哈希值生成唯一标识符
     */
    private String generateKeyId(String secret) {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256")
                .digest(secret.getBytes(StandardCharsets.UTF_8));
            
            // 取前8个字节转换为十六进制字符串作为 Key ID
            StringBuilder keyId = new StringBuilder();
            for (int i = 0; i < Math.min(8, hash.length); i++) {
                keyId.append(String.format("%02x", hash[i] & 0xff));
            }
            
            return "lore-core-" + keyId.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Failed to generate key ID", e);
        }
    }
}