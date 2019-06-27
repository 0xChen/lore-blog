package com.developerchen;

import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.AutoConfigurationExcludeFilter;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.TypeExcludeFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 配置@ComponentScan注解是为了优先扫描core包
 *
 * @author syc
 */
@SpringBootApplication
@ComponentScan(
        basePackages = {
                "com.developerchen.core",
                "com.developerchen.*"
        },
        excludeFilters = {
                @ComponentScan.Filter(type = FilterType.CUSTOM, classes = TypeExcludeFilter.class),
                @ComponentScan.Filter(type = FilterType.CUSTOM, classes = AutoConfigurationExcludeFilter.class)
        })
@EnableAsync
@EnableScheduling
public class BlogApplication {

    public static void main(String[] args) {
        long start = System.currentTimeMillis();
        SpringApplication.run(BlogApplication.class, args);
        long end = System.currentTimeMillis();
        System.out.println("应用启动完成, 用时: " + (end - start) + "ms");
    }

    @Bean
    ApplicationRunner runner() {
        return args -> {
            // Do something after application running
        };
    }

}
