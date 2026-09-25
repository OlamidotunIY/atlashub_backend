package com.atlashub.shared.domain.exception;

import lombok.Getter;

@Getter
public class RateLimitExceededException extends ApplicationException {
    private final long retryAfterSeconds;
    private final long limit;
    public RateLimitExceededException(String message) {
        super(message);
        this.retryAfterSeconds = 0;
        this.limit = 0;
    }
    public RateLimitExceededException(String message, long retryAfterSeconds, long limit) {
        super(message);
        this.retryAfterSeconds = retryAfterSeconds;
        this.limit = limit;
    }
}

