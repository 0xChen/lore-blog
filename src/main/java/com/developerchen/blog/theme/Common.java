package com.developerchen.blog.theme;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.developerchen.blog.constant.BlogConst;
import com.developerchen.blog.module.post.domain.entity.Post;
import com.developerchen.blog.util.BlogUtils;
import com.developerchen.core.config.AppConfig;
import com.developerchen.core.constant.Const;
import com.developerchen.core.domain.entity.User;
import com.developerchen.core.util.UserUtils;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;

import java.util.Date;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Blog公共函数
 * <p>
 *
 * @author syc
 */
public final class Common {

    /**
     * 通过Installed文件是否存在判断程序是否已经安装过了
     *
     * @return true 安装过
     */
    public static boolean hasInstalled() {
        return BlogConst.INSTALLED.exists();
    }

    /**
     * 获取网站首页链接
     */
    public static String blogUrl() {
        return getOption(Const.OPTION_SCHEME) + "://" + getOption(Const.OPTION_HOSTNAME);
    }

    /**
     * 获取网站下子链接
     *
     * @param sub 后面追加的地址
     */
    public static String blogUrl(String sub) {
        return blogUrl() + sub;
    }

    /**
     * 网站标题
     */
    public static String blogTitle() {
        return getOption(BlogConst.OPTION_BLOG_TITLE);
    }

    /**
     * 获取网站关键字
     */
    public static String blogKeywords() {
        return getOption("blog_keywords");
    }

    /**
     * 获取网站描述信息
     */
    public static String blogDescription() {
        return getOption(BlogConst.OPTION_BLOG_DESCRIPTION);
    }


    /**
     * 获取当前主题名称
     */
    public static String blogTheme() {
        return getOption(BlogConst.OPTION_BLOG_THEME, "default");
    }

    /**
     * 获取网站指定的配置项值
     *
     * @param name 配置项名称
     * @return value 配置项值
     */
    public static String getOption(String name) {
        return AppConfig.getOption(name);
    }

    /**
     * 获取网站指定的配置项值, 如果没有此配置项或者此配置项对应的值是 null 或者 ""
     * 则返回默认值
     *
     * @param name         配置项名称
     * @param defaultValue 默认值
     * @return value 配置项值
     */
    public static String getOption(String name, String defaultValue) {
        String value = AppConfig.getOption(name);
        return StringUtils.isEmpty(value) ? defaultValue : value;
    }

    /**
     * 判断分页中是否有数据
     *
     * @param page 分页对象
     * @return boolean
     */
    public static boolean isEmptyPage(IPage<?> page) {
        return null == page || page.getTotal() == 0;
    }

    /**
     * 获取gravatar头像地址
     *
     * @param email 邮箱地址
     */
    public static String gravatar(String email) {
        String gravatarUrl = "https://cn.gravatar.com/avatar/";
        if (StringUtils.isBlank(email)) {
            return gravatarUrl;
        }
        String emailHash = DigestUtils.md5Hex(email.toLowerCase().trim());
        return gravatarUrl + emailHash;
    }

    /**
     * 获取1到指定数值之间的随机整数, 随机值的范围包含最大值与1本身
     *
     * @param max 最大值
     * @return 随机整数的字符串形式
     */
    public static String random(int max) {
        int r = new Random().nextInt(max) + 1;
        return String.valueOf(r);
    }

    private static final Pattern SRC_PATTERN = Pattern.compile("src\\s*=\\s*\'?\"?(.*?)(\'|\"|>|\\s+)");

    /**
     * 获取文章的缩略图地址, 如果没有则取文章内容中的第一张图片作为文章的缩略图
     */
    public static String showThumb(Post post) {
        if (StringUtils.isNotEmpty(post.getThumbnail())) {
            return post.getThumbnail();
        }
        return showThumb(post.getContent());
    }

    /**
     * 获取文章中第一张图片地址
     *
     * @param content 文章内容
     * @return 第一张图片的URL
     */
    public static String showThumb(String content) {
        content = BlogUtils.mdToHtml(content);
        String imgTagPrefix = "<img";
        if (content.contains(imgTagPrefix)) {
            String img = "";
            String imgReg = "<img\\s*src\\s*=\\s*(.*?)[^>]*?>";
            Pattern imagePattern = Pattern.compile(imgReg, Pattern.CASE_INSENSITIVE);
            Matcher imageMatcher = imagePattern.matcher(content);
            if (imageMatcher.find()) {
                img = img + "," + imageMatcher.group();
                // 匹配src
                Matcher m = SRC_PATTERN.matcher(img);
                if (m.find()) {
                    return m.group(1);
                }
            }
        }
        return "";
    }

    /**
     * 是文章中否有图片
     *
     * @param post 文章
     * @return true 有图片, false 无图片
     */
    public static boolean hasThumbnail(Post post) {
        return !StringUtils.isBlank(showThumb(post));
    }

    /**
     * 截取文章摘要
     *
     * @param content  文章内容
     * @param len      要截取文字的个数
     * @param ellipsis 省略符号
     * @return 文章摘要
     */
    public static String intro(String content, int len, String ellipsis) {
        String intro;
        int pos = content.indexOf("<!--more-->");
        if (pos != -1) {
            String html = content.substring(0, pos);
            intro = BlogUtils.htmlToText(BlogUtils.mdToHtml(html));
        } else {
            String text = BlogUtils.htmlToText(BlogUtils.mdToHtml(content));
            if (text.length() > len) {
                intro = text.substring(0, len) + ellipsis;
            } else {
                intro = text;
            }
        }
        return intro;
    }

    /**
     * 将指定时间格式化为"yyyy-MM-dd'T'HH:mm:ss"格式
     */
    public static String dateFormat(Date date) {
        return DateFormatUtils.ISO_8601_EXTENDED_DATETIME_FORMAT.format(date);
    }

    /**
     * 获取当前登陆用户
     */
    public static User user() {
        return UserUtils.getUser();
    }
}
