package com.atlashub.shared.domain.exception;

/**
 * Base sealed class for all AtlasHub domain and application exceptions.
 *
 * <p>Every concrete exception must carry a machine-readable {@code ErrorCode}
 * so that a single {@code @ControllerAdvice} can map the hierarchy to HTTP responses.</p>
 */
public abstract class AtlasHubException extends RuntimeException {

    private final ErrorCode errorCode;

    protected AtlasHubException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    protected AtlasHubException(ErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    /**
     * Machine-readable error code string used by {@code @ControllerAdvice}.
     */
    public String getErrorCodeString() {
        return errorCode.name();
    }
    
    public ErrorCode getErrorCode() {
        return errorCode;
    }
}
