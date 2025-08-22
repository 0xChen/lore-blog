package com.developerchen.core.auth.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.session.web.http.HttpSessionIdResolver;
import org.springframework.session.web.http.CookieHttpSessionIdResolver;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * SessionConfiguration 测试类
 * 
 * @author syc
 */
@SpringBootTest(classes = {SecurityProperties.class, SessionConfiguration.class})
@TestPropertySource(properties = {
    "security.session.stateful=true"
})
class SessionConfigurationTest {
    
    @Test
    void testHttpSessionIdResolverConfiguration() {
        SecurityProperties securityProperties = new SecurityProperties();
        SessionConfiguration sessionConfiguration = new SessionConfiguration(securityProperties);
        
        HttpSessionIdResolver httpSessionIdResolver = sessionConfiguration.httpSessionIdResolver();
        
        assertThat(httpSessionIdResolver).isInstanceOf(CookieHttpSessionIdResolver.class);
        assertThat(httpSessionIdResolver).isNotNull();
    }
}