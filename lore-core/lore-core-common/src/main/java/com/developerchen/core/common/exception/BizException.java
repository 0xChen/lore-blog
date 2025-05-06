package com.developerchen.core.common.exception;

/**
 * 业务异常
 *
 * @author syc
 */
public class BizException extends RuntimeException {
    private static final long serialVersionUID = 5492734612646025775L;

    private String message;
    private int code = 500;

    public BizException(String message) {
        super(message);
        this.message = message;
    }

    public BizException(String message, Throwable e) {
        super(message, e);
        this.message = message;
    }

    public BizException(String message, int code) {
        super(message);
        this.message = message;
        this.code = code;
    }

    public BizException(String message, int code, Throwable e) {
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
