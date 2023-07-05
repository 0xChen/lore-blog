package com.developerchen.core.exception;

import java.io.Serial;

public class CryptoException extends RuntimeException {

    @Serial
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
