package com.developerchen.blog.module.site.domain.dto;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.io.Serial;
import java.io.Serializable;

/**
 * 后台统计对象
 * <p>
 *
 * @author syc
 */
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


    public Long getPosts() {
        return posts;
    }

    public void setPosts(Long posts) {
        this.posts = posts;
    }

    public Long getComments() {
        return comments;
    }

    public void setComments(Long comments) {
        this.comments = comments;
    }

    public Long getCategories() {
        return categories;
    }

    public void setCategories(Long categories) {
        this.categories = categories;
    }

    public Long getTags() {
        return tags;
    }

    public void setTags(Long tags) {
        this.tags = tags;
    }

    public Long getAttachments() {
        return attachments;
    }

    public void setAttachments(Long attachments) {
        this.attachments = attachments;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
    }
}
