package com.developerchen.blog.module.site.domain.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;


/**
 * 主题数据传输对象
 *
 * @author syc
 */
@Data
public class ThemeDTO implements Serializable {

    @Serial
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
}
