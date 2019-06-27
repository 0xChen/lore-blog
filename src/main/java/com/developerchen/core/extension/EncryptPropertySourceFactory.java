/*
 * Copyright 2002-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.developerchen.core.extension;

import com.developerchen.core.config.AppConfig;
import com.developerchen.core.util.SecureUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.core.io.support.PropertySourceFactory;
import org.springframework.core.io.support.ResourcePropertySource;
import org.springframework.lang.Nullable;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Map;

/**
 * 读取jdbc.properties配置文件, 并解密其中的加密项
 *
 * @author syc
 * @see PropertySourceFactory
 * @see ResourcePropertySource
 */
public class EncryptPropertySourceFactory implements PropertySourceFactory {

    @Override
    public PropertySource<?> createPropertySource(@Nullable String name,
                                                  EncodedResource resource) throws IOException {
        ResourcePropertySource resourcePropertySource;
        if (name != null) {
            resourcePropertySource = new ResourcePropertySource(name, resource);
        } else {
            resourcePropertySource = new ResourcePropertySource(resource);
        }
        Map<String, Object> source = resourcePropertySource.getSource();

        // 只有生产环境的数据库连接密码需要解密
        String password = (String) source.get("jdbc.prod.password");
        String secretKeyPath = (String) source.get("secretKeyPath");
        if (StringUtils.isBlank(secretKeyPath)) {
            secretKeyPath = AppConfig.HOME_PATH + File.separator + "jdbcSecretKey";
        }

        // 获取用于解密的密钥
        File secretKeyFile = ResourceUtils.getFile(secretKeyPath);
        StringBuilder secretKey = new StringBuilder();
        for (String line : Files.readAllLines(secretKeyFile.toPath())) {
            secretKey.append(line);
        }

        String decryptedPassword = SecureUtils.decrypt(password, secretKey.toString());

        // 用解密后的密码替换原加密密码
        source.put("jdbc.prod.password", decryptedPassword);
        return resourcePropertySource;
    }

}
