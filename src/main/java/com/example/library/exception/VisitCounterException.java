package com.example.library.exception;

public class VisitCounterException extends RuntimeException {
    public VisitCounterException(String message) {
        super(message);
    }

    public VisitCounterException(String message, Throwable cause) {
        super(message, cause);
    }
}