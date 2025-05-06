package com.developerchen.core.base.event;

/**
 * 创建<T>的Event
 *
 * @param <T>
 * @author syc
 */
public class EntityCreatingEvent<T> extends AbstractEntityEvent<T> {

    public EntityCreatingEvent(T entity) {
        super(entity);
    }
}
