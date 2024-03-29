package com.developerchen.core.initializer;

import com.developerchen.blog.constant.BlogConst;
import com.developerchen.core.exception.AlertException;
import com.developerchen.core.extension.DecryptJdbcPassword;
import com.developerchen.core.util.CommonUtils;
import com.developerchen.core.util.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.boot.sql.init.DatabaseInitializationMode;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.ConfigurableEnvironment;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Map;

/**
 * 通过监听 ApplicationEnvironmentPreparedEvent 事件，在应用未连接数据库前，
 * 通过判断 ./lock/Installed 文件是否存在决定是否需要初始化数据库。
 * 如果 "Installed" 文件存在就更改Environment中key为
 * "spring.datasource.initialization-mode" 的值为 "NEVER" 以阻止执行初始化
 * 数据库的schema。如果不存在还会在配置文件中指定的位置创建一个schema用于将
 * 数据库用户名为 ${spring.datasource.druid.username}的密码修改为
 * ${spring.datasource.druid.password}}解密后的密码。
 *
 * @author syc
 */
public class DatabaseInitializer implements ApplicationListener<ApplicationEnvironmentPreparedEvent> {
    private final Logger logger = LoggerFactory.getLogger(DatabaseInitializer.class);

    @Override
    public void onApplicationEvent(ApplicationEnvironmentPreparedEvent event) {
        String initializationModeKey = "spring.sql.init.mode";
        String changePasswdSchemaKey = "spring.sql.init.schema-locations[1]";

        ConfigurableEnvironment environment = event.getEnvironment();
        if (!environment.containsProperty(initializationModeKey)) {
            return;
        }

        Map<Object, Object> propertyMap = CommonUtils.getPropertyMap(environment, initializationModeKey);

        if (propertyMap != null) {
            if (BlogConst.INSTALLED.exists()) {
                // 关闭初始化脚本
                propertyMap.put(initializationModeKey, DatabaseInitializationMode.NEVER);
            } else if (Files.notExists(BlogConst.INSTALLED.toPath())) {
                // 覆盖配置文件中的修改密码脚本的位置设定
                String changePasswdSchemaPath = environment.getProperty(changePasswdSchemaKey);

                // 动态生成change-passwd.sql文件
                String jdbcProdPassword = environment.getProperty(DecryptJdbcPassword.ENCRYPTED_PASSWORD_KEY);
                String sql = "SHOW GRANTS FOR 'root'@'%'; ALTER USER 'root'@'%' IDENTIFIED BY '"
                        + jdbcProdPassword + "' ; FLUSH PRIVILEGES ;";
                try {
                    File file = FileUtils.getFile(changePasswdSchemaPath);
                    if (file == null) {
                        file = FileUtils.createFile(changePasswdSchemaPath);
                    }
                    Files.writeString(file.toPath(), sql);
                } catch (IOException e) {
                    e.printStackTrace();
                    throw new AlertException("无法创建" + changePasswdSchemaPath + "文件。");
                }
            } else {
                propertyMap.put(initializationModeKey, DatabaseInitializationMode.NEVER);
                logger.error("程序无法准确的验证 {} 文件是否存在, " +
                        "所以默认关闭了数据库的初始化功能", BlogConst.INSTALLED.getPath());
            }
        }
    }

}
