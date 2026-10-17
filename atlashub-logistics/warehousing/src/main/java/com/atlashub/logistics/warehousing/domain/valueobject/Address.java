package com.atlashub.logistics.warehousing.domain.valueobject;

import com.atlashub.shared.domain.exception.BusinessRuleException;

public record Address(
        String street,
        String city,
        String state,
        String country,
        String postalCode
) {
    public Address {
        if (street == null || street.isBlank()) {
            throw new BusinessRuleException("Street is required");
        }
        if (city == null || city.isBlank()) {
            throw new BusinessRuleException("City is required");
        }
        if (state == null || state.isBlank()) {
            throw new BusinessRuleException("State is required");
        }
        if (country == null || country.isBlank()) {
            throw new BusinessRuleException("Country is required");
        }
    }
}
