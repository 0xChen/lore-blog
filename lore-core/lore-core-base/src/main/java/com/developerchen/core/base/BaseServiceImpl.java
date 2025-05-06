package com.developerchen.core.base;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;

/**
 * 核心服务
 *
 * @author syc
 */
public abstract class BaseServiceImpl<M extends BaseMapper<T>, T> extends ServiceImpl<M, T> implements IBaseService<T> {

    /**
     * spring 的事件发布器
     */
    @Autowired
    protected ApplicationEventPublisher eventPublisher;

}
