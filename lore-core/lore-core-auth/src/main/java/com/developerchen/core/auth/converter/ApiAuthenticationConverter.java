package com.developerchen.core.auth.converter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationConverter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;

/**
 * API 认证转换器
 * 解析 JSON 请求体，仅处理 /api/login 路径和 application/json 内容类型
 * 
 * @author syc
 */
@Component
public class ApiAuthenticationConverter implements AuthenticationConverter {
    
    private static final String USERNAME_FIELD = "username";
    private static final String PASSWORD_FIELD = "password";
    
    private final RequestMatcher requestMatcher = new AntPathRequestMatcher("/api/login", "POST");
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @Override
    public Authentication convert(HttpServletRequest request) {
        if (!requestMatcher.matches(request)) {
            return null;
        }
        
        // 检查是否为 JSON 内容类型
        String contentType = request.getContentType();
        if (contentType == null || !contentType.contains("application/json")) {
            return null;
        }
        
        try {
            JsonNode jsonNode = objectMapper.readTree(request.getInputStream());
            
            String username = extractField(jsonNode, USERNAME_FIELD);
            String password = extractField(jsonNode, PASSWORD_FIELD);
            
            if (!StringUtils.hasText(username) || !StringUtils.hasText(password)) {
                return null;
            }
            
            return UsernamePasswordAuthenticationToken.unauthenticated(username, password);
            
        } catch (IOException e) {
            // JSON 解析失败，返回 null
            return null;
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
     * 获取请求匹配器
     */
    public RequestMatcher getRequestMatcher() {
        return requestMatcher;
    }
}