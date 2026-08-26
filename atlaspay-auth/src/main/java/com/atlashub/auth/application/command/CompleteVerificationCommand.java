package com.atlashub.auth.application.command;

import com.atlashub.auth.domain.model.VerificationType;

public record CompleteVerificationCommand(
        VerificationType type,
        String identifier,
        String code
) {}
