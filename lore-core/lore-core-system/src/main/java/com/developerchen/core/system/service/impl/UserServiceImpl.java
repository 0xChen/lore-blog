package com.developerchen.core.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.developerchen.core.base.BaseServiceImpl;
import com.developerchen.core.domain.entity.User;
import com.developerchen.core.system.repository.UserMapper;
import com.developerchen.core.system.service.IUserService;
import com.developerchen.core.system.util.SecurityUtils;
import org.apache.commons.lang3.Validate;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;

/**
 * <p>
 * 用户表 服务实现类
 * </p>
 *
 * @author syc
 */
@Service
@Primary
public class UserServiceImpl extends BaseServiceImpl<UserMapper, User> implements IUserService {


    @Override
    @Transactional(rollbackFor = {Exception.class, Error.class})
    public void upsert(User user) {
        String password = user.getPassword();
        if (password != null) {
            // 加密
            user.setPassword(SecurityUtils.encodeUserPassword(password));
        }
        super.saveOrUpdate(user);
    }

    @Override
    public User getUserByUsername(String username) {
        return baseMapper.selectOne(new QueryWrapper<User>().eq("username", username));
    }

    @Override
    public User getUserById(Serializable id) {
        return baseMapper.selectById(id);
    }

    @Override
    @Transactional(rollbackFor = {Exception.class, Error.class})
    public void deleteUserById(Serializable id) {
        Validate.notNull(id, "用户ID不能为空, 删除失败!");
        baseMapper.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = {Exception.class, Error.class})
    public void purge() {
        baseMapper.purge();
    }
}
