package com.atlashub.identity.adapter.in.web.request;

import com.atlashub.identity.domain.valueobject.BusinessSize;
import com.atlashub.identity.domain.valueobject.BusinessType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.URL;

public record RegisterOrganizationRequest(
        @NotBlank(message = "Business name is required")
        String businessName,

        @NotNull(message = "Business type is required")
        BusinessType businessType,

        @NotNull(message = "Business size is required")
        BusinessSize businessSize,

        @URL(message = "Logo URL must be valid")
        String logoUrl
) {
}
