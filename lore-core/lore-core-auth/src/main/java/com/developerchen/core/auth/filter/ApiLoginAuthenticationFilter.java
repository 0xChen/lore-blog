package com.developerchen.core.auth.filter;

import com.developerchen.core.auth.converter.ApiAuthenticationConverter;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

import java.io.IOException;

/**
 * API 登录认证过滤器
 * 继承自 AbstractAuthenticationProcessingFilter，处理 /api/login 的 JSON 登录请求
 * 
 * @author syc
 */
public class ApiLoginAuthenticationFilter extends AbstractAuthenticationProcessingFilter {
    
    private static final RequestMatcher DEFAULT_REQUEST_MATCHER = 
        new AntPathRequestMatcher("/api/login", "POST");
    
    private final ApiAuthenticationConverter authenticationConverter;
    
    /**
     * 构造函数
     * 
     * @param authenticationManager 认证管理器
     * @param authenticationConverter API 认证转换器
     */
    public ApiLoginAuthenticationFilter(
            AuthenticationManager authenticationManager,
            ApiAuthenticationConverter authenticationConverter) {
        super(DEFAULT_REQUEST_MATCHER, authenticationManager);
        this.authenticationConverter = authenticationConverter;
    }
    
    /**
     * 构造函数，支持自定义请求匹配器
     * 
     * @param requestMatcher 请求匹配器
     * @param authenticationManager 认证管理器
     * @param authenticationConverter API 认证转换器
     */
    public ApiLoginAuthenticationFilter(
            RequestMatcher requestMatcher,
            AuthenticationManager authenticationManager,
            ApiAuthenticationConverter authenticationConverter) {
        super(requestMatcher, authenticationManager);
        this.authenticationConverter = authenticationConverter;
    }
    
    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException {
        
        // 检查是否为 JSON 内容类型
        String contentType = request.getContentType();
        if (contentType == null || !contentType.contains("application/json")) {
            throw new AuthenticationException("不支持的内容类型，需要 application/json") {};
        }
        
        // 直接解析 JSON 请求体，因为过滤器已经处理了路径匹配
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(request.getInputStream());
            
            String username = extractField(jsonNode, "username");
            String password = extractField(jsonNode, "password");
            
            if (username == null || username.trim().isEmpty() || 
                password == null || password.trim().isEmpty()) {
                throw new AuthenticationException("用户名或密码不能为空") {};
            }
            
            UsernamePasswordAuthenticationToken authToken = 
                UsernamePasswordAuthenticationToken.unauthenticated(username, password);
            
            // 委托给 AuthenticationManager 进行认证
            return getAuthenticationManager().authenticate(authToken);
            
        } catch (IOException e) {
            throw new AuthenticationException("JSON 解析失败: " + e.getMessage()) {};
        }
    }
    
    /**
     * 从 JSON 节点中提取字段值
     */
    private String extractField(JsonNode jsonNode, String fieldName) {
        JsonNode fieldNode = jsonNode.get(fieldName);
        if (fieldNode == null || fieldNode.isNull()) {
            return null;
        }
        return fieldNode.asText();
    }
    
    /**
     * 设置认证成功处理器
     * 
     * @param successHandler 成功处理器
     */
    public void setAuthenticationSuccessHandler(AuthenticationSuccessHandler successHandler) {
        super.setAuthenticationSuccessHandler(successHandler);
    }
    
    /**
     * 设置认证失败处理器
     * 
     * @param failureHandler 失败处理器
     */
    public void setAuthenticationFailureHandler(AuthenticationFailureHandler failureHandler) {
        super.setAuthenticationFailureHandler(failureHandler);
    }
}