package com.developerchen.core.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.developerchen.core.base.entity.BaseEntity;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.validator.constraints.Length;

import java.io.Serial;

/**
 * <p>
 * 菜单表
 * </p>
 *
 * @author syc
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("sys_menu")
public class Menu extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 菜单名称
     */
    @NotNull(message = "菜单名称不能为空")
    @Length(max = 50, message = "菜单名称长度不能超过 50 个字符")
    private String name;
    /**
     * 父级菜单ID
     */
    private Long parentId;
    /**
     * 菜单状态'
     */
    private String status;
    /**
     * 图标
     */
    @Length(max = 255, message = "图标地址长度不能超过 255 个字符")
    private String icon;
    /**
     * 连接地址
     */
    @Length(max = 255, message = "地址长度不能超过 255 个字符")
    private String url;
    /**
     * 排序
     */
    private Integer sort;
    /**
     * 层级
     */
    private Integer level;
}
