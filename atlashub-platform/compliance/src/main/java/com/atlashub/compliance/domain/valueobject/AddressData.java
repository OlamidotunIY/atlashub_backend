package com.atlashub.compliance.domain.valueobject;

import com.atlashub.compliance.domain.exception.InvalidComplianceDataException;

public record AddressData(
    String addressLine1,
    String addressLine2,
    String city,
    String state,
    String postalCode,
    String country
) {
    public AddressData {
        requireText(addressLine1, "Address line 1");
        requireText(city, "City");
        requireText(state, "State");
        requireText(postalCode, "Postal code");
        requireText(country, "Country");
    }

    private static void requireText(String value, String name) {
        if (value == null || value.isBlank()) throw new InvalidComplianceDataException(name + " is required");
    }
}
