package com.atlashub.auth.application.dto;

import com.atlashub.auth.domain.model.AuthProvider;
import com.atlashub.auth.domain.model.AuthStatus;
import com.atlashub.auth.domain.model.PrincipalType;

public record AuthAccountDto(
    Long id,
    Long principalId,
    PrincipalType principalType,
    AuthProvider provider,
    String scope,
    boolean totpEnabled,
    AuthStatus status
) {}
