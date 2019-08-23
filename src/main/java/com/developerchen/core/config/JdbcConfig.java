package com.developerchen.core.config;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.spring.boot.autoconfigure.DruidDataSourceAutoConfigure;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanInitializationException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.Ordered;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.jdbc.datasource.AbstractDataSource;
import org.springframework.lang.Nullable;

import java.sql.Connection;
import java.sql.SQLException;


/**
 * 数据库配置类
 *
 * @author syc
 */
@Configuration
@Import(JdbcConfig.Registrar.class)
@AutoConfigureBefore(DruidDataSourceAutoConfigure.class)
@ConditionalOnProperty(name = "spring.datasource.initialization-mode")
public class JdbcConfig {

    static class Registrar implements ImportBeanDefinitionRegistrar {

        @Override
        public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata,
                                            BeanDefinitionRegistry registry) {
            GenericBeanDefinition beanDefinition = new GenericBeanDefinition();
            beanDefinition.setBeanClass(LazyDataSourceInitPostProcessor.class);
            beanDefinition.setRole(BeanDefinition.ROLE_INFRASTRUCTURE);
            beanDefinition.setSynthetic(true);
            registry.registerBeanDefinition("lazyDataSourceInitPostProcessor", beanDefinition);
        }
    }

    /**
     * 推迟{@link DruidDataSource#init()}方法的执行, 以确保数据库被初始化后再初始化连接池.
     */
    static class LazyDataSourceInitPostProcessor implements BeanPostProcessor, Ordered {

        @Override
        public int getOrder() {
            // 比 Spring 的 DataSourceInitializerPostProcessor.java 低一个优先级
            return Ordered.HIGHEST_PRECEDENCE + 2;
        }

        @Override
        public Object postProcessBeforeInitialization(@Nullable Object bean, String beanName)
                throws BeansException {
            if (bean instanceof DruidDataSource) {
                return new FakeDataSource(bean);
            }
            return bean;
        }

        @Override
        public Object postProcessAfterInitialization(@Nullable Object bean, String beanName)
                throws BeansException {
            if (bean instanceof FakeDataSource) {
                FakeDataSource fakeDataSource = (FakeDataSource) bean;
                Object object = fakeDataSource.getObject();
                if (object instanceof DruidDataSource) {
                    DruidDataSource druidDataSource = (DruidDataSource) object;
                    try {
                        // Spring 的 DataSourceInitializerInvoker.java 已经完成好了数据库的初始化
                        // 工作, 可以真正的初始化连接池了
                        druidDataSource.init();
                    } catch (SQLException e) {
                        throw new BeanInitializationException("Druid initialization exception.", e);
                    }
                }
                return object;
            }

            return bean;
        }
    }

    static class FakeDataSource extends AbstractDataSource {

        // 存放原连接池对象
        private Object object;

        private FakeDataSource(Object object) {
            this.object = object;
        }

        public void init() {
            // 什么也不做, 就是为了代替原连接池的init方法被调用
        }

        public Object getObject() {
            return object;
        }


        @Override
        public Connection getConnection() {
            return null;
        }

        @Override
        public Connection getConnection(String username, String password) {
            return null;
        }
    }

}
