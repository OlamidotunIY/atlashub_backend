package com.atlashub.pay.accounts.domain.valueobject;

public enum BankingProfileStatus {
    PENDING, PROVISIONING_DEPOSIT, PROVISIONING_SUBACCOUNT,
    PARTIALLY_PROVISIONED, ACTIVE, SUSPENDED, FAILED
}
