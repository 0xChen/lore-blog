package com.developerchen.blog.module.site.domain.dto;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.io.Serializable;

/**
 * 后台统计对象
 * <p>
 *
 * @author syc
 */
public class StatisticsDTO implements Serializable {

    private static final long serialVersionUID = 4765338351529564703L;

    /**
     * 文章数量
     */
    private Integer posts;

    /**
     * 评论数量
     */
    private Integer comments;

    /**
     * 分类数量
     */
    private Integer categories;

    /**
     * 标签数量
     */
    private Integer tags;

    /**
     * 文件数量
     */
    private Integer files;


    public Integer getPosts() {
        return posts;
    }

    public void setPosts(Integer posts) {
        this.posts = posts;
    }

    public Integer getComments() {
        return comments;
    }

    public void setComments(Integer comments) {
        this.comments = comments;
    }

    public Integer getCategories() {
        return categories;
    }

    public void setCategories(Integer categories) {
        this.categories = categories;
    }

    public Integer getTags() {
        return tags;
    }

    public void setTags(Integer tags) {
        this.tags = tags;
    }

    public Integer getFiles() {
        return files;
    }

    public void setFiles(Integer files) {
        this.files = files;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
    }
}
