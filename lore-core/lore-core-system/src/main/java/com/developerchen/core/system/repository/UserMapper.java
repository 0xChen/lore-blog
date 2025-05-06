package com.developerchen.core.system.repository;

import com.developerchen.core.base.BaseMapper;
import com.developerchen.core.domain.entity.User;
import org.apache.ibatis.annotations.Delete;

/**
 * <p>
 * 用户表 Mapper 接口
 * </p>
 *
 * @author syc
 */
public interface UserMapper extends BaseMapper<User> {

    @Delete("DELETE FROM `sys_user` WHERE `id` <> 1")
    void purge();
}
