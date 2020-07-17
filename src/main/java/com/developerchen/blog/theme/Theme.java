package com.developerchen.blog.theme;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.developerchen.blog.constant.BlogConst;
import com.developerchen.blog.module.category.domain.entity.Category;
import com.developerchen.blog.module.category.service.ICategoryService;
import com.developerchen.blog.module.comment.domain.dto.CommentDTO;
import com.developerchen.blog.module.comment.domain.entity.Comment;
import com.developerchen.blog.module.comment.service.ICommentService;
import com.developerchen.blog.module.post.domain.entity.Post;
import com.developerchen.blog.module.post.service.IPostService;
import com.developerchen.blog.util.BlogUtils;
import com.developerchen.core.constant.Const;
import com.developerchen.core.domain.entity.Option;
import com.developerchen.core.domain.entity.User;
import com.developerchen.core.service.IUserService;
import com.developerchen.core.util.JsonUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.thymeleaf.context.WebEngineContext;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * 主题功能
 * <p>
 *
 * @author syc
 */
@Component
public final class Theme {
    private static final Logger logger = LoggerFactory.getLogger(Theme.class);

    private static final Map<String, Map<String, String>> THEME_TO_OPTION = new HashMap<>();

    public static final String THEME_PATH = "/resources/themes/";
    public static final String THEME_LOGO_PATH = "/img/logo.png";

    private static IUserService userService;
    private static IPostService postService;
    private static ICommentService commentService;
    private static ICategoryService categoryService;


    public Theme(
            IPostService post,
            ICategoryService category,
            IUserService user,
            ICommentService comment) {
        userService = user;
        postService = post;
        commentService = comment;
        categoryService = category;
    }

    /**
     * 获取主题自定义配置项值, 如果没有此配置项或者此配置项对应的值是 null 或者 ""
     * 则返回默认值
     */
    public static String themeOption(String name, String defaultValue) {
        Map<String, String> themeOption = themeOption();
        if (themeOption != null) {
            String value = themeOption.get(name);
            return StringUtils.isEmpty(value) ? defaultValue : value;
        }
        return defaultValue;
    }

    /**
     * 获取指定的主题的自定义配置
     */
    public static String themeOption(String name) {
        Map<String, String> themeOption = themeOption();
        if (themeOption != null) {
            return themeOption.get(name);
        }
        return null;
    }

