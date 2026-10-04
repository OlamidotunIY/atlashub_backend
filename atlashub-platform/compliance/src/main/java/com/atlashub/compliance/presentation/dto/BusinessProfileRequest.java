package com.atlashub.compliance.presentation.dto;

import jakarta.validation.constraints.NotBlank;
public record BusinessProfileRequest(
        @NotBlank String businessRegistrationNumber,
        @NotBlank String businessBvn,
        @NotBlank String businessDescription,
        String website
) {
}
