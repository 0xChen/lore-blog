package com.developerchen.blog.module.site.service;

import com.developerchen.blog.constant.BlogConst;
import com.developerchen.blog.module.category.service.ICategoryService;
import com.developerchen.blog.module.comment.service.ICommentService;
import com.developerchen.blog.module.post.domain.entity.Post;
import com.developerchen.blog.module.post.service.IPostService;
import com.developerchen.blog.module.site.domain.dto.StatisticsDTO;
import com.developerchen.blog.module.site.service.ISiteService;
import com.developerchen.core.constant.Const;
import com.developerchen.core.domain.entity.User;
import com.developerchen.core.service.IFileService;
import com.developerchen.core.service.IOptionService;
import com.developerchen.core.service.IUserService;
import org.springframework.stereotype.Service;

import java.util.List;
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
     * @param parameterMap 配置项
     */
    @Override
    public void installOption(Map<String, String> parameterMap) {
        this.optionService.deleteAllOption();
        this.optionService.saveOrUpdateOptions(parameterMap);
    }

    /**
     * 添加一个管理员用户
     */
    @Override
    public User installAdminUser(User user) {
        user.setStatus(Const.USER_STATUS_ENABLED);
        user.setRole(Const.ROLE_ADMIN);
        user.setDescription("由博客安装页面添加的系统管理员");

        this.userService.deleteAllUser();
        this.userService.saveOrUpdateUser(user);
        return user;
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
     * 获取用于构建站点地图的Post
     *
     * @return Post集合
     */
    @Override
    public List<Post> getPostForSitemap() {
        return this.postService.getPostForSitemap();
    }

    /**
     * 激活指定的主题
     *
     * @param themeName 主题名称
     */
    @Override
    public void activeTheme(String themeName) {
        this.optionService.saveOrUpdateOptionByName(BlogConst.OPTION_BLOG_THEME, themeName);
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
