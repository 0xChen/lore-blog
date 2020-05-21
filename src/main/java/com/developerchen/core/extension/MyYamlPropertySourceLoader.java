package com.developerchen.core.extension;

import org.springframework.boot.env.OriginTrackedMapPropertySource;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 将 OriginTrackedMapPropertySource 中的 UnmodifiableMap 用遍历的方式转换成普通 Map
 *
 * @author syc
 */
public class MyYamlPropertySourceLoader extends YamlPropertySourceLoader {

    @Override
    public List<PropertySource<?>> load(String name, Resource resource) throws IOException {
        List<PropertySource<?>> propertySources = super.load(name, resource);
        List<PropertySource<?>> newPropertySources = new ArrayList<>(propertySources.size());

        for (PropertySource<?> propertySource : propertySources) {
            if (propertySource instanceof OriginTrackedMapPropertySource) {
                Map<?, ?> source = (Map<?, ?>) propertySource.getSource();
                Map<Object, Object> newSource = new LinkedHashMap<>((int) (source.size() / 0.75 + 1));
                for (Object key : source.keySet()) {
                    Object value = source.get(key);
                    newSource.put(key, value);
                }
                propertySource = new OriginTrackedMapPropertySource(propertySource.getName(),
                        newSource, true);
            }
            newPropertySources.add(propertySource);
        }
        return newPropertySources;
    }

}
