package com.developerchen.blog.util;

import com.developerchen.blog.constant.BlogConst;
import com.vdurmont.emoji.EmojiParser;
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
            ))
            // 表分隔符列中的 - 或个 : 字符的最小数目
            .set(TablesExtension.MIN_SEPARATOR_DASHES, 1);

    /**
     * markdown parser.
     */
    private static final Parser PARSER = Parser.builder(OPTIONS).build();

    /**
     * markdown HTML renderer.
     */
    private static final HtmlRenderer RENDERER = HtmlRenderer.builder(OPTIONS).build();

    /**
     * 字符转换为emoji表情
     * Examples:<br>
     * <code>:smile:</code> will be replaced by <code>😄</code><br>
     * <code>&amp;#128516;</code> will be replaced by <code>😄</code><br>
     * <code>:boy|type_6:</code> will be replaced by <code>👦🏿</code>
     *
     * @param value the string to parse
     * @return emoji表情unicode字符串
     */
    public static String emoji(String value) {
        return EmojiParser.parseToUnicode(value);
    }

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

        markdown = emoji(markdown);

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
