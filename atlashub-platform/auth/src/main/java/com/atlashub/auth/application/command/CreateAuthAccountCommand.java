package com.atlashub.auth.application.command;


import com.atlashub.auth.domain.valueobject.AuthProvider;
import com.atlashub.auth.domain.valueobject.AuthStatus;
import com.atlashub.auth.domain.valueobject.PrincipalType;

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

