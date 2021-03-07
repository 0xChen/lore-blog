package com.developerchen.core.exception;

import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.boot.autoconfigure.web.servlet.error.DefaultErrorViewResolver;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * 错误视图解析器
 * 继承了{@link org.springframework.boot.autoconfigure.web.servlet.error.ErrorViewResolver}
 * 在此基础上增加一个错误页面, 当 ErrorViewResolver 没有搜索到合适的 templates 或者 static assets
 * 时将返回{@code '/<templates>/error/error.html'}视图
 *
 * @author syc
 */
public class ErrorViewResolver extends DefaultErrorViewResolver {

    private String errorPage = "error/error";

    /**
     * Create a new {@link DefaultErrorViewResolver} instance.
     *
     * @param applicationContext the source application context
     * @param resources          resource properties
     */
    public ErrorViewResolver(ApplicationContext applicationContext, WebProperties.Resources resources) {
        super(applicationContext, resources);
    }

    @Override
    public ModelAndView resolveErrorView(HttpServletRequest request, HttpStatus status,
                                         Map<String, Object> model) {
        ModelAndView modelAndView = super.resolveErrorView(request, status, model);
        if (modelAndView == null) {
            modelAndView = new ModelAndView(errorPage, model, status);
        }
        return modelAndView;
    }

    public String getErrorPage() {
        return errorPage;
    }

    public void setErrorPage(String errorPage) {
        this.errorPage = errorPage;
    }
}
