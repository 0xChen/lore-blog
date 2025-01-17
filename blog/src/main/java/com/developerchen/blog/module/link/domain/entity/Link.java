package com.developerchen.blog.module.link.domain.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.developerchen.core.domain.entity.BaseEntity;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.URL;

import java.io.Serial;
import java.util.Date;

/**
 * <p>
 * 链接表
 * </p>
 *
 * @author syc
 */
@TableName("blog_link")
public class Link extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 网站名称
     */
    @Length(max = 100, message = "网站名称不能超过 100 个字符")
    private String name;
    /**
     * 网站链接
     */
    @URL
    @Length(max = 255, message = "网址不能超过 255 个字符")
    private String url;
    /**
     * 排序
     */
    private Long sort;
    /**
     * 网站描述
     */
    @Length(max = 255, message = "网站描述不能超过 255 个字符")
    private String description;
    /**
     * 是否显示
     */
    private String visible;

    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;


    public String getName() {
        return name;
    }

    public Link setName(String name) {
        this.name = name;
        return this;
    }

    public String getUrl() {
        return url;
    }

    public Link setUrl(String url) {
        this.url = url;
        return this;
    }

    public Long getSort() {
        return sort;
    }

    public Link setSort(Long sort) {
        this.sort = sort;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public Link setDescription(String description) {
        this.description = description;
        return this;
    }

    public String getVisible() {
        return visible;
    }

    public Link setVisible(String visible) {
        this.visible = visible;
        return this;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public Link setCreateTime(Date createTime) {
        this.createTime = createTime;
        return this;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public Link setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
        return this;
    }

}
