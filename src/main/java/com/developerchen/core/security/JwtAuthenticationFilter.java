package com.developerchen.core.security;

import com.developerchen.core.util.JsonUtils;
import com.developerchen.core.util.RequestUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.lang.Nullable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;

public class JwtAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    boolean isAjaxRequest = false;

    private Map<String, String> parameterMap = Collections.emptyMap();

    public JwtAuthenticationFilter() {
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request,
                                                HttpServletResponse response) throws AuthenticationException {
        if (RequestUtils.isAjaxRequest(request)) {
            isAjaxRequest = true;
            try {
                parameterMap = JsonUtils.getObjectMapper().readValue(request.getInputStream(),
                        new TypeReference<Map<String, String>>() {
                        });
            } catch (IOException e) {
                // ignore
            }
        }
        return super.attemptAuthentication(request, response);
    }

    @Override
    @Nullable
    public String obtainUsername(HttpServletRequest request) {
        if (isAjaxRequest) {
            return parameterMap.get(getUsernameParameter());
        }
        return request.getParameter(getUsernameParameter());
    }

    @Override
    @Nullable
    public String obtainPassword(HttpServletRequest request) {
        if (isAjaxRequest) {
            return parameterMap.get(getPasswordParameter());
        }
        return request.getParameter(getPasswordParameter());
    }
}
