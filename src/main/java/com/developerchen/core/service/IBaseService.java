package com.developerchen.core.service;

import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;
import java.util.Map;

/**
 * 基础Service接口
 *
 * @param <T> Entity
 * @author syc
 */
public interface IBaseService<T> extends IService<T> {
    /**
     * <p>
     * 根据 sql 语句，查询记全部录
     * </p>
     *
     * @param sql
     */
    List<Map<String, Object>> selectMapsBySql(String sql);

    /**
     * <p>
     * 根据 sql 语句，查询记录
     * </p>
     *
     * @param sql
     */
    Map<String, Object> selectMapBySql(String sql);

    /**
     * <p>
     * 执行SQL更新语句
     * </p>
     *
     * @param sql 需要执行的SQL
     */
    void updateBySql(String sql);
}
