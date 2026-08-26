package com.atlashub.admin.domain.exception;

import com.atlashub.shared.exception.ErrorCode;

public enum AdminErrorCode implements ErrorCode {
    ADMIN_NOT_FOUND("ADM_001", "Admin not found", 404),
    ADMIN_USERNAME_ALREADY_EXISTS("ADM_002", "Admin username already exists", 409),
    MASTER_ADMIN_ALREADY_EXISTS("ADM_003", "Master admin already exists", 409),
    INSUFFICIENT_PERMISSIONS("ADM_004", "Insufficient permissions for this action", 403);

    private final String code;
    private final String defaultMessage;
    private final int statusCode;

    AdminErrorCode(String code, String defaultMessage, int statusCode) {
        this.code = code;
        this.defaultMessage = defaultMessage;
        this.statusCode = statusCode;
    }

    public String getCode() {
        return code;
    }

    public String getDefaultMessage() {
        return defaultMessage;
    }

    public int getStatusCode() {
        return statusCode;
    }
}
