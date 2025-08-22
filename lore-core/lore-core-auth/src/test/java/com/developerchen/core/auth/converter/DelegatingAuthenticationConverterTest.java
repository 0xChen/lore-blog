package com.developerchen.core.auth.converter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthenticationToken;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * DelegatingAuthenticationConverter 集成测试
 * 
 * @author syc
 */
class DelegatingAuthenticationConverterTest {
    
    private DelegatingAuthenticationConverter delegatingConverter;
    
    @BeforeEach
    void setUp() {
        FormAuthenticationConverter formConverter = new FormAuthenticationConverter();
        ApiAuthenticationConverter apiConverter = new ApiAuthenticationConverter();
        TokenAuthenticationConverter tokenConverter = new TokenAuthenticationConverter();
        
        delegatingConverter = new DelegatingAuthenticationConverter(formConverter, apiConverter, tokenConverter);
    }
    
    @Test
    void convert_FormLogin_ReturnsUsernamePasswordAuthentication() {
        // Given
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/login");
        request.setServletPath("/login");
        request.setContentType("application/x-www-form-urlencoded");
        request.setParameter("username", "testuser");
        request.setParameter("password", "testpass");
        
        // When
        Authentication result = delegatingConverter.convert(request);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result).isInstanceOf(UsernamePasswordAuthenticationToken.class);
        assertThat(result.getName()).isEqualTo("testuser");
        assertThat(result.getCredentials()).isEqualTo("testpass");
    }
    
    @Test
    void convert_ApiLogin_ReturnsUsernamePasswordAuthentication() {
        // Given
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/login");
        request.setServletPath("/api/login");
        request.setContentType("application/json");
        String jsonBody = "{\"username\":\"apiuser\",\"password\":\"apipass\"}";
        request.setContent(jsonBody.getBytes(StandardCharsets.UTF_8));
        
        // When
        Authentication result = delegatingConverter.convert(request);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result).isInstanceOf(UsernamePasswordAuthenticationToken.class);
        assertThat(result.getName()).isEqualTo("apiuser");
        assertThat(result.getCredentials()).isEqualTo("apipass");
    }
    
    @Test
    void convert_JwtToken_ReturnsBearerTokenAuthentication() {
        // Given
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/protected");
        String jwtToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c";
        request.addHeader("Authorization", "Bearer " + jwtToken);
        
        // When
        Authentication result = delegatingConverter.convert(request);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result).isInstanceOf(BearerTokenAuthenticationToken.class);
        assertThat(result.getCredentials()).isEqualTo(jwtToken);
    }
    
    @Test
    void convert_OpaqueToken_ReturnsOpaqueTokenAuthentication() {
        // Given
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/protected");
        String opaqueToken = "550e8400-e29b-41d4-a716-446655440000";
        request.addHeader("Authorization", "Bearer " + opaqueToken);
        
        // When
        Authentication result = delegatingConverter.convert(request);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result).isInstanceOf(OpaqueTokenAuthenticationToken.class);
        OpaqueTokenAuthenticationToken token = (OpaqueTokenAuthenticationToken) result;
        assertThat(token.getToken()).isEqualTo(opaqueToken);
    }
    
    @Test
    void convert_NoMatchingConverter_ReturnsNull() {
        // Given - 一个不匹配任何转换器的请求
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/some/other/path");
        
        // When
        Authentication result = delegatingConverter.convert(request);
        
        // Then
        assertThat(result).isNull();
    }
    
    @Test
    void convert_FormLoginTakesPrecedenceOverToken() {
        // Given - 同时包含表单数据和令牌的请求
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/login");
        request.setServletPath("/login");
        request.setContentType("application/x-www-form-urlencoded");
        request.setParameter("username", "formuser");
        request.setParameter("password", "formpass");
        request.addHeader("Authorization", "Bearer some-token");
        
        // When
        Authentication result = delegatingConverter.convert(request);
        
        // Then - 应该返回表单认证，因为表单转换器优先级更高
        assertThat(result).isNotNull();
        assertThat(result).isInstanceOf(UsernamePasswordAuthenticationToken.class);
        assertThat(result.getName()).isEqualTo("formuser");
    }
    
    @Test
    void convert_ApiLoginTakesPrecedenceOverToken() {
        // Given - 同时包含 API JSON 数据和令牌的请求
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/login");
        request.setServletPath("/api/login");
        request.setContentType("application/json");
        String jsonBody = "{\"username\":\"apiuser\",\"password\":\"apipass\"}";
        request.setContent(jsonBody.getBytes(StandardCharsets.UTF_8));
        request.addHeader("Authorization", "Bearer some-token");
        
        // When
        Authentication result = delegatingConverter.convert(request);
        
        // Then - 应该返回 API 认证，因为 API 转换器优先级更高
        assertThat(result).isNotNull();
        assertThat(result).isInstanceOf(UsernamePasswordAuthenticationToken.class);
        assertThat(result.getName()).isEqualTo("apiuser");
    }
    
    @Test
    void convert_InvalidFormLogin_FallsBackToToken() {
        // Given - 表单登录路径但缺少必要参数，同时包含有效令牌
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/login");
        request.setServletPath("/login");
        request.setContentType("application/x-www-form-urlencoded");
        // 缺少 username 和 password 参数
        String token = "550e8400-e29b-41d4-a716-446655440000";
        request.addHeader("Authorization", "Bearer " + token);
        
        // When
        Authentication result = delegatingConverter.convert(request);
        
        // Then - 应该回退到令牌认证
        assertThat(result).isNotNull();
        assertThat(result).isInstanceOf(OpaqueTokenAuthenticationToken.class);
        OpaqueTokenAuthenticationToken opaqueToken = (OpaqueTokenAuthenticationToken) result;
        assertThat(opaqueToken.getToken()).isEqualTo(token);
    }
    
    @Test
    void convert_InvalidApiLogin_FallsBackToToken() {
        // Given - API 登录路径但 JSON 格式错误，同时包含有效令牌
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/login");
        request.setServletPath("/api/login");
        request.setContentType("application/json");
        String invalidJson = "{invalid json}";
        request.setContent(invalidJson.getBytes(StandardCharsets.UTF_8));
        String token = "550e8400-e29b-41d4-a716-446655440000";
        request.addHeader("Authorization", "Bearer " + token);
        
        // When
        Authentication result = delegatingConverter.convert(request);
        
        // Then - 应该回退到令牌认证
        assertThat(result).isNotNull();
        assertThat(result).isInstanceOf(OpaqueTokenAuthenticationToken.class);
        OpaqueTokenAuthenticationToken opaqueToken = (OpaqueTokenAuthenticationToken) result;
        assertThat(opaqueToken.getToken()).isEqualTo(token);
    }
    
    @Test
    void getConverters_ReturnsAllConverters() {
        // When
        var converters = delegatingConverter.getConverters();
        
        // Then
        assertThat(converters).hasSize(3);
        assertThat(converters.get(0)).isInstanceOf(FormAuthenticationConverter.class);
        assertThat(converters.get(1)).isInstanceOf(ApiAuthenticationConverter.class);
        assertThat(converters.get(2)).isInstanceOf(TokenAuthenticationConverter.class);
    }
}