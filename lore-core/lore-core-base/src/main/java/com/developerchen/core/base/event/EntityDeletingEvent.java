package com.developerchen.core.base.event;

/**
 * 删除<T>的Event
 *
 * @param <T>
 * @author syc
 */
public class EntityDeletingEvent<T> extends AbstractEntityEvent<T> {

    public EntityDeletingEvent(T entity) {
        super(entity);
    }
}
