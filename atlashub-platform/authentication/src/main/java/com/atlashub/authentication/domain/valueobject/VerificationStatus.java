package com.atlashub.authentication.domain.valueobject;

public enum VerificationStatus {
    pending,
    verified,
    expired,
    max_attempts_exceeded
}
