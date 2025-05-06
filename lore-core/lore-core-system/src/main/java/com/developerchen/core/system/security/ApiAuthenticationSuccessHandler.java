package com.developerchen.core.system.security;

import com.developerchen.core.common.constant.Const;
import com.developerchen.core.common.domain.R;
import com.developerchen.core.common.util.JsonUtils;
import com.developerchen.core.common.util.RequestUtils;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;

import java.io.IOException;
import java.io.PrintWriter;

/**
 * 登陆成功处理
 * 如果是AJAX请求则返回 true, 否则重定向到指定url
 * 由于还没有前后端分离，暂直接将token写入cookie, 每次请求时由浏览器自动带入
 *
 * @author syc
 */
public class ApiAuthenticationSuccessHandler extends
        SimpleUrlAuthenticationSuccessHandler {
    private static final Logger logger = LoggerFactory.getLogger(ApiAuthenticationSuccessHandler.class);

    public ApiAuthenticationSuccessHandler() {
    }

    public ApiAuthenticationSuccessHandler(String defaultTargetUrl) {
        super(defaultTargetUrl);
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication)
            throws ServletException, IOException {

        ApiUser apiUser = (ApiUser) authentication.getPrincipal();
        String token = JwtTokenUtil.generateToken(apiUser);

        boolean isAjaxRequest = RequestUtils.isApiRequest(request);
        boolean isRememberMe = Boolean.parseBoolean(request.getParameter(Const.REQUEST_REMEMBER_ME));

        if (!isAjaxRequest || isRememberMe) {
            // 记住我
            Cookie cookie = new Cookie(Const.COOKIE_ACCESS_TOKEN, token);
            cookie.setMaxAge((int) (JwtTokenUtil.EXPIRE_TIME / 1000));
            cookie.setPath(getCookiePath(request));
            cookie.setSecure(request.isSecure());
            cookie.setHttpOnly(true);

            response.addCookie(cookie);
        }
        if (isAjaxRequest) {
            response.setCharacterEncoding("UTF-8");
            response.setHeader("Content-Type", "application/json");
            PrintWriter pw = response.getWriter();
            R<String> r = new R<>(true, HttpStatus.OK.value());
            r.setData(token);
            JsonUtils.getObjectMapper().writeValue(pw, r);
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
}
