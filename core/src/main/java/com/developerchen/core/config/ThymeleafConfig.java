package com.developerchen.core.config;

import com.developerchen.core.extension.ThymeleafDialect;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Thymeleaf 配置类
 *
 * @author syc
 */
@Configuration
public class ThymeleafConfig {
    /**
     * 扩展 Thymeleaf 的工具方法
     */
    @Bean
    @ConditionalOnMissingBean
    public ThymeleafDialect thymeleafDialect() {
        return new ThymeleafDialect();
    }
}
