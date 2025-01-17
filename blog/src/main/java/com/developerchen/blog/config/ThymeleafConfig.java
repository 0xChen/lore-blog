package com.developerchen.blog.config;

import com.developerchen.blog.extension.thymeleaf.BlogDialect;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Thymeleaf 配置类
 *
 * @author syc
 */
@Configuration("thymeleafConfig_blog")
public class ThymeleafConfig {
    /**
     * 扩展 Thymeleaf 的工具方法
     */
    @Bean
    @ConditionalOnMissingBean
    public BlogDialect blogDialect() {
        return new BlogDialect();
    }
}
