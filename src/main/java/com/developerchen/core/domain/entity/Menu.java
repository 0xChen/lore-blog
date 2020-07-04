package com.developerchen.core.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotNull;

/**
 * <p>
 * 菜单表
 * </p>
 *
 * @author syc
 */
@TableName("sys_menu")
public class Menu extends BaseEntity {

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


    public String getName() {
        return name;
    }

    public Menu setName(String name) {
        this.name = name;
        return this;
    }

    @JsonSerialize(using = ToStringSerializer.class)
    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public String getStatus() {
        return status;
    }

    public Menu setStatus(String status) {
        this.status = status;
        return this;
    }

    public String getIcon() {
        return icon;
    }

    public Menu setIcon(String icon) {
        this.icon = icon;
        return this;
    }

    public String getUrl() {
        return url;
    }

    public Menu setUrl(String url) {
        this.url = url;
        return this;
    }

    public Integer getSort() {
        return sort;
    }

    public Menu setSort(Integer sort) {
        this.sort = sort;
        return this;
    }

    public Integer getLevel() {
        return level;
    }

    public Menu setLevel(Integer level) {
        this.level = level;
        return this;
    }

}
