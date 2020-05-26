package com.developerchen.core.security;

import com.developerchen.core.constant.Const;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 登陆失败处理
 * 如果是AJAX请求则返回 false, 否则重定向到指定url
 *
 * @author syc
 */
public class JwtAuthenticationFailureHandler extends
        SimpleUrlAuthenticationFailureHandler {
    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFailureHandler.class);

    public JwtAuthenticationFailureHandler() {
    }

    public JwtAuthenticationFailureHandler(String defaultFailureUrl) {
        super(defaultFailureUrl);
    }

    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {
        if (logger.isDebugEnabled()) {
            String username = request.getParameter(Const.REQUEST_USER_NAME);
            logger.debug("用户名: " + username + " 登陆失败! " + exception.getMessage());
        }
        if (isAjaxRequest(request)) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, exception.getMessage());
        } else {
            // Redirect url
            super.onAuthenticationFailure(request, response, exception);
        }
    }

    private boolean isAjaxRequest(HttpServletRequest request) {
        return "XMLHttpRequest".equals(request.getHeader("X-Requested-With"));
    }
}
