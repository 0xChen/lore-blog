package com.developerchen.blog.module.site.service.impl;

import com.developerchen.blog.constant.BlogConst;
import com.developerchen.blog.exception.BlogException;
import com.developerchen.blog.module.category.service.ICategoryService;
import com.developerchen.blog.module.comment.service.ICommentService;
import com.developerchen.blog.module.post.service.IPostService;
import com.developerchen.blog.module.site.domain.dto.StatisticsDTO;
import com.developerchen.blog.module.site.service.ISiteService;
import com.developerchen.core.config.AppConfig;
import com.developerchen.core.constant.Const;
import com.developerchen.core.domain.entity.User;
import com.developerchen.core.service.IFileService;
import com.developerchen.core.service.IOptionService;
import com.developerchen.core.service.IUserService;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.Map;

/**
 * <p>
 * 站点Service
 * </p>
 *
 * @author syc
 */
@Service
public class SiteServiceImpl implements ISiteService {

    private IUserService userService;
    private IOptionService optionService;
    private IFileService fileService;
    private IPostService postService;
    private ICommentService commentService;
    private ICategoryService categoryService;

    public SiteServiceImpl(IUserService userService,
                           IOptionService optionService,
                           IFileService fileService,
                           IPostService postService,
                           ICommentService commentService,
                           ICategoryService categoryService) {
        this.userService = userService;
        this.optionService = optionService;
        this.fileService = fileService;
        this.postService = postService;
        this.commentService = commentService;
        this.categoryService = categoryService;
    }

    /**
     * 初始化站点
     *
     * @param user 用户
     */
    @Override
    public void initSite(User user) {
        user.setStatus(Const.USER_STATUS_ENABLED);
        user.setRole(Const.ROLE_ADMIN);
        user.setDescription("系统管理员");
        try {
            this.userService.saveOrUpdateUser(user);
        } catch (Exception e) {
            throw new BlogException("初始化站点失败, 无法新增系统管理员用户. ");
        }
        String homePath = AppConfig.HOME_PATH;
        File installed = new File(homePath + File.separator + "Installed");
        if (installed.exists()) {
            throw new BlogException("已经初始化过站点, 不能重复初始化. " +
                    "如果需要重新初始化站点请手动删除[" + installed.getPath() + "]文件. ");
        }
        boolean createFileSuccess = true;
        try {
            if (!installed.createNewFile()) {
                createFileSuccess = false;
            }
        } catch (IOException e) {
            createFileSuccess = false;
        }
        if (!createFileSuccess) {
            throw new BlogException("初始化站点失败, 无法创建[" + installed.getPath() + "]文件. ");
        }
    }

    /**
     * 获取后台统计数据
     * 包括发布文章的数量, 评论数量, 文件数量, 类别数量等
     */
    @Override
    public StatisticsDTO getStatistics() {

        StatisticsDTO statistics = new StatisticsDTO();

        int posts = this.postService.postCount(BlogConst.POST_STATUS_PUBLISH, BlogConst.POST_TYPE_POST);
        int comments = this.commentService.commentCount(BlogConst.COMMENT_APPROVED);
        int files = this.fileService.fileCount(null);
        int categories = this.categoryService.categoryCount();

        statistics.setPosts(posts);
        statistics.setComments(comments);
        statistics.setFiles(files);
        statistics.setCategories(categories);

        return statistics;
    }

    /**
     * 激活指定的主题
     *
     * @param themeName 主题名称
     */
    @Override
    public void activeTheme(String themeName) {
        this.optionService.saveOrUpdateOptionByName(BlogConst.BLOG_THEME, themeName);
    }

    /**
     * 保存主题设置
     *
     * @param parameterMap 设置项
     */
    @Override
    public void saveThemeSetting(Map<String, String> parameterMap) {
        this.optionService.saveOrUpdateOptions(parameterMap);
    }

    /**
     * 保存站点设置
     *
     * @param parameterMap 设置项
     */
    @Override
    public void saveSiteSetting(Map<String, String> parameterMap) {
        this.optionService.saveOrUpdateOptions(parameterMap);
    }
}
