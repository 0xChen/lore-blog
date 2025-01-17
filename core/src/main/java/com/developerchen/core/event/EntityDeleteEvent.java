package com.developerchen.core.event;

/**
 * 删除<T>的Event
 *
 * @param <T>
 * @author syc
 */
public class EntityDeleteEvent<T> extends AbstractEntityEvent<T> {

    public EntityDeleteEvent(T entity) {
        super(entity);
    }
}
