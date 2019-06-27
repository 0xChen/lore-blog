package com.developerchen.core.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Map;

/**
 * 核心 mapper 继承自 mybatis-plus BaseMapper 添加几个自定义方法
 *
 * @author syc
 */
public interface CoreMapper<T> extends BaseMapper<T> {
    /**
     * <p>
     * 根据 sql 语句，查询全部记录
     * </p>
     *
     * @param sql 可执行SQL语句
     * @return List<Map < String, Object>> 表的字段 -> 值
     */
    @Select("${sql}")
    List<Map<String, Object>> selectMapsBySql(@Param("sql") String sql);

    /**
     * <p>
     * 根据 sql 语句，查询记录
     * </p>
     *
     * @param sql 可执行SQL语句
     * @return Map<String, Object> 表的字段 -> 值
     */
    @Select("${sql}")
    Map<String, Object> selectMapBySql(@Param("sql") String sql);

    /**
     * <p>
     * 根据 sql 语句，查询指定类型的全部记录
     * </p>
     *
     * @param sql        可执行SQL语句
     * @param resultType 查询结果的类型
     * @return List<E>
     */
    @Select("${sql}")
    <E> List<E> selectListBySql(@Param("sql") String sql, @Param("resultType") Class<E> resultType);

    /**
     * <p>
     * 根据 sql 语句，查询指定类型记录
     * </p>
     *
     * @param sql        可执行SQL语句
     * @param resultType 查询结果的类型
     * @return E
     */
    @Select("${sql}")
    <E> E selectOneBySql(@Param("sql") String sql, @Param("resultType") Class<E> resultType);

    /**
     * <p>
     * 执行SQL插入语句
     * </p>
     *
     * @param sql 需要执行的SQL
     */
    @Insert("${sql}")
    void insertBySql(@Param("sql") String sql);

    /**
     * <p>
     * 执行SQL更新语句
     * </p>
     *
     * @param sql 需要执行的SQL
     */
    @Update("${sql}")
    void updateBySql(@Param("sql") String sql);

}
