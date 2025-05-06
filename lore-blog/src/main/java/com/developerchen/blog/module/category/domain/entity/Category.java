package com.developerchen.blog.module.category.domain.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.developerchen.core.base.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;
import org.springframework.validation.annotation.Validated;

import java.io.Serial;
import java.time.LocalDateTime;

/**
 * <p>
 * 分类表
 * </p>
 *
 * @author syc
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@Validated
@TableName("blog_category")
public class Category extends BaseEntity {

    @Serial
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
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

}
