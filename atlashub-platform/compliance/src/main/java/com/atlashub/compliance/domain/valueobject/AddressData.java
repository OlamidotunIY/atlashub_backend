package com.atlashub.compliance.domain.valueobject;

import com.atlashub.shared.domain.exception.ValidationException;

public record AddressData(
    String street,
    String city,
    String state,
    String country
) {
    public AddressData {
        if (street == null || street.isBlank()) {
            throw new ValidationException("street cannot be blank");
        }
        if (city == null || city.isBlank()) {
            throw new ValidationException("city cannot be blank");
        }
        if (state == null || state.isBlank()) {
            throw new ValidationException("state cannot be blank");
        }
        if (country == null || country.isBlank()) {
            throw new ValidationException("country cannot be blank");
        }
    }
}
