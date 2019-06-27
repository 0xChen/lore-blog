package com.developerchen.blog.config;

import com.developerchen.blog.interceptor.ThemeInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
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
        registry.addInterceptor(new ThemeInterceptor());
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/admin/pages").setViewName("admin/page/list");
        registry.addViewController("/admin/page").setViewName("admin/page/new");
        registry.addViewController("/admin/page/{pageId}").setViewName("admin/page/edit");

        registry.addViewController("/admin/posts").setViewName("admin/post/list");
        registry.addViewController("/admin/post").setViewName("admin/post/new");
        registry.addViewController("/admin/post/{postId}").setViewName("admin/post/edit");

        registry.addViewController("/admin/links").setViewName("admin/link/list");
        registry.addViewController("/admin/link/").setViewName("admin/link/edit");
        registry.addViewController("/admin/link/{linkId}").setViewName("admin/link/edit");

        registry.addViewController("/admin/comments").setViewName("admin/comment/list");

        registry.addViewController("/admin/categories").setViewName("admin/category/index");

        registry.addViewController("/admin/themes").setViewName("admin/theme/index");
        registry.addViewController("/admin/setting").setViewName("admin/setting");
    }

}
