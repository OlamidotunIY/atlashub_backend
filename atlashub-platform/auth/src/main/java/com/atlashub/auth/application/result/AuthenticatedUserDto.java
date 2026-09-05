package com.atlashub.auth.application.result;

import com.atlashub.auth.domain.model.AuthAccount;
import com.atlashub.auth.domain.valueobject.AuthStatus;
import com.atlashub.identity.application.result.UserDto;

public record AuthenticatedUserDto(
        Long authAccountId,
        Long userId,
        String firstName,
        String lastName,
        String email,
        String phone,
        String country,
        String status,
        boolean isEmailVerified,
        boolean totpEnabled,
        String onboardingStatus
) {
    public static AuthenticatedUserDto from(AuthAccount authAccount, UserDto user, String onboardingStatus) {
        return new AuthenticatedUserDto(
                authAccount.getId(),
                authAccount.getPrincipalId(),
                user.firstName(),
                user.lastName(),
                user.email(),
                user.phone(),
                user.country(),
                authAccount.getStatus().name(),
                authAccount.getStatus() != AuthStatus.PENDING_EMAIL_VERIFICATION,
                authAccount.getTotpEnabled() != null && authAccount.getTotpEnabled(),
                onboardingStatus
        );
    }
}
