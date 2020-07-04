package com.developerchen.core.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.developerchen.core.config.AppConfig;
import com.developerchen.core.domain.entity.Option;
import com.developerchen.core.repository.OptionMapper;
import com.developerchen.core.service.IOptionService;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 * 设置表 服务实现类
 * </p>
 *
 * @author syc
 */
@Service
public class OptionServiceImpl extends BaseServiceImpl<OptionMapper, Option> implements IOptionService {
    public OptionServiceImpl() {
    }

    /**
     * 保存配置
     *
     * @param name  配置名称
     * @param value 配置值
     */
    @Override
    @Transactional(rollbackFor = {Exception.class, Error.class})
    public void saveOrUpdateOptionByName(String name, String value) {
        Validate.notBlank(name, "没有指定配置项");
        Option option = baseMapper.selectOne(new QueryWrapper<Option>().eq("name", name));
        option = option == null ? new Option() : option;
        option.setName(name);
        option.setValue(value);
        super.saveOrUpdate(option);
        updateAppConfigOptions();
    }

    /**
     * 保存配置
     *
     * @param option Option实例
     */
    @Override
    @Transactional(rollbackFor = {Exception.class, Error.class})
    public void saveOrUpdateOption(Option option) {
        if (option.getId() == null) {
            Option oldOption = baseMapper.selectOne(new QueryWrapper<Option>()
                    .eq("name", option.getName()));
            if (oldOption != null) {
                option.setId(oldOption.getId());
            }
        }
        super.saveOrUpdate(option);
        updateAppConfigOptions();
    }

    @Override
    @Transactional(rollbackFor = {Exception.class, Error.class})
    public void saveOrUpdateOptions(Map<String, String> parameterMap) {
        QueryWrapper<Option> qw = new QueryWrapper<>();
        qw.in("name", parameterMap.keySet());
        List<Option> optionList = baseMapper.selectList(qw);
        Map<String, Option> nameToOption = optionList.stream()
                .collect(Collectors.toMap(Option::getName, option -> option));

        parameterMap.forEach((key, value) -> {
            if (nameToOption.containsKey(key)) {
                nameToOption.get(key).setValue(value);
            } else {
                Option option = new Option();
                option.setName(key);
                option.setValue(value);
                optionList.add(option);
            }
        });
        super.saveOrUpdateBatch(optionList);
        updateAppConfigOptions();
    }

    /**
     * 获取所有配置项
     */
    @Override
    public Map<String, String> getAllOption() {
        Map<String, String> options = new HashMap<>(32);
        List<Option> optionsList = baseMapper.selectList(null);
        if (null != optionsList) {
            optionsList.forEach(option -> options.put(option.getName(), option.getValue()));
        }
        return options;
    }

    /**
     * 获取配置项
     *
     * @param name 名称查询条件
     * @return Option集合
     */
    @Override
    public List<Option> getOptionList(String name) {
        QueryWrapper<Option> qw = new QueryWrapper<>();
        qw.like(StringUtils.isNotBlank(name), "name", name);
        return baseMapper.selectList(qw);
    }

    /**
     * 根据名称获取指定配置项
     *
     * @param name 配置名称
     * @return Option
     */
    @Override
    public Option getOption(String name) {
        Validate.notBlank(name, "没有指定配置项");
        return baseMapper.selectOne(new QueryWrapper<Option>().eq("name", name));
    }

    /**
     * 根据name删除配置项
     *
     * @param name 配置名称
     */
    @Override
    @Transactional(rollbackFor = {Exception.class, Error.class})
    public void deleteOptionByName(String name) {
        Validate.notBlank(name, "没有指定配置项");
        baseMapper.delete(new QueryWrapper<Option>().eq("name", name));
        updateAppConfigOptions();
    }

    /**
     * 根据主键删除配置项
     *
     * @param id Option表主键
     */
    @Override
    @Transactional(rollbackFor = {Exception.class, Error.class})
    public void deleteOptionById(long id) {
        baseMapper.delete(new QueryWrapper<Option>().eq("id", id));
        updateAppConfigOptions();
    }

    /**
     * 根据主键批量删除配置项
     *
     * @param ids option表主键集合
     */
    @Override
    @Transactional(rollbackFor = {Exception.class, Error.class})
    public void deleteOptionByIds(Collection<? extends Serializable> ids) {
        Validate.notEmpty(ids, "没有指定配置项主键");
        baseMapper.deleteBatchIds(ids);
        updateAppConfigOptions();
    }

    /**
     * 更新AppConfig.OPTIONS中的数据为数据库中最新的设定
     * 当sys_option表中的数据发生改变时, 必须调用此方法更新AppConfig.OPTIONS
     */
    private void updateAppConfigOptions() {
        AppConfig.updateOptions(getAllOption());
    }

    /**
     * 删除所有设置项
     */
    @Override
    @Transactional(rollbackFor = {Exception.class, Error.class})
    public void deleteAllOption() {
        baseMapper.deleteBySql("truncate table sys_option");
    }
}
