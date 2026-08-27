package com.atlashub.auth.application.result;

import com.atlashub.auth.domain.valueobject.AuthStatus;
import com.atlashub.shared.api.UserQueryApi.UserSharedDto;
import com.atlashub.auth.domain.model.AuthAccount;

public record AuthenticatedUserDto(
    Long userId,
    String email,
    String firstName,
    String lastName,
    String phone,
    String country,
    boolean totpEnabled,
    AuthStatus status,
    String scope
) {
    public static AuthenticatedUserDto from(AuthAccount account, UserSharedDto profile) {
        return new AuthenticatedUserDto(
            profile.id(),
            profile.email(),
            profile.firstName(),
            profile.lastName(),
            profile.phone(),
            profile.country(),
            account.getTotpEnabled() != null && account.getTotpEnabled(),
            account.getStatus(),
            account.getScope()
        );
    }
}
