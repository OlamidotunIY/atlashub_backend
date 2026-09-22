package com.atlashub.authentication.presentation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ResetPasswordRequest(
        @NotBlank String email,
        @NotBlank String otp,
        @NotBlank @Size(min = 8) String newPassword
) {
}
