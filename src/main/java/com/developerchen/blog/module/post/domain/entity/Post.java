package com.developerchen.blog.module.post.domain.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.developerchen.core.domain.entity.BaseEntity;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import org.hibernate.validator.constraints.Length;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.NotNull;
import java.util.Date;

/**
 * <p>
 * 文章与页面表
 * </p>
 *
 * @author syc
 */
@Validated
@TableName("blog_post")
public class Post extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 标题
     */
    @NotNull(message = "文章标题不能为空")
    @Length(max = 200, message = "文章标题不能超过200个字符")
    private String title;

    /**
     * 文章缩略名, 用于自定义访问路径
     */
    @Length(max = 300, message = "文章缩略名不能超过300个字符")
    private String slug;

    /**
     * 作者
     */
    private Long authorId;

    /**
     * 缩略图
     */
    private String thumbnail;

    /**
     * 文章内容
     */
    private String content;

    /**
     * 标签
     */
    @Length(max = 300, message = "标签不能超过255个字符")
    private String tags;

    /**
     * post类型文章, 页面等
     */
    @Length(max = 10)
    private String type;

    /**
     * 内容类型html, markdown等
     */
    @Length(max = 10)
    private String contentType;

    /**
     * 分类
     */
    private Long categoryId;

    /**
     * 文章状态
     */
    private String status;

    /**
     * 评论状态
     */
    private String commentStatus;

    private String pingStatus;

    /**
     * 评论数量
     */
    private Integer commentCount;

    /**
     * 阅读次数
     */
    private Integer readCount;

    /**
     * 发布时间
     */
    private Date pubdate;

    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;


    public String getTitle() {
        return title;
    }

    public Post setTitle(String title) {
        this.title = title;
        return this;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    @JsonSerialize(using = ToStringSerializer.class)
    public Long getAuthorId() {
        return authorId;
    }

    public Post setAuthorId(Long authorId) {
        this.authorId = authorId;
        return this;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public String getContent() {
        return content;
    }

    public Post setContent(String content) {
        this.content = content;
        return this;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public String getType() {
        return type;
    }

    public Post setType(String type) {
        this.type = type;
        return this;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    @JsonSerialize(using = ToStringSerializer.class)
    public Long getCategoryId() {
        return categoryId;
    }

    public Post setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
        return this;
    }

    public String getStatus() {
        return status;
    }

    public Post setStatus(String status) {
        this.status = status;
        return this;
    }

    public String getCommentStatus() {
        return commentStatus;
    }

    public Post setCommentStatus(String commentStatus) {
        this.commentStatus = commentStatus;
        return this;
    }

    public String getPingStatus() {
        return pingStatus;
    }

    public Post setPingStatus(String pingStatus) {
        this.pingStatus = pingStatus;
        return this;
    }

    public Integer getCommentCount() {
        return commentCount;
    }

    public Post setCommentCount(Integer commentCount) {
        this.commentCount = commentCount;
        return this;
    }

    public Integer getReadCount() {
        return readCount;
    }

    public Post setReadCount(Integer readCount) {
        this.readCount = readCount;
        return this;
    }

    public Date getPubdate() {
        return pubdate;
    }

    public void setPubdate(Date pubdate) {
        this.pubdate = pubdate;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public Post setCreateTime(Date createTime) {
        this.createTime = createTime;
        return this;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public Post setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
        return this;
    }

}
