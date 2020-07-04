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

    /**
     * 附件在磁盘中的文件名, 附件上传后会生成一个唯一的文件名并已这个名字保存到磁盘中
     */
    private String name;
    /**
     * 原始文件名
     */
    private String originalName;
    /**
     * 附件类型
     */
    private String type;
    /**
     * 附件大小
     */
    private Long size;
    /**
     * 文件的SHA1值
     */
    private String sha1;

    @TableField("`key`")
    private String key;
    /**
     * 附件描述
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

    public String getSha1() {
        return sha1;
    }

    public void setSha1(String sha1) {
        this.sha1 = sha1;
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
