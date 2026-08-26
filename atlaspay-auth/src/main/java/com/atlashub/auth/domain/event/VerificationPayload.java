package com.atlashub.auth.domain.event;

import com.atlashub.auth.domain.model.VerificationType;

public record VerificationPayload(
        String identifier,
        String value,
        VerificationType type,
        String rawCode
) {}
