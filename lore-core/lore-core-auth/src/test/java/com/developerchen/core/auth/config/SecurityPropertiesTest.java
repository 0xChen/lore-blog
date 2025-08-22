package com.developerchen.core.auth.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

/**
 * SecurityProperties 配置属性测试
 * 
 * @author syc
 */
@SpringBootTest(classes = SecurityPropertiesTest.TestConfiguration.class)
@TestPropertySource(properties = {
    "security.token.access-token-expiration=PT15M",
    "security.token.refresh-token-expiration=P3D", 
    "security.token.jwt-secret=test-secret",
    "security.token.jwt-issuer=test-issuer",
    "security.session.stateful=false",
    "security.session.redis-namespace=test:session",
    "security.remember-me.key=test-remember-me",
    "security.remember-me.token-validity-seconds=P7D"
})
class SecurityPropertiesTest {

    @Autowired
    private SecurityProperties securityProperties;

    @Test
    void testTokenProperties() {
        SecurityProperties.Token token = securityProperties.getToken();
        
        assertEquals(Duration.ofMinutes(15), token.getAccessTokenExpiration());
        assertEquals(Duration.ofDays(3), token.getRefreshTokenExpiration());
        assertEquals("test-secret", token.getJwtSecret());
        assertEquals("test-issuer", token.getJwtIssuer());
    }

    @Test
    void testSessionProperties() {
        SecurityProperties.Session session = securityProperties.getSession();
        
        assertFalse(session.isStateful());
        assertEquals("test:session", session.getRedisNamespace());
    }

    @Test
    void testRememberMeProperties() {
        SecurityProperties.RememberMe rememberMe = securityProperties.getRememberMe();
        
        assertEquals("test-remember-me", rememberMe.getKey());
        assertEquals(Duration.ofDays(7), rememberMe.getTokenValiditySeconds());
    }

    @Test
    void testDefaultValues() {
        SecurityProperties defaultProperties = new SecurityProperties();
        
        // 测试默认值
        assertEquals(Duration.ofMinutes(30), defaultProperties.getToken().getAccessTokenExpiration());
        assertEquals(Duration.ofDays(7), defaultProperties.getToken().getRefreshTokenExpiration());
        assertEquals("lore-core", defaultProperties.getToken().getJwtIssuer());
        assertTrue(defaultProperties.getSession().isStateful());
        assertEquals("lore:session", defaultProperties.getSession().getRedisNamespace());
        assertEquals("lore-remember-me", defaultProperties.getRememberMe().getKey());
        assertEquals(Duration.ofDays(14), defaultProperties.getRememberMe().getTokenValiditySeconds());
    }

    @EnableConfigurationProperties(SecurityProperties.class)
    static class TestConfiguration {
    }
}