package com.developerchen.blog.module.site.service;

import com.developerchen.blog.module.post.domain.entity.Post;
import com.developerchen.blog.module.site.domain.dto.StatisticsDTO;
import com.developerchen.core.domain.entity.Option;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * 标签表 服务类
 * </p>
 *
 * @author syc
 */
public interface ISiteService {

    void install(Map<String, String> parameterMap);

    StatisticsDTO getStatistics();

    void activeTheme(String themeName);

    void saveThemeSetting(Option option);

    void saveSiteSetting(Map<String, String> parameterMap);

    List<Post> getPostForSitemap();
}
