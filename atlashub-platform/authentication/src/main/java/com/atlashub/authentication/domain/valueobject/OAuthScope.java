package com.atlashub.authentication.domain.valueobject;

import java.util.Arrays;
import java.util.Optional;

public enum OAuthScope {
    OPENID("openid"),
    PROFILE("profile"),
    EMAIL("email"),
    OFFLINE_ACCESS("offline_access");

    private final String value;

    OAuthScope(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static Optional<OAuthScope> fromValue(String value) {
        if (value == null || value.isBlank()) {
            return Optional.empty();
        }
        return Arrays.stream(values())
                .filter(scope -> scope.value.equalsIgnoreCase(value.trim()))
                .findFirst();
    }
}
