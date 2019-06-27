package com.developerchen.blog.module.site.domain.dto;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.io.Serializable;


/**
 * 主题数据传输对象
 *
 * @author syc
 */
public class ThemeDTO implements Serializable {

    private static final long serialVersionUID = -944989543427061453L;

    /**
     * 主题名称
     */
    private String name;

    /**
     * 是否有设置项
     */
    private Boolean hasSetting;

    public ThemeDTO(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean getHasSetting() {
        return hasSetting;
    }

    public void setHasSetting(Boolean hasSetting) {
        this.hasSetting = hasSetting;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
    }
}
