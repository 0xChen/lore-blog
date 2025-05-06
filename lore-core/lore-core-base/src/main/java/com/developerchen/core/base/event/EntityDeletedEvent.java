package com.developerchen.core.base.event;

/**
 * 删除<T>的Event
 *
 * @param <T>
 * @author syc
 */
public class EntityDeletedEvent<T> extends AbstractEntityEvent<T> {

    public EntityDeletedEvent(T entity) {
        super(entity);
    }
}
