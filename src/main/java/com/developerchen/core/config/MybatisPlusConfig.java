package com.developerchen.core.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.baomidou.mybatisplus.extension.plugins.PaginationInterceptor;
import com.developerchen.core.domain.entity.User;
import com.developerchen.core.repository.CoreMapper;
import com.developerchen.core.util.UserUtils;
import org.apache.ibatis.reflection.MetaObject;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.util.Date;

/**
 * mybatis-plus 配置文件。
 * 相关文档：http://mp.baomidou.com
 *
 * Spring 官方关于事务注解的说明:
 * The Spring team recommends that you annotate only concrete classes (and methods of concrete classes)
 * with the @Transactional annotation, as opposed to annotating interfaces.
 *
 * Spring 事务注解文档:
 * https://docs.spring.io/spring/docs/5.2.7.RELEASE/spring-framework-reference/data-access.html#transaction-declarative-annotations
 *
 * @author syc
 */
@EnableTransactionManagement
@Configuration
@MapperScan(basePackages = "com.developerchen", markerInterface = CoreMapper.class)
public class MybatisPlusConfig {

    /**
     * mybatis-plus分页插件，自动识别数据库类型
     */
    @Bean
    public PaginationInterceptor paginationInterceptor() {
        return new PaginationInterceptor();
    }

    /**
     * 注入自定义公共字段自动填充器
     * 实现对 createTime、updateTime 及 createUserId、updateUserId字段的自动填充
     */
    @Bean
    public MetaObjectHandler fieldFillMetaObjectHandler() {
        return new MetaObjectHandler() {
            @Override
            public void insertFill(MetaObject metaObject) {
                String[] fields = new String[]{"createTime", "updateTime"};
                for (String field : fields) {
                    if (metaObject.hasSetter(field)) {
                        setFieldValByName(field, new Date(), metaObject);
                    }
                }
                setUserIdField(metaObject, "createUserId");
            }

            @Override
            public void updateFill(MetaObject metaObject) {
                if (metaObject.hasSetter("updateTime")) {
                    setFieldValByName("updateTime", new Date(), metaObject);
                }
                setUserIdField(metaObject, "updateUserId");
            }

            private void setUserIdField(MetaObject metaObject, String userIdField) {
                if (metaObject.hasSetter(userIdField)) {
                    User user = UserUtils.getUser();
                    if (user != null) {
                        setFieldValByName(userIdField, user.getId(), metaObject);
                    }
                }
            }
        };
    }

}
