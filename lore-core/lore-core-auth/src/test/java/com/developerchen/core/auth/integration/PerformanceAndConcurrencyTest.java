package com.developerchen.core.auth.integration;

import com.developerchen.core.auth.config.SecurityAutoConfiguration;
import com.developerchen.core.auth.service.InMemoryRefreshTokenService;
import com.developerchen.core.auth.service.InMemoryTokenService;
import com.developerchen.core.auth.service.TokenType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * 性能和并发测试
 * 测试令牌验证性能、高并发登录和令牌刷新场景、内存令牌服务的并发安全性
 * 
 * 需求覆盖:
 * - 2.6: 令牌验证性能
 * - 3.2: 高并发令牌刷新
 * 
 * @author syc
 */
@SpringBootTest(classes = {
    SecurityAutoConfiguration.class,
    PerformanceAndConcurrencyTest.TestConfig.class
})
@AutoConfigureWebMvc
@TestPropertySource(properties = {
    "security.token.jwt-secret=test-secret-key-for-performance-and-concurrency-test-with-sufficient-length-for-security",
    "security.token.access-token-expiration=PT30M",
    "security.token.refresh-token-expiration=P7D",
    "security.session.stateful=true",
    "security.remember-me.key=test-remember-me-key"
})
class PerformanceAndConcurrencyTest {

    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Autowired
    private InMemoryTokenService tokenService;
    
    @Autowired
    private InMemoryRefreshTokenService refreshTokenService;

    private static final String TEST_USERNAME = "testuser";
    private static final String TEST_PASSWORD = "password";
    private static final int CONCURRENT_THREADS = 10;
    private static final int OPERATIONS_PER_THREAD = 50;

    @BeforeEach
    void setUp() {
        tokenService.cleanupExpiredTokens();
        refreshTokenService.cleanupExpiredRefreshTokens();
    }

    /**
     * 测试令牌验证性能
     * 需求 2.6: 验证令牌验证操作的性能表现
     */
    @Test
    void testTokenValidationPerformance() throws Exception {
        // 1. 准备测试数据 - 生成多个令牌
        List<String> tokens = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
            String token = tokenService.generateAccessToken(TEST_USERNAME + i, 
                TokenType.UUID);
            tokens.add(token);
        }

        // 2. 测试UUID令牌验证性能
        long startTime = System.currentTimeMillis();
        
        for (String token : tokens) {
            var result = tokenService.validateToken(token);
            assertTrue(result.valid());
        }
        
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        // 验证性能 - 1000个令牌验证应该在合理时间内完成（比如1秒）
        assertTrue(duration < 1000, 
            String.format("令牌验证耗时过长: %d ms", duration));
        
        System.out.printf("UUID令牌验证性能: 1000个令牌耗时 %d ms, 平均每个 %.2f ms%n", 
            duration, (double) duration / tokens.size());

        // 3. 测试JWT令牌验证性能
        List<String> jwtTokens = new ArrayList<>();
        for (int i = 0; i < 100; i++) { // JWT验证相对较慢，测试较少数量
            String jwtToken = tokenService.generateAccessToken(TEST_USERNAME + i, 
                TokenType.JWT);
            jwtTokens.add(jwtToken);
        }

        long jwtStartTime = System.currentTimeMillis();
        
        for (String jwtToken : jwtTokens) {
            var result = tokenService.validateToken(jwtToken);
            assertTrue(result.valid());
        }
        
        long jwtEndTime = System.currentTimeMillis();
        long jwtDuration = jwtEndTime - jwtStartTime;
        
