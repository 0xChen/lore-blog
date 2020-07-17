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

    /**
     * 初始化安装
     *
     * @param parameterMap 配置项
     *                     key:
     *                     scheme:
     *                     hostname:
     *                     blog_title:
     *                     blog_description:
     *                     username:
     *                     nickname:
     *                     email:
     *                     password:
     *                     confirmPassword:
     */
    void install(Map<String, String> parameterMap);

    /**
     * 获取后台统计数据
     * 包括发布文章的数量, 评论数量, 文件数量, 分类数量等
     *
     * @return StatisticsDTO
     */
    StatisticsDTO getStatistics();

    /**
     * 激活指定的主题
     *
     * @param themeName 主题名称
     */
    void activeTheme(String themeName);

    /**
     * 保存主题设置
     *
     * @param option 设置项
     */
    void saveThemeSetting(Option option);

    /**
     * 保存站点设置
     *
     * @param parameterMap 设置项
     */
    void saveSiteSetting(Map<String, String> parameterMap);

    /**
     * 获取用于构建站点地图的Post
     *
     * @return Post集合
     */
    List<Post> getPostForSitemap();
}
