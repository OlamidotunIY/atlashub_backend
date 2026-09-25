package com.atlashub.shared.domain.valueobject;

import com.atlashub.shared.domain.exception.UnsupportedCountryException;

import java.util.Set;

public record Country(String code) {
    private static final Set<String> SUPPORTED = Set.of("NG");
    public Country {
        if (!SUPPORTED.contains(code))
            throw new UnsupportedCountryException("UnSupported Country");
    }
    public CurrencyCode deriveCurrency() {
        return switch (code) {
            case "NG" -> CurrencyCode.NGN;
            default -> throw new UnsupportedCountryException("UnSupported Country");
        };
    }
}
