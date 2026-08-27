package com.atlashub.auth.application.result;

public record PreAuthTokenDto(
        String preAuthToken,
        boolean requiresTwoFactor
) {}
