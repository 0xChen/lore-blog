package com.developerchen.core.auth.controller;

import com.developerchen.core.auth.service.RefreshTokenService;
import com.developerchen.core.auth.service.RefreshTokenValidationResult;
import com.developerchen.core.auth.service.TokenPair;
import com.developerchen.core.auth.service.TokenType;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * 令牌刷新控制器
 * 处理 /api/token/refresh 端点的令牌刷新请求
 * 
 * @author syc
 */
@RestController
@RequestMapping("/api/token")
public class TokenRefreshController {

    private static final Logger logger = LoggerFactory.getLogger(TokenRefreshController.class);

    private final RefreshTokenService refreshTokenService;
    private final ObjectMapper objectMapper;

    public TokenRefreshController(RefreshTokenService refreshTokenService, ObjectMapper objectMapper) {
        this.refreshTokenService = refreshTokenService;
        this.objectMapper = objectMapper;
    }

    /**
     * 刷新访问令牌端点
     * 支持 JSON 和表单数据两种请求格式
     * 
     * @param request HTTP 请求
     * @return 新的令牌对或错误响应
     */
    @PostMapping(value = "/refresh", 
                consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_FORM_URLENCODED_VALUE})
    public ResponseEntity<Map<String, Object>> refreshToken(HttpServletRequest request) {
        try {
            // 提取刷新令牌
            String refreshToken = extractRefreshToken(request);
            
            if (!StringUtils.hasText(refreshToken)) {
                return createErrorResponse(HttpStatus.BAD_REQUEST, "missing_refresh_token", "刷新令牌不能为空");
            }

            // 确定令牌类型偏好
            TokenType preferredType = determineTokenType(request);

            // 验证并刷新令牌
            RefreshTokenValidationResult validationResult = refreshTokenService.validateRefreshToken(refreshToken);
            if (!validationResult.valid()) {
                logger.warn("刷新令牌验证失败: {}", validationResult.errorMessage());
                return createErrorResponse(HttpStatus.UNAUTHORIZED, "invalid_refresh_token", validationResult.errorMessage());
            }

            // 生成新的令牌对
            TokenPair tokenPair = refreshTokenService.refreshAccessToken(refreshToken, preferredType);

            // 构建成功响应
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("access_token", tokenPair.accessToken());
            responseData.put("refresh_token", tokenPair.refreshToken());
            responseData.put("token_type", tokenPair.tokenType().name().toLowerCase());
            responseData.put("expires_in", tokenPair.expiresIn());
            responseData.put("timestamp", Instant.now().toString());

            logger.debug("令牌刷新成功，用户: {}", validationResult.username());
            return ResponseEntity.ok(responseData);

        } catch (IllegalArgumentException e) {
            logger.warn("令牌刷新请求无效: {}", e.getMessage());
            return createErrorResponse(HttpStatus.UNAUTHORIZED, "invalid_refresh_token", e.getMessage());
        } catch (Exception e) {
            logger.error("令牌刷新过程中发生错误", e);
            return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "server_error", "服务器内部错误");
        }
    }

    /**
     * 从请求中提取刷新令牌
     * 支持 JSON 和表单数据格式
     * 
     * @param request HTTP 请求
     * @return 刷新令牌
     */
    private String extractRefreshToken(HttpServletRequest request) {
        String contentType = request.getContentType();
        
        if (contentType != null && contentType.contains(MediaType.APPLICATION_JSON_VALUE)) {
            // 处理 JSON 请求
            return extractRefreshTokenFromJson(request);
        } else {
            // 处理表单数据请求
            return request.getParameter("refresh_token");
        }
    }

    /**
     * 从 JSON 请求体中提取刷新令牌
     * 
     * @param request HTTP 请求
     * @return 刷新令牌
     */
    private String extractRefreshTokenFromJson(HttpServletRequest request) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> requestBody = objectMapper.readValue(request.getInputStream(), Map.class);
            Object refreshToken = requestBody.get("refresh_token");
            return refreshToken != null ? refreshToken.toString() : null;
        } catch (IOException e) {
            logger.warn("解析 JSON 请求体失败", e);
            return null;
        }
    }

    /**
     * 根据请求确定令牌类型偏好
     * 
     * @param request HTTP 请求
     * @return 令牌类型
     */
    private TokenType determineTokenType(HttpServletRequest request) {
        // 检查 Accept 头
        String acceptHeader = request.getHeader("Accept");
        if (acceptHeader != null && acceptHeader.contains("application/jwt")) {
            return TokenType.JWT;
        }
        
        // 检查自定义参数
        String tokenTypeParam = request.getParameter("token_type");
        if ("jwt".equalsIgnoreCase(tokenTypeParam)) {
            return TokenType.JWT;
        }
        
        // 检查自定义头
        String tokenTypeHeader = request.getHeader("X-Token-Type");
        if ("jwt".equalsIgnoreCase(tokenTypeHeader)) {
            return TokenType.JWT;
        }
        
        // 默认返回 UUID 类型
        return TokenType.UUID;
    }

    /**
     * 创建错误响应
     * 
     * @param status HTTP 状态码
     * @param error 错误代码
     * @param errorDescription 错误描述
     * @return 错误响应
     */
    private ResponseEntity<Map<String, Object>> createErrorResponse(HttpStatus status, String error, String errorDescription) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("error", error);
        errorResponse.put("error_description", errorDescription);
        errorResponse.put("timestamp", Instant.now().toString());
        
        return ResponseEntity.status(status).body(errorResponse);
    }
}