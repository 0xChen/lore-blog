package com.developerchen.blog.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

/**
 * 用于引入各类自定义的配置文件
 */
@Configuration
@PropertySource("classpath:jdbc.properties")
public class PropertySourcesConfig {
}
