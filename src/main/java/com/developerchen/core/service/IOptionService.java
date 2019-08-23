package com.developerchen.core.service;

import com.developerchen.core.domain.entity.Option;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 设置表 服务类
 * </p>
 *
 * @author syc
 */
public interface IOptionService extends IBaseService<Option> {
    /**
     * 保存或更新指定配置
     *
     * @param name  配置名称
     * @param value 配置值
     */
    void saveOrUpdateOptionByName(String name, String value);

    /**
     * 保存或更新指定配置
     */
    void saveOrUpdateOption(Option option);

    /**
     * 批量保存或更新指定配置
     *
     * @param parameterMap key: 配置名称 -> value: 值
     */
    void saveOrUpdateOptions(Map<String, String> parameterMap);

    /**
     * 获取所有系统配置
     *
     * @return Map key: 配置名称 -> value: 值
     */
    Map<String, String> getAllOption();

    /**
     * 根据条件获取系统配置
     *
     * @param name 模糊查询条件
     */
    List<Option> getOptionList(String name);

    /**
     * 根据名称获取指定配置项
     *
     * @param name 配置名称
     */
    Option getOption(String name);

    /**
     * 根据name删除配置项
     *
     * @param name 配置名称
     */
    void deleteOptionByName(String name);

    /**
     * 根据主键删除配置项
     *
     * @param id option表主键
     */
    void deleteOptionById(String id);

    /**
     * 根据主键批量删除配置项
     *
     * @param ids option表主键集合
     */
    void deleteOptionByIds(Collection<? extends Serializable> ids);

    /**
     * 删除所有设置项
     */
    void deleteAllOption();
}
