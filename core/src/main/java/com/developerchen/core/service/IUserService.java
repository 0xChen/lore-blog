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
    /**
     * 新增或更新用户信息
     *
     * @param user 用户信息
     */
    void saveOrUpdateUser(User user);

    /**
     * 根据用户名查询用户
     *
     * @param username 用户名
     * @return User
     */
    User getUserByUsername(String username);

    /**
     * 根据用户ID查询用户
     *
     * @param id 用户ID
     * @return User
     */
    User getUserById(Serializable id);

    /**
     * 通过用户ID删除用户
     *
     * @param id 用户ID
     */
    void deleteUserById(Serializable id);

    /**
     * 删除默认用户以外的所有用户
     */
    void deleteAllUser();
}
