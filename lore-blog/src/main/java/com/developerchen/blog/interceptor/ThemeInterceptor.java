package com.developerchen.blog.interceptor;

import com.developerchen.blog.theme.Common;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.Nullable;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

/**
 * 根据Blog设置的主题更改controller返回的视图名称
 *
 * @author syc
 */
public final class ThemeInterceptor implements HandlerInterceptor {

    private static final String PLACEHOLDER = "{theme}";

    /**
     * Create a new ThemeInterceptor instance.
     */
    public ThemeInterceptor() {
    }

    @Override
    public void postHandle(@Nullable HttpServletRequest request,
                           @Nullable HttpServletResponse response,
                           @Nullable Object handler,
                           @Nullable ModelAndView modelAndView) {

        if (modelAndView != null) {
            String viewName = modelAndView.getViewName();
            String themeName = Common.blogTheme();

            if (viewName != null && viewName.contains(PLACEHOLDER)) {
                viewName = viewName.replace(PLACEHOLDER, themeName);
                modelAndView.setViewName(viewName);
            }
        }
    }
}
