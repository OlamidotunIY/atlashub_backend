package com.atlashub.auth.application.dto;

public record PreAuthTokenDto(
        String preAuthToken,
        boolean requiresTwoFactor
) {}
