package com.atlashub.authentication.presentation.dto;

import jakarta.validation.constraints.NotBlank;

public record VerifyEmailRequest(
        @NotBlank String email,
        @NotBlank String otp
) {
}
