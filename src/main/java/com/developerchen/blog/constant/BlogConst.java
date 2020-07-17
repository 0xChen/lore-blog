package com.developerchen.blog.constant;

import com.developerchen.core.config.AppConfig;
import com.developerchen.core.constant.Const;

import java.io.File;
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

    /**
     * 博客成功安装后生成的文件, 通过判断这个文件是否存在可以识别博客安装状态
     */
    public static final File INSTALLED = new File(AppConfig.HOME_PATH
            + File.separator + "lock" + File.separator + "Installed");

    /**
     * 标识博客安装状态
     */
    public static Boolean HAS_INSTALLED;

    /**
     * 安装页面URI
     */
    public static final String INSTALL_URI = "/blog/install";

    /* =================== option 相关常量 ==================*/
    public static final String OPTION_BLOG_THEME = "blog_theme";
    public static final String OPTION_BLOG_TITLE = "blog_title";
    public static final String OPTION_BLOG_DESCRIPTION = "blog_description";
    public static final String OPTION_THEME_LOGO_URL = "theme_logo_url";
    public static final String OPTION_ALLOW_COMMENT_APPROVE = "allow_comment_approve";
    /**
     * 每个主题自定义配置项, 以 'theme_' + "themeName" 作为 name 保存在 option 表中
     */
    public static final String OPTION_THEME_OPTION_PREFIX = "theme_";

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
     * 已批准
     */
    public static final String COMMENT_STATUS_APPROVED = "1";
    /**
     * 未批准
     */
    public static final String COMMENT_STATUS_UNAPPROVED = "0";
    /**
     * 等待审批
     */
    public static final String COMMENT_STATUS_PENDING = "2 ";
    /**
     * 一次获取最近评论的数量
     */
    public static final long COMMENT_RECENT_SIZE = 50;

    /* =================== category 相关常量 ====================*/
    /**
     * 顶级默认分类名称
     */
    public static final Long CATEGORY_ROOT_ID = 0L;
    public static final String CATEGORY_ROOT_NAME = "默认分类";

    public static final String MP3_PREFIX = "[mp3:";
    public static final String MUSIC_IFRAME = "<iframe frameborder=\"no\" border=\"0\" marginwidth=\"0\" marginheight=\"0\" width=350 height=106 src=\"//music.163.com/outchain/player?type=2&id=$1&auto=0&height=88\"></iframe>";
    public static final String MUSIC_REG_PATTERN = "\\[mp3:(\\d+)\\]";

}
