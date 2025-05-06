package com.developerchen.core.system.security;

import com.developerchen.core.common.util.RequestUtils;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;

import java.io.IOException;

/**
 * 登陆失败处理
 * 如果是AJAX请求则返回 false, 否则重定向到指定url
 *
 * @author syc
 */
public class ApiAuthenticationFailureHandler extends
        SimpleUrlAuthenticationFailureHandler {
    private static final Logger logger = LoggerFactory.getLogger(ApiAuthenticationFailureHandler.class);

    public ApiAuthenticationFailureHandler() {
    }

    public ApiAuthenticationFailureHandler(String defaultFailureUrl) {
        super(defaultFailureUrl);
    }

    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {
        if (RequestUtils.isApiRequest(request)) {
            String message;
            if (exception instanceof BadCredentialsException) {
                message = "用户名或密码错误";
            } else {
                message = exception.getMessage();
            }
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, message);
        } else {
            // Redirect url
            super.onAuthenticationFailure(request, response, exception);
        }
    }
}
