package com.developerchen.core.domain.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import org.hibernate.validator.constraints.Length;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotNull;
import java.io.Serial;
import java.util.Date;

/**
 * <p>
 * 设置表
 * </p>
 *
 * @author syc
 */
@Validated
@TableName("sys_option")
public class Option extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    @NotNull(message = "配置名称不能为空")
    @Length(max = 50, message = "配置名称长度不能超过 50 个字符")
    private String name;

    @Length(max = 5000, message = "设置值长度不能超过 5000 个字符")
    private String value;

    @Length(max = 200, message = "Label长度不能超过 200 个字符")
    private String label;

    @Length(max = 200, message = "描述长度不能超过 200 个字符")
    private String description;

    @TableField(fill = FieldFill.INSERT)
    private Date createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;


    public String getName() {
        return name;
    }

    public Option setName(String name) {
        this.name = name;
        return this;
    }

    public String getValue() {
        return value;
    }

    public Option setValue(String value) {
        this.value = value;
        return this;
    }

    public String getLabel() {
        return label;
    }

    public Option setLabel(String label) {
        this.label = label;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public Option setDescription(String description) {
        this.description = description;
        return this;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public Option setCreateTime(Date createTime) {
        this.createTime = createTime;
        return this;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public Option setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
        return this;
    }

}
