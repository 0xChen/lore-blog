package com.developerchen.blog.module.link.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.developerchen.blog.module.link.domain.entity.Link;
import com.developerchen.blog.module.link.repository.LinkMapper;
import com.developerchen.blog.module.link.service.ILinkService;
import com.developerchen.core.constant.Const;
import com.developerchen.core.service.impl.BaseServiceImpl;
import com.developerchen.core.util.RestPage;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

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

    /**
     * 新增或更新链接
     *
     * @param link 链接
     */
    @Override
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

    /**
     * 获取链接
     *
     * @param linkId 链接ID
     * @return 链接
     */
    @Override
    public Link getLinkById(long linkId) {
        return baseMapper.selectById(linkId);
    }

    /**
     * 获取链接
     *
     * @param name        链接名称查询条件
     * @param url         链接地址查询条件
     * @param visible     是否可见查询条件
     * @param description 描述查询条件
     * @param page        当前页码
     * @param size        每页数量
     * @return 分页形式的链接
     */
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

    /**
     * 删除链接
     *
     * @param linkId 链接主键
     */
    @Override
    public void deleteLinkById(long linkId) {
        baseMapper.deleteById(linkId);
    }

    /**
     * 批量删除链接
     *
     * @param linkIds 需要删除的链接主键集合
     */
    @Override
    public void deleteLinkByIds(Set<Long> linkIds) {
        baseMapper.deleteBatchIds(linkIds);
    }

    /**
     * 删除所有链接
     */
    @Override
    public void deleteAll() {
        baseMapper.delete(null);
    }
}
