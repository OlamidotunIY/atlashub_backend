package com.atlashub.accounts.presentation.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank @Size(max = 100) String firstName,
        @NotBlank @Size(max = 100) String lastName,
        @NotBlank @Email String email,
        @NotBlank @Size(min = 8) String password,
        @NotBlank @Size(min = 2, max = 2) String country,
        @NotBlank @Size(max = 150) String businessName,
        @NotNull String businessType,
        @NotNull String businessSize,
        @Size(max = 100) String industry,
        @Size(max = 1000) String description,
        @Size(max = 2048) String logoUrl,
        @Size(max = 2048) String websiteUrl
) {
}
