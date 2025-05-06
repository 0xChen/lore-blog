package com.developerchen.core.base.event;

/**
 * 更新<T>的Event
 *
 * @param <T>
 * @author syc
 */
public class EntityUpdatedEvent<T> extends AbstractEntityEvent<T> {

    public EntityUpdatedEvent(T entity) {
        super(entity);
    }
}
