package com.developerchen.core.event;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.springframework.context.ApplicationEvent;
import org.springframework.core.ResolvableType;
import org.springframework.core.ResolvableTypeProvider;

import java.util.Collection;
import java.util.Iterator;

/**
 * 实例化后仍可以获取泛型信息的事件类
 *
 * @author syc
 */
public abstract class AbstractEntityEvent<T> extends ApplicationEvent implements ResolvableTypeProvider {

    public AbstractEntityEvent(T entity) {
        super(entity);
    }

    @SuppressWarnings("unchecked")
    @Override
    public T getSource() {
        return (T) super.getSource();
    }

    @Override
    public ResolvableType getResolvableType() {
        T source = getSource();
        ResolvableType generics;
        if (source instanceof Collection) {
            generics = getCollectionGenerics((Collection) source);
        } else if (source instanceof IPage) {
            generics = getIPageGenerics((IPage) source);
        } else {
            generics = ResolvableType.forInstance(getSource());
        }
        return ResolvableType.forClassWithGenerics(getClass(), generics);
    }

    /**
     * 获取集合对象的泛型
     * 原理是通过集合内的元素推断泛型信息
     *
     * @param collection 待推断泛型的集合
     * @return 集合的泛型信息
     */
    public ResolvableType getCollectionGenerics(Collection collection) {
        Iterator iterator = collection.iterator();
        if (iterator.hasNext()) {
            Object object = iterator.next();
            ResolvableType resolvableType;
            if (object instanceof Collection) {
                resolvableType = this.getCollectionGenerics((Collection) object);
            } else {
                // 所有集合都使用其父类Collection.class作为resolvableType, 方便Listener监听所有以集合为泛型的Event
                resolvableType = ResolvableType.forClassWithGenerics(Collection.class,
                        ResolvableType.forInstance(object));
            }
            return resolvableType;
        }
        return ResolvableType.forInstance(collection);
    }

    /**
     * 获取分页对象的泛型
     *
     * @param page 分页对象
     * @return 集合的泛型信息
     */
    public ResolvableType getIPageGenerics(IPage page) {
        Iterator iterator = page.getRecords().iterator();
        if (iterator.hasNext()) {
            Object object = iterator.next();
            return ResolvableType.forClassWithGenerics(IPage.class,
                    ResolvableType.forInstance(object));
        }
        return ResolvableType.forInstance(page);
    }
}
