package com.atlashub.shared.application.security;

import java.util.Locale;

/**
 * AtlasHub execution boundary. Provider resources, money records, and idempotency
 * keys must never cross this boundary.
 */
public enum ApiEnvironment {
    TEST,
    LIVE;

    public static ApiEnvironment parse(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("API environment is required");
        }
        try {
            return valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("API environment must be TEST or LIVE", exception);
        }
    }
}
