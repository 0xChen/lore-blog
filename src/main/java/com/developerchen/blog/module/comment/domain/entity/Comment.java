package com.developerchen.blog.module.comment.domain.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.developerchen.core.domain.entity.BaseEntity;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.URL;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import java.util.Date;

/**
 * <p>
 * 评论表
 * </p>
 *
 * @author syc
 */
@Validated
@TableName("blog_comment")
public class Comment extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 评论所属的主体
     */
    private Long ownerId;

    /**
     * 父级评论
     */
    private Long parentId;

    /**
     * 评论者ID
     */
    private Long authorId;

    /**
     * 评论者昵称
     */
    @NotBlank(message = "请填写昵称")
    @Length(max = 20, message = "昵称不能超过20个字符")
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
    @Length(max = 2000, message = "评论内容不能超出2000个字符")
    @Length(min = 3, message = "评论内容至少要3个字符")
    private String content;

    /**
     * 评论状态
     */
    private String status;

    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;


    public Long getOwnerId() {
        return ownerId;
    }

    public Comment setOwnerId(Long ownerId) {
        this.ownerId = ownerId;
        return this;
    }

    @JsonSerialize(using = ToStringSerializer.class)
    public Long getParentId() {
        return parentId;
    }

    public Comment setParentId(Long parentId) {
        this.parentId = parentId;
        return this;
    }

    @JsonSerialize(using = ToStringSerializer.class)
    public Long getAuthorId() {
        return authorId;
    }

    public Comment setAuthorId(Long authorId) {
        this.authorId = authorId;
        return this;
    }

    public String getAuthorName() {
        return authorName;
    }

    public Comment setAuthorName(String authorName) {
        this.authorName = authorName;
        return this;
    }

    public String getEmail() {
        return email;
    }

    public Comment setEmail(String email) {
        this.email = email;
        return this;
    }

    public String getUrl() {
        return url;
    }

    public Comment setUrl(String url) {
        this.url = url;
        return this;
    }

    public String getIp() {
        return ip;
    }

    public Comment setIp(String ip) {
        this.ip = ip;
        return this;
    }

    public String getAgent() {
        return agent;
    }

    public Comment setAgent(String agent) {
        this.agent = agent;
        return this;
    }

    public String getContent() {
        return content;
    }

    public Comment setContent(String content) {
        this.content = content;
        return this;
    }

    public String getStatus() {
        return status;
    }

    public Comment setStatus(String status) {
        this.status = status;
        return this;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public Comment setCreateTime(Date createTime) {
        this.createTime = createTime;
        return this;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public Comment setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
        return this;
    }

}
