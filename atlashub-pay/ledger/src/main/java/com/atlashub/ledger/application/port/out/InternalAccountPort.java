package com.atlashub.ledger.application.port.out;

public interface InternalAccountPort {
    Long getAccountId(Long organizationId, String accountType);
}