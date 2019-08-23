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

    void saveOrUpdateLink(Link link);

    Link getLinkById(long linkId);

    IPage<Link> getLinkPage(String name,
                            String url,
                            String visible,
                            String desc,
                            long page, long size);

    void deleteLinkById(long linkId);

    void deleteLinkByIds(Set<Long> linkIds);

    void deleteAll();
}
