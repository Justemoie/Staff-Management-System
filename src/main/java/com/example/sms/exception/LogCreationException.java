package com.example.sms.exception;

public class LogCreationException extends RuntimeException {

    public LogCreationException(String message) {
        super(message);
    }

    public LogCreationException(String message, Throwable cause) {
        super(message, cause);
    }
}
