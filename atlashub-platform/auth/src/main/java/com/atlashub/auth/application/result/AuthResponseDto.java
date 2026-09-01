package com.atlashub.auth.application.result;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record AuthResponseDto(
        boolean requiresTwoFactor,
        boolean requiresPasswordChange,
        String identifier,
        String preAuthToken,
        AuthTokenDto tokens,
        String onboardingStatus
) {
    public static AuthResponseDto forTwoFactor(String preAuthToken) {
        return new AuthResponseDto(true, false, null, preAuthToken, null, null);
    }
    
    public static AuthResponseDto forPasswordChangeRequired(String identifier) {
        return new AuthResponseDto(false, true, identifier, null, null, null);
    }
    
    public static AuthResponseDto forSuccess(AuthTokenDto tokens) {
        return new AuthResponseDto(false, false, null, null, tokens, null);
    }

    public static AuthResponseDto forSuccess(AuthTokenDto tokens, String onboardingStatus) {
        return new AuthResponseDto(false, false, null, null, tokens, onboardingStatus);
    }
}
