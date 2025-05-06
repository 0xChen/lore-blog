package com.developerchen.blog.module.link.domain.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.developerchen.core.base.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.URL;

import java.io.Serial;
import java.time.LocalDateTime;

/**
 * <p>
 * 链接表
 * </p>
 *
 * @author syc
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
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
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

}
