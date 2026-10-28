package com.atlashub.shared.domain.valueobject;

import com.atlashub.shared.domain.exception.UnsupportedCountryException;

import java.util.Arrays;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

public record Country(String code) {
    private static final Set<String> SUPPORTED = Arrays.stream(Locale.getISOCountries())
            .collect(Collectors.toUnmodifiableSet());
    public Country {
        code = code == null ? null : code.trim().toUpperCase(Locale.ROOT);
        if (!SUPPORTED.contains(code))
            throw new UnsupportedCountryException("Invalid ISO country code");
    }
    public CurrencyCode deriveCurrency() {
        return switch (code) {
            case "NG" -> CurrencyCode.NGN;
            case "GH" -> CurrencyCode.GHS;
            case "KE" -> CurrencyCode.KES;
            case "ZA" -> CurrencyCode.ZAR;
            case "GB" -> CurrencyCode.GBP;
            case "US" -> CurrencyCode.USD;
            case "CI" -> CurrencyCode.XOF;
            case "EG" -> CurrencyCode.EGP;
            default -> throw new UnsupportedCountryException("Choose a reporting currency explicitly");
        };
    }
}
