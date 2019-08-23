package com.developerchen.core.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;

/**
 * <p>
 * 附件 文件、图片等
 * </p>
 *
 * @author syc
 */
@TableName("sys_attachment")
public class Attachment extends DataEntity {

    private static final long serialVersionUID = 1L;

    private String name;
    /**
     * 原始文件名
     */
    private String originalName;
    /**
     * 类型
     */
    private String type;

    private Long size;

    @TableField("`key`")
    private String key;
    /**
     * 资源描述
     */
    private String description;
    /**
     * 如果是图片类型存放图片的高度
     */
    private Integer height;
    /**
     * 如果是图片类型存放图片的宽度
     */
    private Integer width;


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOriginalName() {
        return originalName;
    }

    public Attachment setOriginalName(String originalName) {
        this.originalName = originalName;
        return this;
    }

    public String getType() {
        return type;
    }

    public Attachment setType(String type) {
        this.type = type;
        return this;
    }

    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size;
    }

    public String getKey() {
        return key;
    }

    public Attachment setKey(String key) {
        this.key = key;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public Attachment setDescription(String description) {
        this.description = description;
        return this;
    }

    public Integer getHeight() {
        return height;
    }

    public Attachment setHeight(Integer height) {
        this.height = height;
        return this;
    }

    public Integer getWidth() {
        return width;
    }

    public Attachment setWidth(Integer width) {
        this.width = width;
        return this;
    }
}
