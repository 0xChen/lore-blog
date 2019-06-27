package com.developerchen.blog.constant;

import com.developerchen.core.constant.Const;

import java.lang.reflect.Field;
import java.util.Map;

/**
 * Blog 应用常量
 *
 * @author syc
 */
@SuppressWarnings("all")
public final class BlogConst {

    private BlogConst() {
    }

    /**
     * 将Blog中的常量注册到Const.constMap中
     *
     * @throws IllegalAccessException
     */
    public static void init() throws IllegalAccessException {
        for (Field field : BlogConst.class.getFields()) {
            if (!field.getType().isAssignableFrom(Map.class)) {
                Const.CONST_MAP.put(field.getName(), field.get(null));
            }
        }
    }

    /* =================== option 相关常量 ==================*/
    public static final String BLOG_THEME = "blog_theme";
    public static final String BLOG_TITLE = "blog_title";
    public static final String BLOG_DESCRIPTION = "blog_description";
    public static final String BLOG_LOGO_URL = "blog_logo_url";

    /* =================== post 相关常量 ====================*/
    /**
     * 最近文章
     */
    public static final String POST_RECENT = "recent";
    /**
     * 随机文章
     */
    public static final String POST_RANDOM = "random";
    /**
     * 每次获取随机文章的数量
     */
    public static final long POST_RANDOM_SIZE = 10;
    /**
     * 删除状态
     */
    public static final String POST_STATUS_TRASH = "0";
    /**
     * 自动草稿
     */
    public static final String POST_STATUS_AUTO_DRAFT = "1";
    /**
     * 草稿状态
     */
    public static final String POST_STATUS_DRAFT = "2";
    /**
     * 发布状态
     */
    public static final String POST_STATUS_PUBLISH = "3";
    /**
     * markdown 格式
     */
    public static final String POST_CONTENT_TYPE_MD = "markdown";
    /**
     * html 格式
     */
    public static final String POST_CONTENT_TYPE_HTML = "html";
    /**
     * 博文
     */
    public static final String POST_TYPE_POST = "post";
    /**
     * 页面
     */
    public static final String POST_TYPE_PAGE = "page";

    /* =================== comment 相关常量 ====================*/
    /**
     * 审批通过
     */
    public static final String COMMENT_APPROVED = "1";
    /**
     * 审核状态
     */
    public static final String COMMENT_NO_AUDIT = "0";
    /**
     * 一次获取最近评论的数量
     */
    public static final long COMMENT_RECENT_SIZE = 5;

    /* =================== category 相关常量 ====================*/
    /**
     * 顶级默认分类名称
     */
    public static final Long CATEGORY_ROOT_ID = 0L;
    public static final String CATEGORY_ROOT_NAME = "默认分类";

    public static final String MP3_PREFIX = "[mp3:";
    public static final String MUSIC_IFRAME = "<iframe frameborder=\"no\" border=\"0\" marginwidth=\"0\" marginheight=\"0\" width=350 height=106 src=\"//music.163.com/outchain/player?type=2&id=$1&auto=0&height=88\"></iframe>";
    public static final String MUSIC_REG_PATTERN = "\\[mp3:(\\d+)\\]";

    public static final String OPTION_SITE_THEME = "site_theme";
    public static final String OPTION_ALLOW_INSTALL = "allow_install";
    public static final String OPTION_ALLOW_COMMENT_AUDIT = "allow_comment_audit";
}
