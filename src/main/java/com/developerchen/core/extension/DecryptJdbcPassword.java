package com.developerchen.core.extension;

import com.developerchen.core.config.AppConfig;
import com.developerchen.core.util.CommonUtils;
import com.developerchen.core.util.FileUtils;
import com.developerchen.core.util.SecurityUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.ConfigurableEnvironment;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Map;

/**
 * 将配置文件中加密的数据库连接密码解密
 *
 * @author syc
 */
public class DecryptJdbcPassword implements ApplicationListener<ApplicationEnvironmentPreparedEvent> {

    public static String ENCRYPTED_PASSWORD_KEY = "spring.datasource.druid.password";
    public static String JDBC_SECRET_KEY_PATH_KEY = "my-app.jdbc-secret-key-path";
    public static String DEFAULT_JDBC_SECRET_KEY_PATH = AppConfig.HOME_PATH + File.separator + "jdbcSecretKey";

    @Override
    public void onApplicationEvent(ApplicationEnvironmentPreparedEvent event) {


        ConfigurableEnvironment environment = event.getEnvironment();
        if (!environment.containsProperty(JDBC_SECRET_KEY_PATH_KEY)) {
            return;
        }

        Map<Object, Object> propertyMap = CommonUtils.getPropertyMap(environment, ENCRYPTED_PASSWORD_KEY);

        if (propertyMap != null) {
            String jdbcSecretKeyPath = environment.getProperty(JDBC_SECRET_KEY_PATH_KEY);
            if (StringUtils.isBlank(jdbcSecretKeyPath)) {
                jdbcSecretKeyPath = DEFAULT_JDBC_SECRET_KEY_PATH;
            }

            try {
                File jdbcSecretKeyFile = FileUtils.getFile(jdbcSecretKeyPath);
                StringBuilder secretKey = new StringBuilder();
                for (String line : Files.readAllLines(jdbcSecretKeyFile.toPath())) {
                    secretKey.append(line);
                }
                String encryptedPassword = environment.getProperty(ENCRYPTED_PASSWORD_KEY);
                String decryptedPassword = SecurityUtils.decrypt(encryptedPassword, secretKey.toString());

                // 用解密后的密码替换原加密密码
                propertyMap.put(ENCRYPTED_PASSWORD_KEY, decryptedPassword);
            } catch (IOException e) {
                // 没有密钥文件就不解密, 直接使用原始密码
                e.printStackTrace();
            }

        }
    }

}
