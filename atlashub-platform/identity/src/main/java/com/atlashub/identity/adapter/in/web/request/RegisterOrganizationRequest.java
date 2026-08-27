package com.atlashub.identity.adapter.in.web.request;

import com.atlashub.identity.domain.valueobject.BusinessType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record RegisterOrganizationRequest(
    @NotBlank(message = "Business name is required")
    String businessName,

    @NotNull(message = "Business type is required")
    BusinessType businessType
) {}
