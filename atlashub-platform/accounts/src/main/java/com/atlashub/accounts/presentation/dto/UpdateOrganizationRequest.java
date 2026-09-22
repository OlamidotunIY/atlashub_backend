package com.atlashub.accounts.presentation.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateOrganizationRequest(
        @NotBlank String businessName,
        String description,
        String logoUrl,
        String industry,
        String websiteUrl
) {
}
