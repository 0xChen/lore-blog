package com.developerchen.blog.exception;

/**
 * Blog 自定义异常
 *
 * @author syc
 */
public class BlogException extends RuntimeException {
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
