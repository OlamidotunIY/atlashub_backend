package com.atlashub.accounts.presentation.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank String firstName,
        @NotBlank String lastName,
        @NotBlank @Email String email,
        @NotBlank @Size(min = 8) String password,
        @NotBlank String country,
        @NotBlank String businessName,
        @NotNull String businessType,
        @NotNull String businessSize,
        String industry,
        String description,
        String logoUrl,
        String websiteUrl
) {
}
