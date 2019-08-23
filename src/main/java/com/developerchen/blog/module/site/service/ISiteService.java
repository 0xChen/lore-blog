package com.developerchen.blog.module.site.service;

import com.developerchen.blog.module.site.domain.dto.StatisticsDTO;
import com.developerchen.core.domain.entity.User;

import java.util.Map;

/**
 * <p>
 * 标签表 服务类
 * </p>
 *
 * @author syc
 */
public interface ISiteService {
    void installOption(Map<String, String> parameterMap);

    User installAdminUser(User user);

    StatisticsDTO getStatistics();

    void activeTheme(String themeName);

    void saveThemeSetting(Map<String, String> parameterMap);

    void saveSiteSetting(Map<String, String> parameterMap);

}
