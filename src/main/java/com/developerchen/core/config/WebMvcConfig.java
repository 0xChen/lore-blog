package com.developerchen.core.config;

import com.developerchen.core.exception.ErrorViewResolver;
import org.springframework.boot.autoconfigure.web.ResourceProperties;
import org.springframework.boot.autoconfigure.web.servlet.error.DefaultErrorViewResolver;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Spring MVC 配置类
 *
 * @author syc
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/robots.txt")
                .addResourceLocations("classpath:/static/robots.txt");

        registry.addResourceHandler("/favicon.ico")
                .addResourceLocations("classpath:/static/favicon.ico");

    }

    @Bean
    public DefaultErrorViewResolver conventionErrorViewResolver(ApplicationContext applicationContext,
                                                                ResourceProperties resourceProperties) {
        return new ErrorViewResolver(applicationContext, resourceProperties);
    }

}
