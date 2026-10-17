package com.atlashub.logistics.shipping.domain.valueobject;

public record Address(
        String street,
        String city,
        String state,
        String country,
        String postalCode
) {
    public Address {
        if (state == null || state.isBlank()) {
            throw new IllegalArgumentException("State must not be blank");
        }
        if (city == null || city.isBlank()) {
            throw new IllegalArgumentException("City must not be blank");
        }
    }
}
