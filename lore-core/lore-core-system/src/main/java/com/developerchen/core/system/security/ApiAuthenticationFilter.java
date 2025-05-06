package com.developerchen.core.system.security;

import com.developerchen.core.common.util.JsonUtils;
import com.developerchen.core.common.util.RequestUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.Nullable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

/**
 * 该过滤器用于处理API认证请求，从请求参数中获取用户名和密码，并尝试进行认证。
 *
 * @author syc
 */
public class ApiAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private static final String API_AUTH_PARAMS_ATTR = "API_AUTH_PARAMS";

    public ApiAuthenticationFilter() {
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request,
                                                HttpServletResponse response)
            throws AuthenticationException {
        if (RequestUtils.isApiRequest(request)) {
            cacheAuthParameters(request);
        }
        return super.attemptAuthentication(request, response);
    }

    /**
     * 缓存认证参数到HttpServletRequest中
     * 该方法尝试从请求输入流中解析认证参数，并将其缓存到请求属性中
     * 如果解析失败，将记录警告日志，并将空字典作为认证参数缓存到请求属性中
     *
     * @param request HTTP请求对象，用于获取输入流和设置认证参数属性
     */
    private void cacheAuthParameters(HttpServletRequest request) {
        try {
            // 从请求输入流中读取并解析认证参数
            Map<String, String> authParams = JsonUtils.getObjectMapper().readValue(
                    request.getInputStream(),
                    new TypeReference<>() {
                    }
            );
            // 将解析后的认证参数缓存到请求属性中
            request.setAttribute(API_AUTH_PARAMS_ATTR, authParams);
        } catch (IOException e) {
            // 如果解析失败，记录警告日志
            logger.warn("Failed to parse authentication parameters from request", e);
            // 将空字典作为认证参数缓存到请求属性中
            request.setAttribute(API_AUTH_PARAMS_ATTR, Collections.emptyMap());
        }
    }


    @Override
    @Nullable
    protected String obtainUsername(HttpServletRequest request) {
        return getAuthParameter(request, getUsernameParameter());
    }

    @Override
    @Nullable
    protected String obtainPassword(HttpServletRequest request) {
        return getAuthParameter(request, getPasswordParameter());
    }

    @Nullable
    private String getAuthParameter(HttpServletRequest request, String parameterName) {
        Map<String, String> ajaxParams = getAjaxAuthParams(request);
        return ajaxParams != null ? ajaxParams.get(parameterName) : request.getParameter(parameterName);
    }

    @SuppressWarnings("unchecked")
    private Map<String, String> getAjaxAuthParams(HttpServletRequest request) {
        Object params = request.getAttribute(API_AUTH_PARAMS_ATTR);
        if (params instanceof Map) {
            return (Map<String, String>) params;
        }
        return null;
    }
}
