package com.developerchen.core.system.service.impl;

import com.developerchen.core.base.BaseServiceImpl;
import com.developerchen.core.domain.entity.Menu;
import com.developerchen.core.system.repository.MenuMapper;
import com.developerchen.core.system.service.IMenuService;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 菜单表 服务实现类
 * </p>
 *
 * @author syc
 */
@Service
@Primary
public class MenuServiceImpl extends BaseServiceImpl<MenuMapper, Menu> implements IMenuService {

}
