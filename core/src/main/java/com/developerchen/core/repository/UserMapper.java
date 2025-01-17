package com.developerchen.core.repository;

import com.developerchen.core.domain.entity.User;
import org.apache.ibatis.annotations.Delete;

/**
 * <p>
 * 用户表 Mapper 接口
 * </p>
 *
 * @author syc
 */
public interface UserMapper extends CoreMapper<User> {

    @Delete("DELETE FROM `sys_user` WHERE `id` <> 1")
    void deleteAllUser();
}
