package com.developerchen.core.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.developerchen.core.base.entity.DataEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serial;

/**
 * <p>
 * 附件 文件、图片等
 * </p>
 *
 * @author syc
 */

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@TableName("sys_attachment")
public class Attachment extends DataEntity {

    @Serial
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
     * 如果是图片类型存放图片的宽度
     */
    private Integer width;
    /**
     * 如果是图片类型存放图片的高度
     */
    private Integer height;

}
