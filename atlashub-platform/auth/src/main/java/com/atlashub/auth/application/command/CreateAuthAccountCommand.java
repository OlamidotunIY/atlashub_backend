package com.atlashub.auth.application.command;

import com.atlashub.auth.domain.model.AuthProvider;
import com.atlashub.auth.domain.model.AuthStatus;
import com.atlashub.auth.domain.model.PrincipalType;

public record CreateAuthAccountCommand(
        Long principalId,
        PrincipalType principalType,
        String identifier,
        String secondaryIdentifier,
        AuthProvider provider,
        String rawCredential,
        String scope,
        AuthStatus initialStatus
) {}
