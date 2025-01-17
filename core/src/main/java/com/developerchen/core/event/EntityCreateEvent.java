package com.developerchen.core.event;

/**
 * 创建<T>的Event
 *
 * @param <T>
 * @author syc
 */
public class EntityCreateEvent<T> extends AbstractEntityEvent<T> {

    public EntityCreateEvent(T entity) {
        super(entity);
    }
}
