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


    /**
     * <p>
     * 根据 sql 语句，查询记全部录
     * </p>
     *
     * @param sql 需要指定的SQL
     * @return 字段 -> 值 的集合
     */
    @Override
    public List<Map<String, Object>> selectMapsBySql(String sql) {
        return baseMapper.selectMapsBySql(sql);
    }

    /**
     * <p>
     * 根据 sql 语句，查询记录
     * </p>
     *
     * @param sql 需要执行的SQL
     * @return 字段 -> 值
     */
    @Override
    public Map<String, Object> selectMapBySql(String sql) {
        return baseMapper.selectMapBySql(sql);
    }

    /**
     * <p>
     * 执行SQL更新语句
     * </p>
     *
     * @param sql 需要执行的SQL
     */
    @Override
    @Transactional(rollbackFor = {Exception.class, Error.class})
    public void updateBySql(String sql) {
        baseMapper.updateBySql(sql);
    }

    /**
     * <p>
     * 执行SQL删除语句
     * </p>
     *
     * @param sql 需要执行的SQL
     */
    @Override
    @Transactional(rollbackFor = {Exception.class, Error.class})
    public void deleteBySql(String sql) {
        baseMapper.deleteBySql(sql);
    }
}
