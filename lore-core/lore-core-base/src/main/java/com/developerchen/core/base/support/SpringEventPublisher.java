package com.developerchen.core.base.support;

import com.developerchen.core.base.event.AbstractEntityEvent;
import com.developerchen.core.base.event.EntityUpdatingEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.context.annotation.Configuration;

/**
 * 事件发布工具类 - 提供静态方法发布Spring事件
 * <p>
 * 本工具类主要用于以下场景：
 * 1. 在无法通过依赖注入获取ApplicationEventPublisher的静态方法/工具类中发布事件
 * 2. 统一应用内的事件发布入口
 * </p>
 *
 * <p><b>初始化要求：</b>
 * 需通过{@link SpringEventPublisher.EventPublisherConfig}在Spring容器启动时初始化事件发布器
 * </p>
 *
 * @author syc
 * @see ApplicationEventPublisher
 * @see org.springframework.context.ApplicationEvent
 */
@Slf4j
public class SpringEventPublisher {
    /**
     * Spring事件发布器实例（静态持有）
     * <p>
     * <b>生命周期管理：</b>
     * 通过{@link #setApplicationEventPublisher(ApplicationEventPublisher)}在应用启动时注入，
     * 应确保在调用发布方法前完成初始化
     * </p>
     */
    private static ApplicationEventPublisher applicationEventPublisher;

    /**
     * 初始化事件发布器（仅限Spring配置类调用）
     * <p>
     * <b>注意：</b> 此方法应由Spring容器在启动阶段调用，
     * 业务代码中禁止直接调用以避免线程安全问题
     * </p>
     *
     * @param applicationEventPublisher Spring注入的事件发布器bean
     */
    public static void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        SpringEventPublisher.applicationEventPublisher = applicationEventPublisher;
    }

    /**
     * 发布通用Spring事件
     * <p>典型使用场景示例：</p>
     * <pre>
     * // 发布自定义事件
     * SpringEventPublisher.publishEvent(new MyCustomEvent(data));
     * </pre>
     *
     * @param event 要发布的事件对象（非null）
     * @return true - 事件发布成功，false - 事件发布失败
     */
    public static boolean publishEvent(ApplicationEvent event) {
        if (validateInitialized()) {
            SpringEventPublisher.applicationEventPublisher.publishEvent(event);
            return true;
        }
        return false;
    }

    /**
     * 发布实体相关事件（泛型增强版）
     * <p>
     * 专用于发布继承自{@link AbstractEntityEvent}的事件，
     * 如{@link EntityUpdatingEvent}等实体变更事件
     * </p>
     *
     * @param event 实体事件对象（非null）
     * @param <T>   实体类型
     * @return true - 事件发布成功，false - 事件发布失败
     */
    public static <T> boolean publishEvent(AbstractEntityEvent<T> event) {
        if (validateInitialized()) {
            SpringEventPublisher.applicationEventPublisher.publishEvent(event);
            return true;
        }
        return false;
    }

    /**
     * 校验事件发布器是否已初始化
     *
     * @return true - 已初始化，false - 未初始化
     */
    public static boolean validateInitialized() {
        boolean result = SpringEventPublisher.applicationEventPublisher != null;
        log.warn("Spring事件发布器未初始化，事件发布失败！");
        return result;
    }

    @Configuration
    static class EventPublisherConfig implements ApplicationEventPublisherAware {

        @Override
        public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
            SpringEventPublisher.setApplicationEventPublisher(applicationEventPublisher);
        }
    }
}
