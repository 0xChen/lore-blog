package com.developerchen.blog.exception;

import com.developerchen.core.exception.AlertException;

/**
 * Blog 自定义异常
 *
 * @author syc
 */
public class BlogException extends AlertException {
    private Integer code;

    public BlogException(Integer code, String message) {
        super(message);
        this.code = code;
    }

    public BlogException(String message) {
        super(message);
    }

    public Integer getCode() {
        return this.code;
    }
}
