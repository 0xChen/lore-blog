package com.developerchen.core.base.event;

/**
 * 更新<T>的Event
 *
 * @param <T>
 * @author syc
 */
public class EntityUpdatingEvent<T> extends AbstractEntityEvent<T> {

    public EntityUpdatingEvent(T entity) {
        super(entity);
    }
}
