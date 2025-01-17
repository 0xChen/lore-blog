package com.developerchen.blog.module.post.common;

import com.developerchen.core.event.AbstractEntityEvent;

/**
 * post被阅读的事件， 当有post被用户阅读时，会像容器发布此事件
 *
 * @param <T> post or postDTO or Collection<Post> or Collection<postDTO>
 * @author syc
 */
public class PostReadEvent<T> extends AbstractEntityEvent<T> {

    public PostReadEvent(T entity) {
        super(entity);
    }
}
