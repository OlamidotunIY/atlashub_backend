package com.atlashub.anchor.dto.reservedaccount;

import com.atlashub.anchor.dto.common.AnchorRelationship;

import java.util.Objects;

/**
 * Request resource for an Anchor reserved account with an individual customer identity.
 * Business-customer support will use a separate, provider-confirmed request resource.
 */
public record CreateReservedAccountData(
        String type,
        Attributes attributes,
        Relationships relationships
) {
    public static final String RESOURCE_TYPE = "ReservedAccount";

    public CreateReservedAccountData(Attributes attributes, Relationships relationships) {
        this(RESOURCE_TYPE, attributes, relationships);
    }

    public CreateReservedAccountData {
        if (!RESOURCE_TYPE.equals(type)) {
            throw new IllegalArgumentException("Reserved-account resource type must be " + RESOURCE_TYPE);
        }
        Objects.requireNonNull(attributes, "Reserved-account attributes are required");
        Objects.requireNonNull(relationships, "Reserved-account relationships are required");
    }

    public record Attributes(String provider, Customer customer) {
        public Attributes {
            if (provider == null || provider.isBlank()) {
                throw new IllegalArgumentException("Reserved-account provider is required");
            }
        }
    }

    public record Customer(IndividualCustomer individualCustomer) {
        public Customer {
            Objects.requireNonNull(individualCustomer, "Individual customer is required");
        }
    }

    public record IndividualCustomer(FullName fullName, String email, String bvn) {
        public IndividualCustomer {
            Objects.requireNonNull(fullName, "Individual customer full name is required");
            if (email == null || email.isBlank()) {
                throw new IllegalArgumentException("Individual customer email is required");
            }
            if (bvn == null || bvn.isBlank()) {
                throw new IllegalArgumentException("Individual customer BVN is required");
            }
        }
    }

    public record FullName(String firstName, String lastName) {
        public FullName {
            if (firstName == null || firstName.isBlank() || lastName == null || lastName.isBlank()) {
                throw new IllegalArgumentException("Individual customer first and last names are required");
            }
        }
    }

    public record Relationships(AnchorRelationship payoutAccount, AnchorRelationship customer) {
        public Relationships(AnchorRelationship payoutAccount) {
            this(payoutAccount, null);
        }
        public Relationships {
            Objects.requireNonNull(payoutAccount, "Reserved-account payout-account relationship is required");
        }
    }
}
