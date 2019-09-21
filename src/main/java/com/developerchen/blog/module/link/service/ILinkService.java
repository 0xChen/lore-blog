package com.developerchen.blog.module.link.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.developerchen.blog.module.link.domain.entity.Link;
import com.developerchen.core.service.IBaseService;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

/**
 * <p>
 * 链接表
 * </p>
 *
 * @author syc
 */
public interface ILinkService extends IBaseService<Link> {

    @Transactional
    void saveOrUpdateLink(Link link);

    Link getLinkById(long linkId);

    IPage<Link> getLinkPage(String name,
                            String url,
                            String visible,
                            String desc,
                            long page, long size);

    @Transactional
    void deleteLinkById(long linkId);

    @Transactional
    void deleteLinkByIds(Set<Long> linkIds);

    @Transactional
    void deleteAll();
}
