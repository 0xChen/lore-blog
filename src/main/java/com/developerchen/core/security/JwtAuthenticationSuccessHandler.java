package com.developerchen.core.security;

import com.developerchen.core.constant.Const;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * 登陆成功处理
 * 如果是AJAX请求则返回 true, 否则重定向到指定url
 * 由于还没有前后端分离，暂直接将token写入cookie, 每次请求时由浏览器自动带入
 *
 * @author syc
 */
public class JwtAuthenticationSuccessHandler extends
        SimpleUrlAuthenticationSuccessHandler {
    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationSuccessHandler.class);

    public JwtAuthenticationSuccessHandler() {
    }

    public JwtAuthenticationSuccessHandler(String defaultTargetUrl) {
        super(defaultTargetUrl);
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication)
            throws ServletException, IOException {

        JwtUser jwtUser = (JwtUser) authentication.getPrincipal();
        String token = JwtTokenUtil.generateToken(jwtUser);

        Cookie cookie = new Cookie(Const.COOKIE_ACCESS_TOKEN, token);
        cookie.setMaxAge((int) (JwtTokenUtil.EXPIRE_TIME / 1000));
        cookie.setPath(getCookiePath(request));
        cookie.setSecure(request.isSecure());
        cookie.setHttpOnly(true);

        response.addCookie(cookie);
        if (isAjaxRequest(request)) {
            PrintWriter pw = response.getWriter();
            pw.print(true);
            pw.flush();
        } else {
            // Redirect url
            super.onAuthenticationSuccess(request, response, authentication);
        }
    }

    private String getCookiePath(HttpServletRequest request) {
        String contextPath = request.getContextPath();
        return contextPath.length() > 0 ? contextPath : "/";
    }

    private boolean isAjaxRequest(HttpServletRequest request) {
        String ajaxHeader = request.getHeader("X-Requested-With");
        return "XMLHttpRequest".equals(ajaxHeader);
    }
}
