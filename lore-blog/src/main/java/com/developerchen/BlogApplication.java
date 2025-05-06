package com.developerchen;

import com.developerchen.core.system.extension.DecryptJdbcPassword;
import com.developerchen.core.system.initializer.DatabaseInitializer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.AutoConfigurationExcludeFilter;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.TypeExcludeFilter;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.event.ContextClosedEvent;

/**
 * 配置@ComponentScan注解是为了优先扫描core包
 *
 * @author syc
 */
@Slf4j
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
public class BlogApplication {

    public static void main(String[] args) {
        long start = System.currentTimeMillis();
        new SpringApplicationBuilder()
                .listeners(new DecryptJdbcPassword(), new DatabaseInitializer())
                .sources(BlogApplication.class)
                .run(args);
        long end = System.currentTimeMillis();
        log.info("======== 应用启动完成, 用时: {}ms ========", end - start);

        Runtime.getRuntime().addShutdownHook(new Thread(
                () -> log.info("======== JVM 关闭钩子触发 ========")
        ));
    }

    @Bean
    ApplicationRunner runner() {
        return args -> {
            // Do something after application running
        };
    }

    @Bean
    public ApplicationListener<ContextClosedEvent> closedEventListener() {
        return event -> {
            log.info("======== 应用已关闭，应用上下文已销毁 ========");
        };
    }

}
