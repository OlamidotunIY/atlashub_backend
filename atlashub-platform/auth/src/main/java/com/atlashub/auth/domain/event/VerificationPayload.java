package com.atlashub.auth.domain.event;

import com.atlashub.auth.domain.valueobject.VerificationType;

public record VerificationPayload(
        String identifier,
        String value,
        VerificationType type,
        String rawCode
) {}

