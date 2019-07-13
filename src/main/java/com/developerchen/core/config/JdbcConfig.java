package com.developerchen.core.config;

import com.developerchen.core.extension.EncryptPropertySourceFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;

/**
 * 数据库连接配置类
 *
 * @author syc
 */
@Configuration
public class JdbcConfig {

    @Profile("dev")
    @Configuration
    @PropertySource(value = {"classpath:jdbc.properties"},
            encoding = "UTF-8")
    class DevJdbcConfig {

    }

    @Profile("prod")
    @Configuration
    @PropertySource(value = {"classpath:jdbc.properties"},
            encoding = "UTF-8",
            factory = EncryptPropertySourceFactory.class)
    class ProdJdbcConfig {

    }
}
