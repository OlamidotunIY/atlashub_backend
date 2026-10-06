package com.atlashub.accounts.presentation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateProfileRequest(
        @NotBlank @Size(max = 100) String firstName,
        @NotBlank @Size(max = 100) String lastName,
        @Size(max = 32) String phone,
        @Size(max = 16) String locale,
        @Size(max = 64) String timezone
) {
}
