package com.atlashub.shared.domain.exception;

/**
 * Base sealed class for all AtlasHub domain and application exceptions.
 *
 * <p>Every concrete exception must carry a machine-readable {@code ErrorCode}
 * so that a single {@code @ControllerAdvice} can map the hierarchy to HTTP responses.</p>
 */
public abstract class AtlasHubException extends RuntimeException {
    protected AtlasHubException(String message) {
        super(message);
    }
    protected AtlasHubException(String message, Throwable cause) {
        super(message, cause);
    }

    public String getErrorCodeString() {
        // Returns exactly the class name, e.g., "UnsupportedCountryException"
        return this.getClass().getSimpleName();
    }
}
