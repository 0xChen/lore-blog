package com.developerchen.blog.exception;

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
