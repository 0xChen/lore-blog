package com.developerchen.core.system.security;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.server.resource.BearerTokenError;
import org.springframework.security.oauth2.server.resource.BearerTokenErrors;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 多格式令牌解析器，支持JWT和自定义令牌格式
 * 可以通过令牌格式自动识别或通过header标记识别令牌格式
 *
 * @author syc
 */
public class MultiFormatTokenResolver implements BearerTokenResolver {

    private static final Logger logger = LoggerFactory.getLogger(MultiFormatTokenResolver.class);
    
    private static final String ACCESS_TOKEN_PARAMETER_NAME = "access_token";
    private static final String ACCESS_TOKEN_COOKIE_NAME = "access_token";
    private static final String TOKEN_TYPE_HEADER = "X-Token-Type";
    private static final String JWT_TOKEN_TYPE = "jwt";
    private static final String CUSTOM_TOKEN_TYPE = "custom";

    private static final Pattern authorizationPattern = Pattern.compile("^Bearer (?<token>[a-zA-Z0-9-._~+/]+=*)$",
            Pattern.CASE_INSENSITIVE);

    private boolean allowFormEncodedBodyParameter = false;
    private boolean allowUriQueryParameter = false;
    private String bearerTokenHeaderName = HttpHeaders.AUTHORIZATION;
    private String accessTokenCookieName = ACCESS_TOKEN_COOKIE_NAME;

    @Override
    public String resolve(final HttpServletRequest request) {
        final String authorizationHeaderToken = resolveFromAuthorizationHeader(request);
        final String parameterToken = isParameterTokenSupportedForRequest(request)
                ? resolveFromRequestParameters(request) : null;
        final String cookieToken = resolveFromCookie(request);

        // 1. 验证 Header 令牌
        if (authorizationHeaderToken != null) {
            validateTokenUniqueness(parameterToken, cookieToken);
            return authorizationHeaderToken;
        }

        // 2. 验证参数令牌
        if (parameterToken != null && isParameterTokenEnabledForRequest(request)) {
            validateTokenUniqueness(null, cookieToken);
            validateParameterToken(parameterToken);
            return parameterToken;
        }

        // 3. 最后检查 Cookie（优先级最低）
        if (cookieToken != null) {
            return cookieToken;
        }

        return null;
    }

    /**
     * 获取令牌类型，通过header标记或自动识别
     * 
     * @param request HTTP请求
     * @param token 令牌字符串
     * @return 令牌类型（"jwt"或"custom"）
     */
    public String resolveTokenType(HttpServletRequest request, String token) {
        // 1. 首先尝试从header中获取令牌类型
        String tokenType = request.getHeader(TOKEN_TYPE_HEADER);
        if (StringUtils.isNotBlank(tokenType)) {
            if (JWT_TOKEN_TYPE.equalsIgnoreCase(tokenType) || CUSTOM_TOKEN_TYPE.equalsIgnoreCase(tokenType)) {
                return tokenType.toLowerCase();
            }
        }
        
        // 2. 自动识别令牌类型
        return detectTokenType(token);
    }
    
    /**
     * 自动检测令牌类型
     * 
     * @param token 令牌字符串
     * @return 令牌类型（"jwt"或"custom"）
     */
    private String detectTokenType(String token) {
        if (token == null) {
            return null;
        }
        
        // JWT通常由三部分组成，用点号分隔
        if (token.matches("^[A-Za-z0-9-_]+\\.[A-Za-z0-9-_]+\\.[A-Za-z0-9-_]*$")) {
            return JWT_TOKEN_TYPE;
        } else {
            return CUSTOM_TOKEN_TYPE;
        }
    }

    private String resolveFromAuthorizationHeader(HttpServletRequest request) {
        String authorization = request.getHeader(this.bearerTokenHeaderName);
        if (!StringUtils.startsWithIgnoreCase(authorization, "bearer")) {
            return null;
        }
        Matcher matcher = authorizationPattern.matcher(authorization);
        if (!matcher.matches()) {
            BearerTokenError error = BearerTokenErrors.invalidToken("Bearer token is malformed");
            throw new OAuth2AuthenticationException(error);
        }
        return matcher.group("token");
    }

    private static String resolveFromRequestParameters(HttpServletRequest request) {
        String[] values = request.getParameterValues(ACCESS_TOKEN_PARAMETER_NAME);
        if (values == null || values.length == 0) {
            return null;
        }
        if (values.length == 1) {
            return values[0];
        }
        BearerTokenError error = BearerTokenErrors.invalidRequest("Found multiple bearer tokens in the request");
        throw new OAuth2AuthenticationException(error);
    }

    private String resolveFromCookie(HttpServletRequest request) {
        if (StringUtils.isBlank(this.accessTokenCookieName)) {
            return null;
        }
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }
        for (Cookie cookie : cookies) {
            if (this.accessTokenCookieName.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }

    private void validateTokenUniqueness(String parameterToken, String cookieToken) {
        boolean hasConflict = (parameterToken != null) || (cookieToken != null);
        if (hasConflict) {
            BearerTokenError error = BearerTokenErrors.invalidRequest("Multiple authentication tokens detected");
            throw new OAuth2AuthenticationException(error);
        }
    }

    private void validateParameterToken(String parameterToken) {
        if (StringUtils.isBlank(parameterToken)) {
            BearerTokenError error = BearerTokenErrors.invalidRequest("Empty parameter token");
            throw new OAuth2AuthenticationException(error);
        }
    }

    private boolean isParameterTokenSupportedForRequest(HttpServletRequest request) {
        return (this.allowFormEncodedBodyParameter && "POST".equals(request.getMethod())
                && request.getContentType() != null
                && request.getContentType().contains("application/x-www-form-urlencoded"))
                || (this.allowUriQueryParameter && "GET".equals(request.getMethod()));
    }

    private boolean isParameterTokenEnabledForRequest(HttpServletRequest request) {
        return this.allowFormEncodedBodyParameter || this.allowUriQueryParameter;
    }

    public void setAllowFormEncodedBodyParameter(boolean allowFormEncodedBodyParameter) {
        this.allowFormEncodedBodyParameter = allowFormEncodedBodyParameter;
    }

    public void setAllowUriQueryParameter(boolean allowUriQueryParameter) {
        this.allowUriQueryParameter = allowUriQueryParameter;
    }

    public void setBearerTokenHeaderName(String bearerTokenHeaderName) {
        this.bearerTokenHeaderName = bearerTokenHeaderName;
    }

    public void setAccessTokenCookieName(String accessTokenCookieName) {
        this.accessTokenCookieName = accessTokenCookieName;
    }
}
