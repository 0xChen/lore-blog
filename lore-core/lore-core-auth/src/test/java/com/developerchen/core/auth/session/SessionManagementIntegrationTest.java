package com.developerchen.core.auth.session;

import com.developerchen.core.auth.config.SecurityAutoConfiguration;
import com.developerchen.core.auth.config.SecurityProperties;
import com.developerchen.core.auth.config.SessionConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.session.web.http.CookieSerializer;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 会话管理集成测试
 * 
 * @author syc
 */
@SpringBootTest(classes = {
    SecurityProperties.class,
    SecurityAutoConfiguration.class,
    SessionConfiguration.class
})
@TestPropertySource(properties = {
    "security.session.stateful=true",
    "security.session.redis-namespace=test:session"
})
class SessionManagementIntegrationTest {
    
    @Autowired
    private SecurityProperties securityProperties;
    
    @Autowired
    private SessionRegistry sessionRegistry;
    
    @Autowired
    private CookieSerializer cookieSerializer;
    
    @Test
    void testSessionConfigurationLoaded() {
        assertThat(securityProperties).isNotNull();
        assertThat(securityProperties.getSession().isStateful()).isTrue();
        assertThat(securityProperties.getSession().getRedisNamespace()).isEqualTo("test:session");
    }
    
    @Test
    void testSessionRegistryBean() {
        assertThat(sessionRegistry).isNotNull();
    }
    
    @Test
    void testCookieSerializerBean() {
        assertThat(cookieSerializer).isNotNull();
    }
}