package com.developerchen.blog.module.category.domain.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.developerchen.core.domain.entity.BaseEntity;
import org.hibernate.validator.constraints.Length;

import java.util.Date;

/**
 * <p>
 * 分类表
 * </p>
 *
 * @author syc
 */
@TableName("blog_category")
public class Category extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 分类名称
     */
    @Length(max = 25, message = "分类名称不能超过 25 个字符")
    private String name;

    /**
     * 左值
     */
    private Integer leftValue;

    /**
     * 右值
     */
    private Integer rightValue;

    /**
     * 是否可见
     */
    private String visible;

    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;


    public String getName() {
        return name;
    }

    public Category setName(String name) {
        this.name = name;
        return this;
    }

    public Integer getLeftValue() {
        return leftValue;
    }

    public Category setLeftValue(Integer leftValue) {
        this.leftValue = leftValue;
        return this;
    }

    public Integer getRightValue() {
        return rightValue;
    }

    public Category setRightValue(Integer rightValue) {
        this.rightValue = rightValue;
        return this;
    }

    public String getVisible() {
        return visible;
    }

    public Category setVisible(String visible) {
        this.visible = visible;
        return this;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public Category setCreateTime(Date createTime) {
        this.createTime = createTime;
        return this;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public Category setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
        return this;
    }

}
