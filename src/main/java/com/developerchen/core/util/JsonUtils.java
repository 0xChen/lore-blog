package com.developerchen.core.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

/**
 * JSON工具类
 *
 * @author syc
 */
public final class JsonUtils {

    private static ObjectMapper objectMapper;

    /**
     * 将对象序列化成JSON字符串的形式
     *
     * @param object 带转换成JSON字符串的对象
     * @return 对象的JSON字符串
     */
    public static String toJsonString(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    /**
     * 利用springboot初始化后的Jackson2ObjectMapperBuilder构造一个ObjectMapper给
     * JsonUtils使用, 使JsonUtils序列化的结果与spring一致
     */
    @Configuration
    static class InitJsonUtils implements Jackson2ObjectMapperBuilderCustomizer {

        @Override
        public void customize(Jackson2ObjectMapperBuilder builder) {
            JsonUtils.objectMapper = builder.build();
        }
    }
}
