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

    private final IUserService userService;
    private final IOptionService optionService;
    private final IAttachmentService attachmentService;
    private final IPostService postService;
    private final ICommentService commentService;
    private final ICategoryService categoryService;

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

    @Override
    @Transactional(rollbackFor = {Exception.class, Error.class})
    public void install(Map<String, String> parameterMap) {
        User user = BeanUtil.toBean(parameterMap, User.class);
        user.setStatus(Const.USER_STATUS_ENABLED);
        user.setRole(Const.ROLE_ADMIN);
        user.setDescription("由博客安装页面添加的系统管理员");

        Map<String, String> options = new HashMap<>(8);
        options.put("scheme", parameterMap.get("scheme"));
        options.put("hostname", parameterMap.get("hostname"));
        options.put("blog_title", parameterMap.get("blog_title"));
        options.put("blog_description", parameterMap.get("blog_description"));
        options.put("installation_time", System.currentTimeMillis() + "");

        this.optionService.deleteAllOption();
        this.userService.deleteAllUser();

        this.optionService.saveOrUpdateOptions(options);
        this.userService.saveOrUpdateUser(user);
    }

    @Override
    public StatisticsDTO getStatistics() {

        StatisticsDTO statistics = new StatisticsDTO();

        Long posts = this.postService.countPost(BlogConst.POST_STATUS_PUBLISH, BlogConst.POST_TYPE_POST);
        Long comments = this.commentService.countComment(BlogConst.COMMENT_STATUS_APPROVED);
        Long attachments = this.attachmentService.countAttachment(null);
        Long categories = this.categoryService.countCategory();
        Long tags = this.postService.countTag();

        statistics.setPosts(posts);
        statistics.setComments(comments);
        statistics.setAttachments(attachments);
        statistics.setCategories(categories);
        statistics.setTags(tags);

        return statistics;
    }

    @Override
    public List<Post> getPostForSitemap() {
        return this.postService.getPostForSitemap();
    }

    @Override
    @Transactional(rollbackFor = {Exception.class, Error.class})
    public void activeTheme(String themeName) {
        this.optionService.saveOrUpdateOptionByName(BlogConst.OPTION_BLOG_THEME, themeName);
    }

    @Override
    @Transactional(rollbackFor = {Exception.class, Error.class})
    public void saveThemeSetting(Option option) {
        option.setName(BlogConst.OPTION_THEME_OPTION_PREFIX + option.getName());
        this.optionService.saveOrUpdateOption(option);
    }

    @Override
    @Transactional(rollbackFor = {Exception.class, Error.class})
    public void saveSiteSetting(Map<String, String> parameterMap) {
        this.optionService.saveOrUpdateOptions(parameterMap);
    }
}
