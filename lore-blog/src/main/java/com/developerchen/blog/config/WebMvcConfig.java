package com.developerchen.blog.config;

import com.developerchen.blog.interceptor.InstallInterceptor;
import com.developerchen.blog.interceptor.ThemeInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Spring MVC 配置类
 *
 * @author syc
 */
@Configuration("WebMvcConfig_blog")
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new InstallInterceptor());
        registry.addInterceptor(new ThemeInterceptor());
    }
}
