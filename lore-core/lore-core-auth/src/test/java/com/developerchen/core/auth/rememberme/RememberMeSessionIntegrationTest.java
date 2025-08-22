package com.developerchen.core.auth.rememberme;

import com.developerchen.core.auth.config.RememberMeConfig;
import com.developerchen.core.auth.config.SecurityConfig;
import com.developerchen.core.auth.config.SecurityProperties;
import com.developerchen.core.auth.user.InMemoryUserDetailsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.RememberMeServices;
import org.springframework.security.web.authentication.rememberme.TokenBasedRememberMeServices;
import org.springframework.test.context.TestPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 记住我功能与会话管理集成测试
 * 
 * @author syc
 */
@SpringBootTest(classes = {
    SecurityConfig.class,
    RememberMeConfig.class,
    RememberMeSessionIntegrationTest.TestConfig.class
})
@TestPropertySource(properties = {
    "security.remember-me.key=test-remember-me-key",
    "security.remember-me.token-validity-seconds=PT24H",
    "security.session.stateful=true"
})
class RememberMeSessionIntegrationTest {

    @Autowired
    private RememberMeServices rememberMeServices;

    @Autowired
    private SecurityProperties securityProperties;

    @TestConfiguration
    static class TestConfig {
        @Bean
        public SecurityProperties securityProperties() {
            SecurityProperties properties = new SecurityProperties();
            properties.getRememberMe().setKey("test-remember-me-key");
            properties.getRememberMe().setTokenValiditySeconds(java.time.Duration.ofHours(24));
            properties.getSession().setStateful(true);
            return properties;
        }

        @Bean
        public UserDetailsService userDetailsService() {
            return new InMemoryUserDetailsService();
        }
    }

    @Test
    void shouldConfigureRememberMeWithStatefulSession() {
        // 验证记住我服务配置正确
        assertThat(rememberMeServices).isNotNull();
        assertThat(rememberMeServices).isInstanceOf(TokenBasedRememberMeServices.class);
        
        // 验证会话配置为有状态
        assertThat(securityProperties.getSession().isStateful()).isTrue();
        
        // 验证记住我配置
        assertThat(securityProperties.getRememberMe().getKey()).isEqualTo("test-remember-me-key");
        assertThat(securityProperties.getRememberMe().getTokenValiditySeconds()).isEqualTo(java.time.Duration.ofHours(24));
    }

    @Test
    void shouldHaveCorrectRememberMeConfiguration() {
        TokenBasedRememberMeServices tokenBasedService = (TokenBasedRememberMeServices) rememberMeServices;
        
        // 验证参数名称
        assertThat(tokenBasedService.getParameter()).isEqualTo("remember-me");
        
        // 验证服务已正确配置
        assertThat(tokenBasedService).isNotNull();
    }
}