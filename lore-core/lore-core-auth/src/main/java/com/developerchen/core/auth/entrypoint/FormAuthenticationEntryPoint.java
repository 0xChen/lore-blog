package com.developerchen.core.auth.entrypoint;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 表单认证入口点，重定向到登录页面
 */
@Component
public class FormAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final LoginUrlAuthenticationEntryPoint loginUrlEntryPoint;

    public FormAuthenticationEntryPoint() {
        this.loginUrlEntryPoint = new LoginUrlAuthenticationEntryPoint("/login");
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                        AuthenticationException authException) throws IOException, ServletException {
        
        loginUrlEntryPoint.commence(request, response, authException);
    }
}