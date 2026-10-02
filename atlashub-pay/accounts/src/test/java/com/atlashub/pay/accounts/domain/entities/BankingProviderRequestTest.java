package com.atlashub.pay.accounts.domain.entities;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BankingProviderRequestTest {

    @Test
    void normalizes_the_api_environment_for_a_queued_provider_request() {
        BankingProviderRequest request = BankingProviderRequest.create(
                1L, BankingProviderRequest.RequestType.RESERVED_ACCOUNT, 2L, "request-1", "test",
                null, "subaccount-1", "ninepsb", "INDIVIDUAL", "customer-1", "Ada Doe", "ada@example.com", "12345678901"
        );

        assertEquals("TEST", request.getApiEnvironment());
    }

    @Test
    void rejects_an_unknown_api_environment() {
        assertThrows(IllegalArgumentException.class, () -> BankingProviderRequest.create(
                1L, BankingProviderRequest.RequestType.DEPOSIT, 2L, "request-1", "sandbox",
                "customer-1", null, null, null, null, null, null, null
        ));
    }
}
