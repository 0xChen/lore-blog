package com.developerchen.core.common.util;

import lombok.Getter;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEvent;
import org.springframework.stereotype.Component;


public class SpringUtils {

    @Getter
    private static ApplicationContext applicationContext;


    public static <T> T getBean(Class<T> clazz) {
        return clazz == null ? null : applicationContext.getBean(clazz);
    }

    public static <T> T getBean(String name, Class<T> clazz) {
        return clazz == null ? null : applicationContext.getBean(name, clazz);
    }

    public static void publishEvent(ApplicationEvent event) {
        if (applicationContext != null) {
            applicationContext.publishEvent(event);
        }
    }

    @Component
    static class InitJSpringUtils implements ApplicationContextAware {

        @Override
        public void setApplicationContext(ApplicationContext context) throws BeansException {
            applicationContext = context;
        }
    }
}
