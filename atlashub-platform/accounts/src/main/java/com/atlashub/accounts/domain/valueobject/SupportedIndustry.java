package com.atlashub.accounts.domain.valueobject;

import com.atlashub.accounts.domain.exceptions.InvalidOrganizationException;

import java.util.Locale;

public enum SupportedIndustry {
    PHYSICAL_GOODS,
    DIGITAL_SERVICES,
    PHYSICAL_SERVICES,
    PROFESSIONAL_SERVICES,
    HOTELS,
    RESTAURANTS,
    COURIER_SERVICES,
    FREIGHT_SERVICES,
    RETAIL,
    WHOLESALE;

    public static SupportedIndustry parse(String value) {
        if (value == null || value.isBlank()) {
            throw new InvalidOrganizationException("Industry is required");
        }
        try {
            return valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            throw new InvalidOrganizationException("Unsupported AtlasHub industry", exception);
        }
    }
}
