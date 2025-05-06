package com.developerchen.core.domain.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.developerchen.core.base.entity.BaseEntity;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;
import org.springframework.validation.annotation.Validated;

import java.io.Serial;
import java.time.LocalDateTime;

/**
 * <p>
 * 设置表
 * </p>
 *
 * @author syc
 */

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
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
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

}
