package com.efkon.puconfigurationvalidator.ex;

public class WarningException extends RuntimeException {

    public WarningException() {
    }

    public WarningException(String message) {
        super(message);
    }

    public WarningException(String message, Throwable cause) {
        super(message, cause);
    }

    public WarningException(Throwable cause) {
        super(cause);
    }

    public WarningException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
