package com.developerchen.core.config;

import com.developerchen.core.security.JwtTokenUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.system.ApplicationHome;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import javax.annotation.PostConstruct;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 应用的自定义配置
 *
 * @author syc
 */

@Configuration
@EnableAsync
@EnableScheduling
@EnableConfigurationProperties(AppProperties.class)
public class AppConfig {
    public static final Map<String, String> OPTIONS = new LinkedHashMap<>(32);

    /**
     * the application home directory.
     */
    public static final String HOME_PATH = new ApplicationHome().getDir().getPath();

    public static String fileLocation;
    public static String staticPathPattern;
    public static String scheme;
    public static String hostname;

    private AppProperties appProperties;

    public AppConfig(AppProperties appProperties) {
        this.appProperties = appProperties;
    }

    @PostConstruct
    protected void initialize() {
        AppConfig.fileLocation = appProperties.getFileLocation();
        AppConfig.staticPathPattern = appProperties.getStaticPathPattern();
        AppConfig.scheme = appProperties.getScheme();
        AppConfig.hostname = appProperties.getHostname();

        JwtTokenUtil.EXPIRE_TIME = appProperties.getJwtExpireTime();
        JwtTokenUtil.SECRET_KEY_PATH = appProperties.getJwtSecretKeyPath();
    }

    /**
     * 获取指定名称的配置项值
     *
     * @param name 配置项名称
     * @return 配置值
     */
    public static String getOption(String name) {
        return OPTIONS.get(name);
    }

    /**
     * 获取配置项值, 如果此配置项没有值则返回传入的默认值
     *
     * @param name        配置项名称
     * @param defaultVale 默认值
     * @return 配置值
     */
    public static String getOption(String name, String defaultVale) {
        String value = OPTIONS.get(name);

        return StringUtils.isNotEmpty(value) ? value : defaultVale;
    }

    /**
     * 添加配置项
     *
     * @param name  配置名称
     * @param value 配置值
     */
    public static void addOption(String name, String value) {
        OPTIONS.put(name, value);
    }

    /**
     * 将参数中所有配置项添加到OPTIONS中
     *
     * @param options 配置项
     */
    public static void addOptions(Map<String, String> options) {
        OPTIONS.putAll(options);
    }

    /**
     * 更新OPTIONS
     *
     * @param options 配置项
     */
    public static void updateOptions(Map<String, String> options) {
        OPTIONS.clear();
        OPTIONS.putAll(options);
    }

}
