package com.atlashub.authentication.presentation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.ZonedDateTime;

public record LogoutRequest(
        @NotBlank String refreshToken,
        @NotBlank String accessTokenJti,
        @NotNull ZonedDateTime accessTokenExpiresAt
) {
}
