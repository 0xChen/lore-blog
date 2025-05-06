package com.developerchen.blog.module.comment.domain.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.developerchen.core.base.entity.BaseEntity;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.URL;
import org.springframework.validation.annotation.Validated;

import java.io.Serial;
import java.time.LocalDateTime;

/**
 * <p>
 * 评论表
 * </p>
 *
 * @author syc
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Validated
@TableName("blog_comment")
public class Comment extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 评论所属的主体
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long ownerId;

    /**
     * 父级评论
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long parentId;

    /**
     * 评论者ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long authorId;

    /**
     * 评论者昵称
     */
    @NotBlank(message = "请填写昵称")
    @Length(max = 20, message = "昵称不能超过 20 个字符")
    private String authorName;

    /**
     * 评论者Email
     */
    @Email
    private String email;

    /**
     * 评论者网址
     */
    @URL
    @Length(max = 200, message = "网址不能超过 200 个字符")
    private String url;

    /**
     * 评论者IP
     */
    private String ip;

    /**
     * 评论者客户端
     */
    private String agent;

    /**
     * 评论内容
     */
    @Length(max = 2000, message = "评论内容不能超出 2000 个字符")
    @Length(min = 3, message = "评论内容至少要 3 个字符")
    private String content;

    /**
     * 评论状态
     */
    private String status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

}
