package com.developerchen.blog.interceptor;

import com.developerchen.blog.constant.BlogConst;
import com.developerchen.core.config.AppConfig;
import com.developerchen.core.util.RequestUtils;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
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
                             @NonNull HttpServletResponse response,
                             @Nullable Object handler) throws Exception {
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
                if (RequestUtils.isAjaxRequest(request)) {
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
        if (RequestUtils.isAjaxRequest(request)) {
            String installUrl = AppConfig.scheme + "://" + AppConfig.hostname + BlogConst.INSTALL_URI;
            String errorMessage = "网站没有执行过安装初始化步骤, 需要访问 " + installUrl + " 执行安装程序";
            response.sendError(611, errorMessage);
            return false;
        }
        response.sendRedirect(BlogConst.INSTALL_URI);
        return false;
    }
}
