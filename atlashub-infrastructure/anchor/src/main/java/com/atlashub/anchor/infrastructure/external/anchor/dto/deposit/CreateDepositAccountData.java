package com.atlashub.anchor.infrastructure.external.anchor.dto.deposit;

import com.atlashub.anchor.infrastructure.external.anchor.dto.common.AnchorRelationship;

import java.util.Objects;

/** Request resource for Anchor's business-customer deposit-account endpoint. */
public record CreateDepositAccountData(
        String type,
        Attributes attributes,
        Relationships relationships
) {
    public static final String RESOURCE_TYPE = "DepositAccount";

    public CreateDepositAccountData(Attributes attributes, Relationships relationships) {
        this(RESOURCE_TYPE, attributes, relationships);
    }

    public CreateDepositAccountData {
        if (!RESOURCE_TYPE.equals(type)) {
            throw new IllegalArgumentException("Deposit-account resource type must be " + RESOURCE_TYPE);
        }
        Objects.requireNonNull(attributes, "Deposit-account attributes are required");
        Objects.requireNonNull(relationships, "Deposit-account relationships are required");
    }

    public record Attributes(String productName) {
        public Attributes {
            if (productName == null || productName.isBlank()) {
                throw new IllegalArgumentException("Deposit-account product name is required");
            }
        }
    }

    public record Relationships(AnchorRelationship customer) {
        public Relationships {
            Objects.requireNonNull(customer, "Deposit-account customer relationship is required");
        }
    }
}
