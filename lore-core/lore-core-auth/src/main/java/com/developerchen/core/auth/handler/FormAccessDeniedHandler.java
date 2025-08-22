package com.developerchen.core.auth.handler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.access.AccessDeniedHandlerImpl;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 表单访问拒绝处理器，重定向到错误页面
 */
@Component
public class FormAccessDeniedHandler implements AccessDeniedHandler {

    private final AccessDeniedHandlerImpl accessDeniedHandler;

    public FormAccessDeniedHandler() {
        this.accessDeniedHandler = new AccessDeniedHandlerImpl();
        this.accessDeniedHandler.setErrorPage("/error/403");
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                      org.springframework.security.access.AccessDeniedException accessDeniedException) 
                      throws IOException, ServletException {
        
        accessDeniedHandler.handle(request, response, accessDeniedException);
    }
}