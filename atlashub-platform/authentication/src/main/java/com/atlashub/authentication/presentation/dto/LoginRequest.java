package com.atlashub.authentication.presentation.dto;

import com.atlashub.shared.application.security.ApiEnvironment;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record LoginRequest(
        @NotBlank @Email String email,
        @NotBlank String password,
        @NotNull ApiEnvironment environment,
        @NotBlank String deviceFingerprint
) {
}
