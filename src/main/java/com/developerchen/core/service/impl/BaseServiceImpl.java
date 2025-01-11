package com.developerchen.core.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.developerchen.core.repository.CoreMapper;
import com.developerchen.core.service.IBaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * 核心服务
 *
 * @author syc
 */
public abstract class BaseServiceImpl<M extends CoreMapper<T>, T> extends ServiceImpl<M, T> implements IBaseService<T> {

    /**
     * spring 的事件发布器
     */
    @Autowired
    protected ApplicationEventPublisher eventPublisher;

}
