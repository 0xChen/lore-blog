package com.developerchen.core.exception;

public class CryptoException extends RuntimeException {

    private static final long serialVersionUID = 5495846748852573973L;

    public CryptoException(String message) {
        super(message);
    }

    public CryptoException(String message, Throwable cause) {
        super(message, cause);
    }

    public CryptoException(Throwable cause) {
        super(cause);
    }
}
