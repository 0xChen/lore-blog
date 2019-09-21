package com.developerchen.core.service;

import com.developerchen.core.domain.entity.User;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;

/**
 * <p>
 * 用户表 服务类
 * </p>
 *
 * @author syc
 */
public interface IUserService extends IBaseService<User> {
    @Transactional
    void saveOrUpdateUser(User user);

    User getUserByUsername(String username);

    User getUserById(Serializable id);

    @Transactional
    void deleteUserById(Serializable id);

    @Transactional
    void deleteAllUser();
}
