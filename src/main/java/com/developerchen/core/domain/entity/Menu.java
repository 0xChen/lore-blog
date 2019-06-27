package com.developerchen.core.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

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

    private String name;
    /**
     * 父级菜单ID
     */
    private Long parentId;
    private String status;
    /**
     * 图标
     */
    private String icon;
    /**
     * 连接地址
     */
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
