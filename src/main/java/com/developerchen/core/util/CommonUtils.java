package com.developerchen.core.util;

import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;

import java.util.Map;

/**
 * 一些杂项工具方法
 *
 * @author syc
 */
public class CommonUtils {

    /**
     * 获取Environment中指定属性所在的Map
     *
     * @param environment {@link org.springframework.core.env.ConfigurableEnvironment}
     * @param propertyKey 属性名称
     * @return 目标属性所在的Map
     */
    @SuppressWarnings("unchecked")
    public static Map<Object, Object> getPropertyMap(ConfigurableEnvironment environment, String propertyKey) {
        Map<Object, Object> propertyMap = null;
        MutablePropertySources propertySources = environment.getPropertySources();
        for (PropertySource<?> propertySource : propertySources) {
            if (propertySource.containsProperty(propertyKey)) {
                Object source = propertySource.getSource();
                if (source instanceof Map) {
                    propertyMap = (Map<Object, Object>) source;
                    break;
                }
            }
        }
        return propertyMap;
    }
}
