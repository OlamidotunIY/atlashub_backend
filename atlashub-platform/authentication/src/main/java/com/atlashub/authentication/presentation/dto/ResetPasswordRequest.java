package com.atlashub.authentication.presentation.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ResetPasswordRequest(
        @NotBlank @Email String email,
        @NotBlank @Size(min = 4, max = 12) String otp,
        @NotBlank @Size(min = 8) String newPassword
) {
}
