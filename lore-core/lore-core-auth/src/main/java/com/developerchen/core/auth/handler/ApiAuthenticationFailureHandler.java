package com.developerchen.core.auth.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * API 认证失败处理器
 * 处理 API 登录失败后的 JSON 错误响应
 * 
 * @author syc
 */
@Component
public class ApiAuthenticationFailureHandler implements AuthenticationFailureHandler {

    private final ObjectMapper objectMapper;

    public ApiAuthenticationFailureHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                      AuthenticationException exception) throws IOException, ServletException {
        
        // 构建错误响应数据
        Map<String, Object> errorData = new HashMap<>();
        errorData.put("error", "unauthorized");
        errorData.put("error_description", getErrorDescription(exception));
        errorData.put("timestamp", Instant.now().toString());
        
        // 设置响应头
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        
        // 写入 JSON 响应
        objectMapper.writeValue(response.getWriter(), errorData);
    }

    /**
     * 根据异常类型获取错误描述
     * 
     * @param exception 认证异常
     * @return 错误描述
     */
    private String getErrorDescription(AuthenticationException exception) {
        String exceptionName = exception.getClass().getSimpleName();
        
        return switch (exceptionName) {
            case "BadCredentialsException" -> "用户名或密码错误";
            case "UsernameNotFoundException" -> "用户不存在";
            case "AccountExpiredException" -> "账户已过期";
            case "CredentialsExpiredException" -> "密码已过期";
            case "DisabledException" -> "账户已被禁用";
            case "LockedException" -> "账户已被锁定";
            case "InsufficientAuthenticationException" -> "认证信息不足";
            default -> "认证失败：" + exception.getMessage();
        };
    }
}