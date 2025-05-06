package com.developerchen.core.base.event;

/**
 * 创建<T>的Event
 *
 * @param <T>
 * @author syc
 */
public class EntityCreatedEvent<T> extends AbstractEntityEvent<T> {

    public EntityCreatedEvent(T entity) {
        super(entity);
    }
}
