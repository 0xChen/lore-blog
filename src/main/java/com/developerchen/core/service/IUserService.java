package com.developerchen.core.service;

import com.developerchen.core.domain.entity.User;

import java.io.Serializable;

/**
 * <p>
 * 用户表 服务类
 * </p>
 *
 * @author syc
 */
public interface IUserService extends IBaseService<User> {
    void saveOrUpdateUser(User user);

    User getUserByUsername(String username);

    User getUserById(Serializable id);

    void deleteUserById(Serializable id);

    void deleteAllUser();
}