    private static Map<String, String> themeOption() {
        String themeName = Common.blogTheme();
        String valueJson = Common.getOption(BlogConst.OPTION_THEME_OPTION_PREFIX + themeName);
        try {
            List<Option> optionList = JsonUtils.getObjectMapper()
                    .readValue(valueJson, new TypeReference<List<Option>>() {});
            return optionList.stream().collect(Collectors.toMap(Option::getName, Option::getValue));
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    /**
     * 获取主题URL
     */
    public static String themeUrl() {
        return Common.blogUrl(THEME_PATH) + Common.blogTheme();
    }

    /**
     * 获取主题下的文件路径
     *
     * @param sub 子路径
     */
    public static String themeUrl(String sub) {
        return themeUrl() + sub;
    }

    /**
     * 获取当前theme的logo地址
     */
    public static String themeLogo() {
        return themeOption(BlogConst.OPTION_THEME_LOGO_URL, Theme.themeUrl(Theme.THEME_LOGO_PATH));
    }

    /**
     * 用于判断当前上下文环境中的内容是不是目标类型<br>
     * 例:<br>
     * Common.is(ctx, 'post');<br>
     * Common.is(ctx, 'category');<br>
     * Common.is(ctx, 'tag');
     *
     * @param ctx  thymeleaf的上下文
     * @param slug 目标标识
     * @return true 是, false 否
     */
    public static boolean is(WebEngineContext ctx, String slug) {
        Object object = ctx.getVariable("is");
        return StringUtils.equals((String) object, slug);
    }

    /**
     * 获取用于SEO的keywords
     */
    public static String metaKeywords(WebEngineContext ctx) {
        if (is(ctx, "post")) {
            Post post = currentPost(ctx);
            return post.getTags();
        } else {
            return Common.blogKeywords();
        }
    }

    /**
     * 获取用于SEO的description
     */
    public static String metaDescription(WebEngineContext ctx) {
        if (is(ctx, "post")) {
            Post post = currentPost(ctx);
            return Common.intro(post.getContent(), 100, "...");
        } else {
            return Common.blogDescription();
        }
    }

    /**
     * 获取指定数量的最新文章
     *
     * @param size 文章数量
     */
    public static List<Post> recentPosts(long size) {
        return postService.getPostList(BlogConst.POST_RECENT, size);
    }

    /**
     * 获取指定数量的最新评论
     *
     * @param size 评论数量
     */
    public static List<Comment> recentComments(long size) {
        System.out.println("Execute method asynchronously. " + Thread.currentThread().getName());
        return commentService.recentComments(size);
    }

    /**
     * 分页形式获取文章
     *
     * @param ctx thymeleaf的上下文
     * @return 分页文章
     */
    public static IPage<Post> postPage(WebEngineContext ctx) {
        long page = (long) ctx.getVariable("page");
        long size = (long) ctx.getVariable("size");
        return postService.getPublishedPostPage(page, size);
    }

    /**
     * 获取当前页面中的Post对象
     *
     * @param ctx thymeleaf的上下文
     */
    public static Post currentPost(WebEngineContext ctx) {
        return (Post) ctx.getVariable("post");
    }

    /**
     * 获取当前文章的下一篇
     *
     * @param ctx thymeleaf的上下文
     */
    public static Post nextPost(WebEngineContext ctx) {
        Post post = currentPost(ctx);
        return null != post ? postService.getPrevOrNextPost(Const.PAGE_NEXT, post.getPubdate()) : null;
    }

    /**
     * 当前文章的下一篇文章链接地址
     *
     * @param ctx thymeleaf的上下文
     */
    public static String nextPostLink(WebEngineContext ctx) {
        Post post = nextPost(ctx);
        return post != null ? nextPostLink(ctx, title(post)) : "";
    }

    /**
     * 当前文章的下一篇文章超链接
     *
     * @param ctx   thymeleaf的上下文
     * @param title 文章标题
     */
    public static String nextPostLink(WebEngineContext ctx, String title) {
        Post post = nextPost(ctx);
        if (post != null) {
            return "<a href=\"" + permalink(post) + "\" title=\"" + title(post) + "\">" + title + "</a>";
        }
        return "";
    }

    /**
     * 获取当前文章的上一篇
     *
     * @param ctx thymeleaf的上下文
     */
    public static Post prevPost(WebEngineContext ctx) {
        Post post = currentPost(ctx);
        return null != post ? postService.getPrevOrNextPost(Const.PAGE_PREV, post.getPubdate()) : null;
    }

    /**
     * 当前文章的下一篇文章超链接
     *
     * @param ctx thymeleaf的上下文
     */
    public static String prevPostLink(WebEngineContext ctx) {
        Post post = prevPost(ctx);
        return post != null ? prevPostLink(ctx, title(post)) : "";
    }

    /**
     * 当前文章的上一篇文章超链接
     *
     * @param ctx   thymeleaf的上下文
     * @param title 显示的标题
     */
    public static String prevPostLink(WebEngineContext ctx, String title) {
        Post post = prevPost(ctx);
        if (post != null) {
            return "<a href=\"" + permalink(post) + "\" title=\"" + title(post) + "\">" + title + "</a>";
        }
        return "";
    }

    /**
     * 获取文章的链接地址
     *
     * @param post 文章
     */
    public static String permalink(Post post) {
        Long id = post.getId();
        String slug = post.getSlug();
        return Common.blogUrl("/post/" + (StringUtils.isNotEmpty(slug) ? slug : id.toString()));
    }

    /**
     * 获取文章标题
     *
     * @param ctx thymeleaf的上下文
     */
    public static String title(WebEngineContext ctx) {
        return title(currentPost(ctx));
    }

    /**
     * 获取文章标题
     *
     * @param post 文章
     */
    public static String title(Post post) {
        return null != post ? post.getTitle() : Common.blogTitle();
    }

    /**
     * 获取文章内容，格式化markdown后的
     *
     * @param ctx thymeleaf的上下文
     */
    public static String postContent(WebEngineContext ctx) {
        Post post = currentPost(ctx);
        return null != post ? convertContent(post.getContent()) : "";
    }

    /**
     * 转换markdown为html用于页面显示
     *
     * @param content 需要转换的markdown内容
     */
    public static String convertContent(String content) {
        if (StringUtils.isNotBlank(content)) {
            content = content.replace("<!--more-->", "\r\n");
            content = BlogUtils.mdToHtml(content);
        }
        return content;
    }

    /**
     * 获取文章或页面下的评论
     * 以嵌套方式表示父子评论的包含关系
     *
     * @param ctx  thymeleaf的上下文
     * @param size 评论数量
     * @return 评论内容
     */
    public static IPage<CommentDTO> commentPage(WebEngineContext ctx, long size) {
        long page = (long) ctx.getVariable("cp");
        Post post = currentPost(ctx);
        return commentService.getCommentsByOwnerId(post.getId(), page, size);
    }

    /**
     * 获取文章或页面下的评论
     *
     * @param ctx  thymeleaf的上下文
     * @param size 评论数量
     * @return 评论内容
     */
    public static IPage<CommentDTO> flatCommentPage(WebEngineContext ctx, long size) {
        long page = (long) ctx.getVariable("cp");
        Post post = currentPost(ctx);
        return commentService.getFlatCommentsByOwnerId(post.getId(), page, size);
    }

    /**
     * 获取评论@信息
     *
     * @param comment 评论
     * @return 被@人的超链接
     */
    public static String commentAt(CommentDTO comment) {
        Comment parentComment = comment.getParent();
        return "<a href=\"#comment-" + parentComment.getId() + "\">@" + parentComment.getAuthorName() + "</a>";
    }

    /**
     * 获取Post Licenses 信息
     */
    public static String postLicenses() {
        StringBuilder sb = new StringBuilder();
        sb.append("本文采用 ");
        sb.append("<a href=\"https://creativecommons.org/licenses/by/4.0/deed.zh\" target=\"_blank\" rel=\"external nofollow\">知识共享署名4.0</a>");
        sb.append("国际许可协议进行许可. 本站文章除注明转载/出处外, 均为本站原创或翻译，转载前请务必署名!");
        return sb.toString();
    }

    /**
     * 获取指定文章的标签
     *
     * @param post  文章
     * @param split 每个标签之间的分隔符
     * @return 超链接形式的标签
     */
    public static String tags(Post post, String split) {
        String tags = post.getTags();
        if (StringUtils.isBlank(tags)) {
            return "";
        }
        try {
            String[] arr = tags.split(",");
            StringBuilder sb = new StringBuilder();
            for (String tag : arr) {
                sb.append(split).append("<a href=\"/tag/" + URLEncoder.encode(tag, "UTF-8") + "\">" + tag + "</a>");
            }
            return split.length() > 0 ? sb.substring(split.length() - 1) : sb.toString();
        } catch (UnsupportedEncodingException e) {
            logger.error("标签转换失败", e);
            return "";
        }
    }

    /**
     * 获取文章分类
     *
     * @param post 文章
     */
    public static String category(Post post) {
        Long categoryId = post.getCategoryId();
        if (categoryId == null) {
            return "";
        }
        Category category = categoryService.getCategoryById(categoryId);
        String categoryName = category.getName();
        return "<a href=\"/post/category/" + categoryId + "\">" + categoryName + "</a>";
    }

    /**
     * 获取文章作者
     *
     * @param post 文章
     */
    public static String author(Post post) {
        Long userId = post.getAuthorId();
        User user = userService.getUserById(userId);
        return user.getNickname();
    }

}
