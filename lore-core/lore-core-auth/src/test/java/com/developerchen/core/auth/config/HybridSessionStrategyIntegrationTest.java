package com.developerchen.core.auth.config;

import com.developerchen.core.auth.filter.HybridSessionCreationFilter;
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
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 混合会话策略集成测试
 * 
 * @author syc
 */
@SpringBootTest(classes = {
    SecurityAutoConfiguration.class,
    SessionConfiguration.class,
    HybridSessionConfiguration.class,
    SessionStrategySelector.class,
    HybridSessionCreationFilter.class
})
@AutoConfigureWebMvc
@TestPropertySource(properties = {
    "security.session.stateful=true",
    "security.session.hybrid=true",
    "security.session.redis-namespace=test:session"
})
class HybridSessionStrategyIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    
    @Autowired(required = false)
    private SessionRegistry sessionRegistry;
    
    @Autowired(required = false)
    private OnlineUserService onlineUserService;
    
    @Autowired
    private SessionStrategySelector sessionStrategySelector;
    
    @MockBean
    private RedisConnectionFactory redisConnectionFactory;
    
    @MockBean
    private SessionRepository<?> sessionRepository;
    
    @Test
    void contextLoads() {
        // 验证混合会话策略相关的Bean是否正确加载
        assertThat(sessionRegistry).isNotNull();
        assertThat(onlineUserService).isNotNull();
        assertThat(sessionStrategySelector).isNotNull();
    }
    
    @Test
    void webRequest_ShouldUseStatefulSession() throws Exception {
        MvcResult result = mockMvc.perform(get("/dashboard"))
            .andExpect(status().isUnauthorized())
            .andReturn();
        
        // 验证请求被标记为有状态
        String sessionStrategy = (String) result.getRequest().getAttribute("SESSION_STRATEGY");
        assertThat(sessionStrategy).isEqualTo("STATEFUL");
    }
    
    @Test
    void apiRequest_ShouldUseStatelessSession() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/users"))
            .andExpect(status().isUnauthorized())
            .andReturn();
        
        // 验证请求被标记为无状态
        String sessionStrategy = (String) result.getRequest().getAttribute("SESSION_STRATEGY");
        assertThat(sessionStrategy).isEqualTo("STATELESS");
    }
    
    @Test
    void requestWithBearerToken_ShouldUseStatelessSession() throws Exception {
        MvcResult result = mockMvc.perform(get("/dashboard")
                .header("Authorization", "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."))
            .andExpect(status().isUnauthorized())
            .andReturn();
        
        // 验证请求被标记为无状态
        String sessionStrategy = (String) result.getRequest().getAttribute("SESSION_STRATEGY");
        assertThat(sessionStrategy).isEqualTo("STATELESS");
    }
    
    @Test
    void requestWithStatelessHeader_ShouldUseStatelessSession() throws Exception {
        MvcResult result = mockMvc.perform(get("/dashboard")
                .header("X-Session-Strategy", "stateless"))
            .andExpect(status().isUnauthorized())
            .andReturn();
        
        // 验证请求被标记为无状态
        String sessionStrategy = (String) result.getRequest().getAttribute("SESSION_STRATEGY");
        assertThat(sessionStrategy).isEqualTo("STATELESS");
    }
    
    @Test
    void requestWithStatefulHeader_ShouldUseStatefulSession() throws Exception {
        MvcResult result = mockMvc.perform(get("/dashboard")
                .header("X-Session-Strategy", "stateful"))
            .andExpect(status().isUnauthorized())
            .andReturn();
        
        // 验证请求被标记为有状态
        String sessionStrategy = (String) result.getRequest().getAttribute("SESSION_STRATEGY");
        assertThat(sessionStrategy).isEqualTo("STATEFUL");
    }
    
    @Test
    void formLogin_ShouldRequireCsrf() throws Exception {
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
        MvcResult result = mockMvc.perform(post("/api/login")
                .contentType("application/json")
                .content("{\"username\":\"testuser\",\"password\":\"password\"}"))
            .andExpect(status().isUnauthorized()) // No CSRF required, but authentication fails
            .andReturn();
        
        // 验证API请求被标记为无状态
        String sessionStrategy = (String) result.getRequest().getAttribute("SESSION_STRATEGY");
        assertThat(sessionStrategy).isEqualTo("STATELESS");
    }
    
    @Test
    @WithMockUser
    void authenticatedWebRequest_ShouldMaintainSession() throws Exception {
        MvcResult result = mockMvc.perform(get("/dashboard"))
            .andExpect(status().isOk())
            .andReturn();
        
        // 验证有状态请求可以维护会话
        String sessionStrategy = (String) result.getRequest().getAttribute("SESSION_STRATEGY");
        assertThat(sessionStrategy).isEqualTo("STATEFUL");
    }
    
    @Test
    @WithMockUser
    void authenticatedApiRequest_ShouldNotCreateSession() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/profile"))
            .andExpect(status().isOk())
            .andReturn();
        
        // 验证API请求不创建会话
        String sessionStrategy = (String) result.getRequest().getAttribute("SESSION_STRATEGY");
        assertThat(sessionStrategy).isEqualTo("STATELESS");
        
        // 验证无状态请求包装器阻止了会话创建
        assertThat(result.getRequest().getSession(false)).isNull();
    }
}