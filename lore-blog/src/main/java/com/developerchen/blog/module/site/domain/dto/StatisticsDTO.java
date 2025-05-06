package com.developerchen.blog.module.site.domain.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 后台统计对象
 * <p>
 *
 * @author syc
 */
@Data
public class StatisticsDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 4765338351529564703L;

    /**
     * 文章数量
     */
    private Long posts;

    /**
     * 评论数量
     */
    private Long comments;

    /**
     * 分类数量
     */
    private Long categories;

    /**
     * 标签数量
     */
    private Long tags;

    /**
     * 附件数量
     */
    private Long attachments;
}
