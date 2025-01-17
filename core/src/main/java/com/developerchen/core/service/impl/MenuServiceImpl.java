package com.developerchen.core.service.impl;

import com.developerchen.core.domain.entity.Menu;
import com.developerchen.core.repository.MenuMapper;
import com.developerchen.core.service.IMenuService;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 菜单表 服务实现类
 * </p>
 *
 * @author syc
 */
@Service
public class MenuServiceImpl extends BaseServiceImpl<MenuMapper, Menu> implements IMenuService {

    public MenuServiceImpl() {
    }
}
