package com.atlashub.admin.domain.exception;

import com.atlashub.shared.domain.exception.ErrorCode;

public enum AdminErrorCode implements ErrorCode {
    ADMIN_NOT_FOUND("ADM_001", "Admin not found", 404),
    ADMIN_USERNAME_ALREADY_EXISTS("ADM_002", "Admin username already exists", 409),
    MASTER_ADMIN_ALREADY_EXISTS("ADM_003", "Master admin already exists", 409),
    INSUFFICIENT_PERMISSIONS("ADM_004", "Insufficient permissions for this action", 403),
    ADMIN_ALREADY_SUSPENDED("ADM_005", "Admin is already suspended", 409),
    CANNOT_SUSPEND_MASTER_ADMIN("ADM_006", "Master admin cannot be suspended", 400),
    ADMIN_ALREADY_ACTIVE("ADM_007", "Admin is already active", 409);

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
