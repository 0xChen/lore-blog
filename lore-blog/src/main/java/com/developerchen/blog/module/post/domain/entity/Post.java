package com.developerchen.blog.module.post.domain.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.developerchen.core.base.entity.BaseEntity;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;
import org.springframework.validation.annotation.Validated;

import java.io.Serial;
import java.time.LocalDateTime;

/**
 * <p>
 * 文章与页面表
 * </p>
 *
 * @author syc
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Validated
@TableName("blog_post")
public class Post extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;


    /**
     * 标题
     */
    @NotNull(message = "文章标题不能为空")
    @Length(max = 200, message = "文章标题不能超过 200 个字符")
    private String title;

    /**
     * 文章缩略名, 用于自定义访问路径
     */
    @Length(max = 300, message = "文章缩略名不能超过 300 个字符")
    private String slug;

    /**
     * 作者
     */
    @JsonSerialize(using = ToStringSerializer.class)
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
    @JsonSerialize(using = ToStringSerializer.class)
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
    private LocalDateTime pubdate;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
