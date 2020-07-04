package com.developerchen.blog.module.link.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.developerchen.blog.exception.BlogException;
import com.developerchen.blog.module.link.domain.entity.Link;
import com.developerchen.blog.module.link.repository.LinkMapper;
import com.developerchen.core.constant.Const;
import com.developerchen.core.service.impl.BaseServiceImpl;
import com.developerchen.core.domain.RestPage;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

/**
 * <p>
 * 链接表 服务实现类
 * </p>
 *
 * @author syc
 */
@Service
public class LinkServiceImpl extends BaseServiceImpl<LinkMapper, Link> implements ILinkService {

    public LinkServiceImpl() {
    }


    @Override
    @Transactional(rollbackFor = {Exception.class, Error.class})
    public void saveOrUpdateLink(Link link) {
        Long linkId = link.getId();
        if (linkId == null) {
            String visible = link.getVisible();
            if (StringUtils.isBlank(visible)) {
                link.setVisible(Const.YES);
            }
            Integer sort = link.getSort();
            if (sort == null) {
                int count = super.count(null);
                link.setSort(++count);
            }
        }
        super.saveOrUpdate(link);
    }

    @Override
    @Transactional(rollbackFor = {Exception.class, Error.class})
    public void updateLink(Link link) {
        Link linkDb = baseMapper.selectById(link.getId());
        if (linkDb == null) {
            throw new BlogException("不存在此链接, 更新失败");
        }
        super.updateById(link);
    }

    @Override
    public Link getLinkById(long linkId) {
        return baseMapper.selectById(linkId);
    }

    @Override
    public IPage<Link> getLinkPage(String name,
                                   String url,
                                   String visible,
                                   String description,
                                   long page, long size) {
        QueryWrapper<Link> qw = new QueryWrapper<>();
        qw.like(StringUtils.isNotBlank(name), "name", name);
        qw.like(StringUtils.isNotBlank(url), "url", url);
        qw.like(StringUtils.isNotBlank(description), "description", description);
        qw.eq(StringUtils.isNotBlank(visible), "visible", visible);
        qw.orderByAsc("sort");
        qw.orderByAsc("create_time");

        return baseMapper.selectPage(new RestPage<>(page, size), qw);
    }

    @Override
    @Transactional(rollbackFor = {Exception.class, Error.class})
    public void deleteLinkById(long linkId) {
        baseMapper.deleteById(linkId);
    }

    @Override
    @Transactional(rollbackFor = {Exception.class, Error.class})
    public void deleteLinkByIds(Set<Long> linkIds) {
        baseMapper.deleteBatchIds(linkIds);
    }

    @Override
    @Transactional(rollbackFor = {Exception.class, Error.class})
    public void deleteAll() {
        baseMapper.delete(null);
    }
}
