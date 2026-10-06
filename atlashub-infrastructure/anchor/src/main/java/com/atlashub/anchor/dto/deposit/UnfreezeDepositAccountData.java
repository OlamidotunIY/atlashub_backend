package com.atlashub.anchor.dto.deposit;

/** Anchor request resource for unfreezing a deposit account. */
public record UnfreezeDepositAccountData(String id, String type) {
    public static final String RESOURCE_TYPE = "DepositAccount";

    public UnfreezeDepositAccountData(String id) {
        this(id, RESOURCE_TYPE);
    }

    public UnfreezeDepositAccountData {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Deposit-account ID is required");
        }
        if (!RESOURCE_TYPE.equals(type)) {
            throw new IllegalArgumentException("Unfreeze resource type must be " + RESOURCE_TYPE);
        }
    }
}
