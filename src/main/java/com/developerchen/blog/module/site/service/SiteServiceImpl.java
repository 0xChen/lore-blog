package com.developerchen.blog.module.site.service;

import cn.hutool.core.bean.BeanUtil;
import com.developerchen.blog.constant.BlogConst;
import com.developerchen.blog.module.category.service.ICategoryService;
import com.developerchen.blog.module.comment.service.ICommentService;
import com.developerchen.blog.module.post.domain.entity.Post;
import com.developerchen.blog.module.post.service.IPostService;
import com.developerchen.blog.module.site.domain.dto.StatisticsDTO;
import com.developerchen.core.constant.Const;
import com.developerchen.core.domain.entity.Option;
import com.developerchen.core.domain.entity.User;
import com.developerchen.core.service.IAttachmentService;
import com.developerchen.core.service.IOptionService;
import com.developerchen.core.service.IUserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
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
    private IAttachmentService attachmentService;
    private IPostService postService;
    private ICommentService commentService;
    private ICategoryService categoryService;

    public SiteServiceImpl(IUserService userService,
                           IOptionService optionService,
                           IAttachmentService attachmentService,
                           IPostService postService,
                           ICommentService commentService,
                           ICategoryService categoryService) {
        this.userService = userService;
        this.optionService = optionService;
        this.attachmentService = attachmentService;
        this.postService = postService;
        this.commentService = commentService;
        this.categoryService = categoryService;
    }

    /**
     * 初始化安装
     *
     * @param parameterMap 配置项
     *                     key: scheme:
     *                     hostname:
     *                     blog_title:
     *                     blog_description:
     *                     username:
     *                     nickname:
     *                     email:
     *                     password:
     *                     confirmPassword:
     */
    @Override
    @Transactional(rollbackFor = {Exception.class, Error.class})
    public void install(Map<String, String> parameterMap) {
        User user = BeanUtil.mapToBean(parameterMap, User.class, false);
        user.setStatus(Const.USER_STATUS_ENABLED);
        user.setRole(Const.ROLE_ADMIN);
        user.setDescription("由博客安装页面添加的系统管理员");

        Map<String, String> options = new HashMap<>(8);
        options.put("scheme", parameterMap.get("scheme"));
        options.put("hostname", parameterMap.get("hostname"));
        options.put("blog_title", parameterMap.get("blog_title"));
        options.put("blog_description", parameterMap.get("blog_description"));

        this.optionService.deleteAllOption();
        this.userService.deleteAllUser();

        this.optionService.saveOrUpdateOptions(options);
        this.userService.saveOrUpdateUser(user);
    }

    /**
     * 获取后台统计数据
     * 包括发布文章的数量, 评论数量, 文件数量, 分类数量等
     */
    @Override
    public StatisticsDTO getStatistics() {

        StatisticsDTO statistics = new StatisticsDTO();

        int posts = this.postService.countPost(BlogConst.POST_STATUS_PUBLISH, BlogConst.POST_TYPE_POST);
        int comments = this.commentService.countComment(BlogConst.COMMENT_STATUS_APPROVED);
        int files = this.attachmentService.countAttachment(null);
        int categories = this.categoryService.countCategory();

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
    @Transactional(rollbackFor = {Exception.class, Error.class})
    public void activeTheme(String themeName) {
        this.optionService.saveOrUpdateOptionByName(BlogConst.OPTION_BLOG_THEME, themeName);
    }

    /**
     * 保存主题设置
     *
     * @param option 设置项
     */
    @Override
    @Transactional(rollbackFor = {Exception.class, Error.class})
    public void saveThemeSetting(Option option) {
        option.setName(BlogConst.OPTION_THEME_OPTION_PREFIC + option.getName());
        this.optionService.saveOrUpdateOption(option);
    }

    /**
     * 保存站点设置
     *
     * @param parameterMap 设置项
     */
    @Override
    @Transactional(rollbackFor = {Exception.class, Error.class})
    public void saveSiteSetting(Map<String, String> parameterMap) {
        this.optionService.saveOrUpdateOptions(parameterMap);
    }
}
