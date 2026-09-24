package com.atlashub.accounts.presentation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateOrganizationRequest(
        @NotBlank @Size(max = 150) String businessName,
        @Size(max = 1000) String description,
        @Size(max = 2048) String logoUrl,
        @Size(max = 100) String industry,
        @Size(max = 2048) String websiteUrl
) {
}
