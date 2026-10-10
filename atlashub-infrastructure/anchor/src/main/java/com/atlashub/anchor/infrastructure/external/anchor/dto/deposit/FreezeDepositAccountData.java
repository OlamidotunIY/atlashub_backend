package com.atlashub.anchor.infrastructure.external.anchor.dto.deposit;

import java.util.Objects;

/** Anchor request resource for freezing a deposit account. */
public record FreezeDepositAccountData(String type, Attributes attributes) {
    public static final String RESOURCE_TYPE = "DepositAccount";

    public FreezeDepositAccountData(Attributes attributes) {
        this(RESOURCE_TYPE, attributes);
    }

    public FreezeDepositAccountData {
        if (!RESOURCE_TYPE.equals(type)) {
            throw new IllegalArgumentException("Freeze resource type must be " + RESOURCE_TYPE);
        }
        Objects.requireNonNull(attributes, "Freeze attributes are required");
    }

    public record Attributes(String freezeReason, String freezeDescription) {
        public Attributes {
            if (freezeReason == null || freezeReason.isBlank()) {
                throw new IllegalArgumentException("Freeze reason is required");
            }
        }
    }
}
