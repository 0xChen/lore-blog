package com.developerchen.blog.module.link.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.developerchen.blog.module.link.domain.entity.Link;
import com.developerchen.core.service.IBaseService;

import java.util.Set;

/**
 * <p>
 * 链接表
 * </p>
 *
 * @author syc
 */
public interface ILinkService extends IBaseService<Link> {

    /**
     * 新增或更新链接
     *
     * @param link 链接
     */
    void saveOrUpdateLink(Link link);

    /**
     * 更新链接
     *
     * @param link 链接
     */
    void updateLink(Link link);

    /**
     * 获取链接
     *
     * @param linkId 链接ID
     * @return 链接
     */
    Link getLinkById(long linkId);

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
    IPage<Link> getLinkPage(String name,
                            String url,
                            String visible,
                            String description,
                            long page, long size);

    /**
     * 删除链接
     *
     * @param linkId 链接主键
     */
    void deleteLinkById(long linkId);

    /**
     * 批量删除链接
     *
     * @param linkIds 需要删除的链接主键集合
     */
    void deleteLinkByIds(Set<Long> linkIds);

    /**
     * 删除所有链接
     */
    void deleteAll();
}
