package com.developerchen.core.system.repository;

import com.developerchen.core.base.BaseMapper;
import com.developerchen.core.domain.entity.Option;
import org.apache.ibatis.annotations.Delete;

/**
 * <p>
 * 设置表 Mapper 接口
 * </p>
 *
 * @author syc
 */
public interface OptionMapper extends BaseMapper<Option> {

    /**
     * 删除所有设置
     */
    @Delete("truncate table sys_option")
    void deleteAllOption();
}
