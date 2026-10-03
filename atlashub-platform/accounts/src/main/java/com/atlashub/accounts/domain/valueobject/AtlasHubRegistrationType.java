package com.atlashub.accounts.domain.valueobject;

import com.atlashub.accounts.domain.exceptions.InvalidOrganizationException;

import java.util.Locale;

public enum AtlasHubRegistrationType {
    SOLE_PROPRIETORSHIP,
    PRIVATE_LIMITED_COMPANY;

    public static AtlasHubRegistrationType parse(String value) {
        if (value == null || value.isBlank()) {
            throw new InvalidOrganizationException("Registration type is required");
        }
        try {
            return valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            throw new InvalidOrganizationException("Unsupported AtlasHub registration type", exception);
        }
    }
}
