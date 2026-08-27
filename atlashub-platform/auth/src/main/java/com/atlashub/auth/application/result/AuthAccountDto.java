package com.atlashub.auth.application.result;

import com.atlashub.auth.domain.valueobject.AuthProvider;
import com.atlashub.auth.domain.valueobject.AuthStatus;
import com.atlashub.auth.domain.valueobject.PrincipalType;

public record AuthAccountDto(
    Long id,
    Long principalId,
    PrincipalType principalType,
    AuthProvider provider,
    String scope,
    boolean totpEnabled,
    AuthStatus status
) {}
