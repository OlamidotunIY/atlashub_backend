package com.atlashub.accounts.presentation.dto;

import com.atlashub.accounts.domain.valueobject.AtlasHubRegistrationType;
import com.atlashub.accounts.domain.valueobject.SupportedIndustry;
import com.atlashub.shared.domain.valueobject.CurrencyCode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateOrganizationRequest(
        @NotBlank @Size(max = 150) String businessName,
        @NotNull AtlasHubRegistrationType registrationType,
        @NotNull SupportedIndustry industry,
        @Size(max = 1000) String description,
        @Size(max = 2048) String logoUrl,
        @Size(max = 2048) String websiteUrl,
        @NotBlank @Size(min = 2, max = 2) String country,
        @NotNull CurrencyCode baseCurrency) {}
