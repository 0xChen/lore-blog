package com.developerchen.core.system.config;

import com.developerchen.core.system.security.JwtTokenUtil;
import jakarta.annotation.PostConstruct;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.system.ApplicationHome;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

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
@EnableConfigurationProperties(SystemProperties.class)
public class SystemConfig {
    public static final Map<String, String> OPTIONS = new LinkedHashMap<>(32);

    /**
     * the application home directory.
     */
    public static final String HOME_PATH = new ApplicationHome().getDir().getPath();

    public static String fileLocation;
    public static String staticPathPattern;
    public static String scheme;
    public static String hostname;

    private final SystemProperties systemProperties;

    public SystemConfig(SystemProperties systemProperties) {
        this.systemProperties = systemProperties;
    }

    @PostConstruct
    protected void initialize() {
        SystemConfig.fileLocation = systemProperties.getFileLocation();
        SystemConfig.staticPathPattern = systemProperties.getStaticPathPattern();
        SystemConfig.scheme = systemProperties.getScheme();
        SystemConfig.hostname = systemProperties.getHostname();

        JwtTokenUtil.EXPIRE_TIME = systemProperties.getJwtExpireTime();
        JwtTokenUtil.SECRET_KEY_PATH = systemProperties.getJwtSecretKeyPath();
        JwtTokenUtil.SECRET_KEY_STR = systemProperties.getJwtSecretKey();
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
     * 获取配置项值, 如果没有此配置则返回传入的默认值
     *
     * @param name        配置项名称
     * @param defaultVale 默认值
     * @return 配置值
     */
    public static String getOption(String name, String defaultVale) {
        return OPTIONS.getOrDefault(name, defaultVale);
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
