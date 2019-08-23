package com.developerchen.blog.util;

import com.developerchen.blog.constant.BlogConst;
import com.vladsch.flexmark.ext.autolink.AutolinkExtension;
import com.vladsch.flexmark.ext.definition.DefinitionExtension;
import com.vladsch.flexmark.ext.emoji.EmojiExtension;
import com.vladsch.flexmark.ext.gfm.strikethrough.StrikethroughExtension;
import com.vladsch.flexmark.ext.gfm.tasklist.TaskListExtension;
import com.vladsch.flexmark.ext.tables.TablesExtension;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.data.DataHolder;
import com.vladsch.flexmark.util.data.MutableDataSet;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;

/**
 * Blog 工具类
 *
 * @author syc
 */
public class BlogUtils {

    /**
     * markdown options.
     */
    private static final DataHolder OPTIONS = new MutableDataSet()
            .set(Parser.EXTENSIONS, Arrays.asList(
                    TablesExtension.create(),
                    TaskListExtension.create(),
                    StrikethroughExtension.create(),
                    AutolinkExtension.create(),
                    EmojiExtension.create(),
                    DefinitionExtension.create()
            ));

    /**
     * markdown parser.
     */
    private static final Parser PARSER = Parser.builder(OPTIONS).build();

    /**
     * markdown HTML renderer.
     */
    private static final HtmlRenderer RENDERER = HtmlRenderer.builder(OPTIONS).build();


    /**
     * markdown转换为html
     *
     * @param markdown markdown格式字符串
     * @return html格式的字符串
     */
    public static String mdToHtml(String markdown) {
        if (StringUtils.isBlank(markdown)) {
            return StringUtils.EMPTY;
        }

        Node document = PARSER.parse(markdown);
        String html = RENDERER.render(document);

        // 处理网易云音乐输出
        if (html.contains(BlogConst.MP3_PREFIX)) {
            html = html.replaceAll(BlogConst.MUSIC_REG_PATTERN, BlogConst.MUSIC_IFRAME);
        }

        return html;
    }

    /**
     * 提取html中的文字
     *
     * @param html 符合html格式的字符串
     */
    public static String htmlToText(String html) {
        String text = "";
        if (StringUtils.isNotBlank(html)) {
            text = html.replaceAll("(?s)<[^>]*>(\\s*<[^>]*>)*", "");
        }
        return text;
    }
}
