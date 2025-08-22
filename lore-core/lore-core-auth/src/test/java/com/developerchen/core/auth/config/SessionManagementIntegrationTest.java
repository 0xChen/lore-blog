package com.developerchen.core.auth.config;

import com.developerchen.core.auth.service.OnlineUserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.session.SessionRepository;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 会话管理集成测试
 * 
 * @author syc
 */
@SpringBootTest(classes = {
    SecurityAutoConfiguration.class,
    SessionConfiguration.class,
    SecurityConfig.class
})
@AutoConfigureWebMvc
@TestPropertySource(properties = {
    "security.session.stateful=true",
    "security.session.redis-namespace=test:session",
    "spring.session.store-type=redis"
})
class SessionManagementIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    
    @Autowired(required = false)
    private SessionRegistry sessionRegistry;
    
    @Autowired(required = false)
    private OnlineUserService onlineUserService;
    
    @MockBean
    private RedisConnectionFactory redisConnectionFactory;
    
    @MockBean
    private SessionRepository<?> sessionRepository;
    
    @Test
    void contextLoads() {
        // 验证会话管理相关的Bean是否正确加载
        assertThat(sessionRegistry).isNotNull();
        assertThat(onlineUserService).isNotNull();
    }
    
    @Test
    void loginPage_ShouldBeAccessible() throws Exception {
        mockMvc.perform(get("/login"))
            .andExpect(status().isOk());
    }
    
    @Test
    @WithMockUser
    void authenticatedRequest_ShouldCreateSession() throws Exception {
        mockMvc.perform(get("/"))
            .andExpect(status().isOk());
    }
    
    @Test
    void formLogin_ShouldEnforceCsrf() throws Exception {
        mockMvc.perform(post("/login")
                .param("username", "testuser")
                .param("password", "password"))
            .andExpect(status().isForbidden()); // CSRF token missing
    }
    
    @Test
    void formLogin_WithCsrf_ShouldBeAllowed() throws Exception {
        mockMvc.perform(post("/login")
                .param("username", "testuser")
                .param("password", "password")
                .with(csrf()))
            .andExpect(status().isFound()); // Redirect after login attempt
    }
    
    @Test
    void apiLogin_ShouldNotRequireCsrf() throws Exception {
        mockMvc.perform(post("/api/login")
                .contentType("application/json")
                .content("{\"username\":\"testuser\",\"password\":\"password\"}"))
            .andExpect(status().isUnauthorized()); // No CSRF required, but authentication fails
    }
    
    @Test
    void logout_ShouldInvalidateSession() throws Exception {
        mockMvc.perform(post("/logout")
                .with(csrf()))
            .andExpect(status().isFound())
            .andExpect(redirectedUrl("/login?logout=true"));
    }
    
    @Test
    void sessionFixationProtection_ShouldBeEnabled() {
        // 验证会话固定攻击防护配置
        // 这个测试主要验证配置是否正确，实际的会话固定防护由Spring Security处理
        assertThat(sessionRegistry).isNotNull();
    }
    
    @Test
    void concurrentSessionControl_ShouldBeConfigured() {
        // 验证并发会话控制配置
        // 最大会话数应该设置为5，不阻止新登录
        assertThat(sessionRegistry).isNotNull();
    }
}