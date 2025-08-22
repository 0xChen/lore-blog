package com.developerchen.core.auth.handler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 表单认证失败处理器
 * 处理表单登录失败后的重定向逻辑
 * 
 * @author syc
 */
@Component
public class FormAuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    public FormAuthenticationFailureHandler() {
        // 设置默认的失败跳转URL
        setDefaultFailureUrl("/login?error");
        // 允许会话创建
        setAllowSessionCreation(true);
        // 使用转发而不是重定向（可选）
        setUseForward(false);
    }

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                      AuthenticationException exception) throws IOException, ServletException {
        
        // 将错误信息添加到会话中，以便在登录页面显示
        request.getSession().setAttribute("SPRING_SECURITY_LAST_EXCEPTION", exception);
        
        // 调用父类的默认处理逻辑
        super.onAuthenticationFailure(request, response, exception);
    }
}