        System.out.printf("JWT令牌验证性能: 100个令牌耗时 %d ms, 平均每个 %.2f ms%n", 
            jwtDuration, (double) jwtDuration / jwtTokens.size());
    }

    /**
     * 测试高并发登录场景
     * 验证系统在高并发登录时的稳定性和性能
     */
    @Test
    void testHighConcurrencyLogin() throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(CONCURRENT_THREADS);
        CountDownLatch latch = new CountDownLatch(CONCURRENT_THREADS);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);
        List<Exception> exceptions = new CopyOnWriteArrayList<>();

        long startTime = System.currentTimeMillis();

        // 启动并发登录任务
        for (int i = 0; i < CONCURRENT_THREADS; i++) {
            final int threadId = i;
            executor.submit(() -> {
                try {
                    for (int j = 0; j < OPERATIONS_PER_THREAD; j++) {
                        String username = TEST_USERNAME + "_" + threadId + "_" + j;
                        performApiLogin(username, TEST_PASSWORD);
                        successCount.incrementAndGet();
                    }
                } catch (Exception e) {
                    failureCount.incrementAndGet();
                    exceptions.add(e);
                } finally {
                    latch.countDown();
                }
            });
        }

        // 等待所有任务完成
        boolean completed = latch.await(30, TimeUnit.SECONDS);
        assertTrue(completed, "并发登录测试超时");

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        executor.shutdown();

        // 验证结果
        int expectedOperations = CONCURRENT_THREADS * OPERATIONS_PER_THREAD;
        System.out.printf("并发登录测试结果: 成功 %d, 失败 %d, 总耗时 %d ms%n", 
            successCount.get(), failureCount.get(), duration);

        // 大部分操作应该成功
        assertTrue(successCount.get() > expectedOperations * 0.9, 
            "成功率过低: " + successCount.get() + "/" + expectedOperations);

        // 如果有异常，打印第一个异常信息
        if (!exceptions.isEmpty()) {
            System.err.println("并发登录异常示例: " + exceptions.get(0).getMessage());
        }
    }

    /**
     * 测试高并发令牌刷新场景
     * 需求 3.2: 验证令牌刷新在高并发情况下的正确性
     */
    @Test
    void testHighConcurrencyTokenRefresh() throws Exception {
        // 1. 准备测试数据 - 为每个线程生成刷新令牌
        List<String> refreshTokens = new ArrayList<>();
        for (int i = 0; i < CONCURRENT_THREADS; i++) {
            String refreshToken = refreshTokenService.generateRefreshToken(TEST_USERNAME + "_" + i);
            refreshTokens.add(refreshToken);
        }

        ExecutorService executor = Executors.newFixedThreadPool(CONCURRENT_THREADS);
        CountDownLatch latch = new CountDownLatch(CONCURRENT_THREADS);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);
        List<Exception> exceptions = new CopyOnWriteArrayList<>();

        long startTime = System.currentTimeMillis();

        // 启动并发刷新任务
        for (int i = 0; i < CONCURRENT_THREADS; i++) {
            final int threadId = i;
            final String refreshToken = refreshTokens.get(i);
            
            executor.submit(() -> {
                try {
                    for (int j = 0; j < OPERATIONS_PER_THREAD; j++) {
                        // 每次刷新都会生成新的刷新令牌，所以需要使用最新的
                        String currentRefreshToken = (j == 0) ? refreshToken : 
                            refreshTokenService.generateRefreshToken(TEST_USERNAME + "_" + threadId);
                        
                        performTokenRefresh(currentRefreshToken);
                        successCount.incrementAndGet();
                    }
                } catch (Exception e) {
                    failureCount.incrementAndGet();
                    exceptions.add(e);
                } finally {
                    latch.countDown();
                }
            });
        }

        // 等待所有任务完成
        boolean completed = latch.await(30, TimeUnit.SECONDS);
        assertTrue(completed, "并发令牌刷新测试超时");

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        executor.shutdown();

        // 验证结果
        int expectedOperations = CONCURRENT_THREADS * OPERATIONS_PER_THREAD;
        System.out.printf("并发令牌刷新测试结果: 成功 %d, 失败 %d, 总耗时 %d ms%n", 
            successCount.get(), failureCount.get(), duration);

        // 大部分操作应该成功
        assertTrue(successCount.get() > expectedOperations * 0.8, 
            "令牌刷新成功率过低: " + successCount.get() + "/" + expectedOperations);

        if (!exceptions.isEmpty()) {
            System.err.println("并发令牌刷新异常示例: " + exceptions.get(0).getMessage());
        }
    }

    /**
     * 测试内存令牌服务的并发安全性
     * 验证InMemoryTokenService在并发访问时的线程安全性
     */
    @Test
    void testInMemoryTokenServiceConcurrencySafety() throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(CONCURRENT_THREADS);
        CountDownLatch latch = new CountDownLatch(CONCURRENT_THREADS);
        AtomicInteger operationCount = new AtomicInteger(0);
        List<Exception> exceptions = new CopyOnWriteArrayList<>();

        // 并发执行令牌操作
        for (int i = 0; i < CONCURRENT_THREADS; i++) {
            final int threadId = i;
            executor.submit(() -> {
                try {
                    for (int j = 0; j < OPERATIONS_PER_THREAD; j++) {
                        String username = "user_" + threadId + "_" + j;
                        
                        // 生成令牌
                        String token = tokenService.generateAccessToken(username, 
                            TokenType.UUID);
                        assertNotNull(token);
                        
                        // 验证令牌
                        var result = tokenService.validateToken(token);
                        assertTrue(result.valid());
                        assertEquals(username, result.username());
                        
                        // 删除令牌
                        tokenService.deleteToken(token);
                        
                        // 验证令牌已删除
                        var deletedResult = tokenService.validateToken(token);
                        assertFalse(deletedResult.valid());
                        
                        operationCount.incrementAndGet();
                    }
                } catch (Exception e) {
                    exceptions.add(e);
                } finally {
                    latch.countDown();
                }
            });
        }

        // 等待所有任务完成
        boolean completed = latch.await(30, TimeUnit.SECONDS);
        assertTrue(completed, "并发安全性测试超时");

        executor.shutdown();

        // 验证结果
        int expectedOperations = CONCURRENT_THREADS * OPERATIONS_PER_THREAD;
        System.out.printf("令牌服务并发安全性测试: 完成操作 %d/%d%n", 
            operationCount.get(), expectedOperations);

        // 所有操作都应该成功完成
        assertEquals(expectedOperations, operationCount.get(), 
            "并发操作完成数量不符合预期");

        // 不应该有异常
        if (!exceptions.isEmpty()) {
            fail("并发安全性测试出现异常: " + exceptions.get(0).getMessage());
        }
    }

    /**
     * 测试刷新令牌服务的并发安全性
     */
    @Test
    void testRefreshTokenServiceConcurrencySafety() throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(CONCURRENT_THREADS);
        CountDownLatch latch = new CountDownLatch(CONCURRENT_THREADS);
        AtomicInteger operationCount = new AtomicInteger(0);
        List<Exception> exceptions = new CopyOnWriteArrayList<>();

        // 并发执行刷新令牌操作
        for (int i = 0; i < CONCURRENT_THREADS; i++) {
            final int threadId = i;
            executor.submit(() -> {
                try {
                    for (int j = 0; j < OPERATIONS_PER_THREAD; j++) {
                        String username = "user_" + threadId + "_" + j;
                        
                        // 生成刷新令牌
                        String refreshToken = refreshTokenService.generateRefreshToken(username);
                        assertNotNull(refreshToken);
                        
                        // 验证刷新令牌
                        var result = refreshTokenService.validateRefreshToken(refreshToken);
                        assertTrue(result.valid());
                        assertEquals(username, result.username());
                        
                        // 刷新访问令牌
                        var tokenPair = refreshTokenService.refreshAccessToken(refreshToken, 
                            TokenType.UUID);
                        assertNotNull(tokenPair.accessToken());
                        assertNotNull(tokenPair.refreshToken());
                        
                        // 验证原刷新令牌已失效（令牌轮换）
                        var invalidResult = refreshTokenService.validateRefreshToken(refreshToken);
                        assertFalse(invalidResult.valid());
                        
                        operationCount.incrementAndGet();
                    }
                } catch (Exception e) {
                    exceptions.add(e);
                } finally {
                    latch.countDown();
                }
            });
        }

        // 等待所有任务完成
        boolean completed = latch.await(30, TimeUnit.SECONDS);
        assertTrue(completed, "刷新令牌并发安全性测试超时");

        executor.shutdown();

        // 验证结果
        int expectedOperations = CONCURRENT_THREADS * OPERATIONS_PER_THREAD;
        System.out.printf("刷新令牌服务并发安全性测试: 完成操作 %d/%d%n", 
            operationCount.get(), expectedOperations);

        assertEquals(expectedOperations, operationCount.get(), 
            "刷新令牌并发操作完成数量不符合预期");

        if (!exceptions.isEmpty()) {
            fail("刷新令牌并发安全性测试出现异常: " + exceptions.get(0).getMessage());
        }
    }

    /**
     * 测试混合令牌类型的并发性能
     * 同时测试JWT和UUID令牌的并发处理性能
     */
    @Test
    void testMixedTokenTypeConcurrencyPerformance() throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(CONCURRENT_THREADS);
        CountDownLatch latch = new CountDownLatch(CONCURRENT_THREADS);
        AtomicInteger jwtCount = new AtomicInteger(0);
        AtomicInteger uuidCount = new AtomicInteger(0);
        List<Exception> exceptions = new CopyOnWriteArrayList<>();

        long startTime = System.currentTimeMillis();

        // 并发生成和验证不同类型的令牌
        for (int i = 0; i < CONCURRENT_THREADS; i++) {
            final int threadId = i;
            executor.submit(() -> {
                try {
                    for (int j = 0; j < OPERATIONS_PER_THREAD; j++) {
                        String username = "user_" + threadId + "_" + j;
                        
                        // 交替生成JWT和UUID令牌
                        TokenType tokenType = 
                            (j % 2 == 0) ? TokenType.JWT : TokenType.UUID;
                        
                        String token = tokenService.generateAccessToken(username, tokenType);
                        var result = tokenService.validateToken(token);
                        
                        assertTrue(result.valid());
                        assertEquals(username, result.username());
                        
                        if (tokenType == TokenType.JWT) {
                            jwtCount.incrementAndGet();
                        } else {
                            uuidCount.incrementAndGet();
                        }
                    }
                } catch (Exception e) {
                    exceptions.add(e);
                } finally {
                    latch.countDown();
                }
            });
        }

        boolean completed = latch.await(30, TimeUnit.SECONDS);
        assertTrue(completed, "混合令牌类型并发测试超时");

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        executor.shutdown();

        System.out.printf("混合令牌类型并发测试: JWT %d个, UUID %d个, 总耗时 %d ms%n", 
            jwtCount.get(), uuidCount.get(), duration);

        // 验证令牌数量分布合理
        int totalTokens = jwtCount.get() + uuidCount.get();
        int expectedTotal = CONCURRENT_THREADS * OPERATIONS_PER_THREAD;
        assertEquals(expectedTotal, totalTokens, "令牌总数不符合预期");

        if (!exceptions.isEmpty()) {
            fail("混合令牌类型并发测试出现异常: " + exceptions.get(0).getMessage());
        }
    }

    /**
     * 执行API登录
     */
    private void performApiLogin(String username, String password) throws Exception {
        String loginRequest = objectMapper.writeValueAsString(
            java.util.Map.of(
                "username", username,
                "password", password
            )
        );

        MvcResult result = mockMvc.perform(post("/api/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.access_token").exists())
                .andReturn();

        // 验证响应格式
        String responseContent = result.getResponse().getContentAsString();
        JsonNode responseJson = objectMapper.readTree(responseContent);
        assertNotNull(responseJson.get("access_token").asText());
        assertNotNull(responseJson.get("refresh_token").asText());
    }

    /**
     * 执行令牌刷新
     */
    private void performTokenRefresh(String refreshToken) throws Exception {
        String refreshRequest = objectMapper.writeValueAsString(
            java.util.Map.of("refresh_token", refreshToken)
        );

        MvcResult result = mockMvc.perform(post("/api/token/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(refreshRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.access_token").exists())
                .andReturn();

        // 验证响应格式
        String responseContent = result.getResponse().getContentAsString();
        JsonNode responseJson = objectMapper.readTree(responseContent);
        assertNotNull(responseJson.get("access_token").asText());
        assertNotNull(responseJson.get("refresh_token").asText());
    }

    /**
     * 测试配置类
     */
    @Configuration
    static class TestConfig {
        
        @Bean
        @Primary
        public UserDetailsService userDetailsService(PasswordEncoder passwordEncoder) {
            // 创建大量用户用于并发测试
            List<UserDetails> users = new ArrayList<>();
            
            // 基础测试用户
            users.add(User.builder()
                    .username(TEST_USERNAME)
                    .password(passwordEncoder.encode(TEST_PASSWORD))
                    .roles("USER")
                    .build());
            
            // 为并发测试创建用户
            for (int i = 0; i < CONCURRENT_THREADS * OPERATIONS_PER_THREAD; i++) {
                users.add(User.builder()
                        .username(TEST_USERNAME + "_" + i)
                        .password(passwordEncoder.encode(TEST_PASSWORD))
                        .roles("USER")
                        .build());
            }
            
            return new InMemoryUserDetailsManager(users);
        }
    }
}