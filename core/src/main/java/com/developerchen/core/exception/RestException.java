package com.developerchen.core.exception;

/**
 * 自定义异常
 *
 * @author syc
 */
public class RestException extends RuntimeException {
    private static final long serialVersionUID = 5492734612646025775L;

    private String message;
    private int code = 500;

    public RestException(String message) {
        super(message);
        this.message = message;
    }

    public RestException(String message, Throwable e) {
        super(message, e);
        this.message = message;
    }

    public RestException(String message, int code) {
        super(message);
        this.message = message;
        this.code = code;
    }

    public RestException(String message, int code, Throwable e) {
        super(message, e);
        this.message = message;
        this.code = code;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

}
