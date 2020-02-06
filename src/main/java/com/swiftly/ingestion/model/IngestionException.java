package com.swiftly.ingestor.model;

public class IngestionException extends Exception {

    private final ErrorCode errorCode;
    private Throwable cause;
    private String msg;

    public IngestionException(ErrorCode errorCode, Throwable throwable, String msg) {
        super(msg, throwable);
        this.errorCode = errorCode;
        this.cause = throwable;
        this.msg = msg;
    }

    public IngestionException(ErrorCode errorCode, String msg) {
        super(errorCode.name());
        this.errorCode = errorCode;
        this.msg = msg;
    }
}
