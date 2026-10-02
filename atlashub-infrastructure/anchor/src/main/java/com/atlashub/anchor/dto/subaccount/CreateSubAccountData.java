package com.atlashub.anchor.dto.subaccount;

import com.atlashub.anchor.dto.common.AnchorRelationship;

import java.util.Objects;

/** Request resource for an Anchor FBO-backed organization subaccount. */
public record CreateSubAccountData(
        String type,
        Attributes attributes,
        Relationships relationships
) {
    public static final String RESOURCE_TYPE = "SubAccount";

    public CreateSubAccountData(Attributes attributes, Relationships relationships) {
        this(RESOURCE_TYPE, attributes, relationships);
    }

    public CreateSubAccountData {
        if (!RESOURCE_TYPE.equals(type)) {
            throw new IllegalArgumentException("Subaccount resource type must be " + RESOURCE_TYPE);
        }
        Objects.requireNonNull(attributes, "Subaccount attributes are required");
        Objects.requireNonNull(relationships, "Subaccount relationships are required");
    }

    public record Attributes(boolean createVirtualNuban) {
    }

    public record Relationships(AnchorRelationship customer, AnchorRelationship parentAccount) {
        public Relationships {
            Objects.requireNonNull(customer, "Subaccount customer relationship is required");
            Objects.requireNonNull(parentAccount, "Subaccount parent-account relationship is required");
        }
    }
}
