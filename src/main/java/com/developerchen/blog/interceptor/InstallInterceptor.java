package com.developerchen.blog.interceptor;

import com.developerchen.blog.constant.BlogConst;
import com.developerchen.core.config.AppConfig;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.List;

/**
 * 如果识别到应用没有被安装则跳转到安装页面
 *
 * @author syc
 */
public final class InstallInterceptor implements HandlerInterceptor {

    /**
     * Create a new InstallInterceptor instance.
     */
    public InstallInterceptor() {
    }

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {
        String uri = request.getRequestURI();
        if (BlogConst.HAS_INSTALLED) {
            if (!uri.startsWith(BlogConst.INSTALL_URI)) {
                return true;
            }
            String method = request.getMethod().toUpperCase();
            String getMethod = "GET";
            if (getMethod.equals(method)) {
                return true;
            } else {
                // 处理人为构造的非法请求, 返回错误代码或者重定向到安装页面提示错误信息
                String headerValue = request.getHeader("X-Requested-With");
                String ajaxHeader = "XMLHttpRequest";
                if (ajaxHeader.equals(headerValue)) {
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST,
                            "已经安装过了, 不需要重复安装. ");
                } else {
                    response.sendRedirect(BlogConst.INSTALL_URI);
                }
                return false;
            }
        }

        String errorUriPrefix = "/error";
        String staticUriPrefix = AppConfig.staticPathPattern.substring(0,
                AppConfig.staticPathPattern.lastIndexOf("/"));
        List<String> excludeUriList = Arrays.asList(
                staticUriPrefix,
                errorUriPrefix,
                BlogConst.INSTALL_URI);

        for (String excludeUri : excludeUriList) {
            if (uri.startsWith(excludeUri)) {
                return true;
            }
        }
        response.sendRedirect(BlogConst.INSTALL_URI);
        return false;
    }
}
