package com.swiftly.ingestor.model;

public enum ErrorCode {
    FILE_NOT_FOUND("File not found", ErrorCategory.INVALID_INPUT);
    
    final String message;
    final ErrorCategory category;

    ErrorCode(String message, ErrorCategory category) {
        this.message = message;
        this.category = category;
    }

    public String getMessage() {
        return message;
    }

    public ErrorCategory getCategory() {
        return category;
    }
}
