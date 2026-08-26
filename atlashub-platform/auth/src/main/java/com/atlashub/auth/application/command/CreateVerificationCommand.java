package com.atlashub.auth.application.command;

import com.atlashub.auth.domain.model.VerificationType;

public record CreateVerificationCommand(
        Long authAccountId,
        String identifier,
        VerificationType type
) {}
