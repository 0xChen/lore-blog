package com.developerchen.core.config;

import org.jetbrains.annotations.NotNull;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.sql.init.SqlDataSourceScriptDatabaseInitializer;
import org.springframework.boot.autoconfigure.sql.init.SqlInitializationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;
import org.springframework.lang.Nullable;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * 数据库配置类
 *
 * @author syc
 */
@Configuration
@ConditionalOnProperty(name = "spring.sql.init.mode")
public class JdbcConfig {

    @Bean
    @ConditionalOnBean(SqlInitializationProperties.class)
    CreateDatabasePostProcessor createDatabasePostProcessor(DataSource dataSource,
                                                            SqlInitializationProperties properties) {
        return new CreateDatabasePostProcessor(dataSource, properties);
    }

    /**
     * 初始化数据库之前创建数据库。
     */
    static class CreateDatabasePostProcessor implements BeanPostProcessor {

        private final DataSource dataSource;
        private final SqlInitializationProperties properties;

        public CreateDatabasePostProcessor(DataSource dataSource, SqlInitializationProperties properties) {
            this.dataSource = dataSource;
            this.properties = properties;
        }


        @Override
        public Object postProcessBeforeInitialization(@Nullable Object bean, @NotNull String beanName)
                throws BeansException {
            if (bean instanceof SqlDataSourceScriptDatabaseInitializer) {
                // 创建数据库
                createDatabase();
            }
            return bean;
        }

        @Override
        public Object postProcessAfterInitialization(@Nullable Object bean, @NotNull String beanName)
                throws BeansException {
            return bean;
        }

        private void createDatabase() {
            // 创建连接池
            SimpleDriverDataSource simpleDriverDataSource = DataSourceBuilder.derivedFrom(dataSource)
                    .username(properties.getUsername())
                    .password(properties.getPassword())
                    .type(SimpleDriverDataSource.class)
                    .build();
            String url = simpleDriverDataSource.getUrl();
            Pattern p = Pattern.compile("jdbc:[^/]+://[^/]+:\\d+");
            Matcher m = p.matcher(Objects.requireNonNull(url));
            String newUrl = url;
            if (m.find()) {
                newUrl = m.group();
            }
            simpleDriverDataSource.setUrl(newUrl);

            // 创建数据库
            String schema = url.replace(newUrl + "/", "");
            if (schema.contains("?")) {
                schema = schema.substring(0, schema.indexOf("?"));
            }

            String sql = "CREATE DATABASE IF NOT EXISTS " + schema + " DEFAULT CHARSET utf8mb4 COLLATE utf8mb4_0900_ai_ci;";
            try {
                Connection connection = DataSourceUtils.getConnection(simpleDriverDataSource);
                try {
                    connection.createStatement().execute(sql);
                    if (!connection.getAutoCommit() && !DataSourceUtils.isConnectionTransactional(connection, dataSource)) {
                        connection.commit();
                    }
                } finally {
                    DataSourceUtils.releaseConnection(connection, dataSource);
                }
            } catch (Throwable ex) {
                throw new RuntimeException("无法创建数据库。", ex);
            }

        }


    }
}
