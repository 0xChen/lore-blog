package com.developerchen.core.system.security;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.server.resource.BearerTokenError;
import org.springframework.security.oauth2.server.resource.BearerTokenErrors;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.util.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 支持从 Cookie 提取 Token（优先级最低）
 *
 * @author syc
 */
public final class DefaultBearerTokenResolver implements BearerTokenResolver {

    private static final String ACCESS_TOKEN_PARAMETER_NAME = "access_token";
    private static final String ACCESS_TOKEN_COOKIE_NAME = "access_token"; // 新增常量

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

    // Cookie 解析
    private String resolveFromCookie(HttpServletRequest request) {
        if (!StringUtils.hasText(this.accessTokenCookieName)) {
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

    // 统一验证方法命名
    private void validateTokenUniqueness(String parameterToken, String cookieToken) {
        boolean hasConflict = (parameterToken != null) || (cookieToken != null);
        if (hasConflict) {
            BearerTokenError error = BearerTokenErrors.invalidRequest("Multiple authentication tokens detected");
            throw new OAuth2AuthenticationException(error);
        }
    }

    // 保持参数令牌验证逻辑
    private void validateParameterToken(String parameterToken) {
        if (!StringUtils.hasText(parameterToken)) {
            BearerTokenError error = BearerTokenErrors.invalidRequest("Empty parameter token");
            throw new OAuth2AuthenticationException(error);
        }
    }

    // 新增 Cookie 配置方法（统一命名风格）
    public void setAccessTokenCookieName(String accessTokenCookieName) {
        this.accessTokenCookieName = accessTokenCookieName;
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

    private boolean isParameterTokenSupportedForRequest(final HttpServletRequest request) {
        return isFormEncodedRequest(request) || isGetRequest(request);
    }

    private static boolean isGetRequest(HttpServletRequest request) {
        return HttpMethod.GET.name().equals(request.getMethod());
    }

    private static boolean isFormEncodedRequest(HttpServletRequest request) {
        return MediaType.APPLICATION_FORM_URLENCODED_VALUE.equals(request.getContentType());
    }

    private static boolean hasAccessTokenInQueryString(HttpServletRequest request) {
        return (request.getQueryString() != null) && request.getQueryString().contains(ACCESS_TOKEN_PARAMETER_NAME);
    }

    private boolean isParameterTokenEnabledForRequest(HttpServletRequest request) {
        return ((this.allowFormEncodedBodyParameter && isFormEncodedRequest(request) && !isGetRequest(request)
                && !hasAccessTokenInQueryString(request)) || (this.allowUriQueryParameter && isGetRequest(request)));
    }
}
