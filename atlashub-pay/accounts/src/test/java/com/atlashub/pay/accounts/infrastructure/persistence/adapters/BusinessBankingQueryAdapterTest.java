package com.atlashub.pay.accounts.infrastructure.persistence.adapters;

import com.atlashub.pay.accounts.domain.entities.BusinessDepositAccount;
import com.atlashub.pay.accounts.domain.repositories.BusinessDepositAccountRepository;
import com.atlashub.shared.application.security.ApiEnvironment;
import com.atlashub.shared.domain.valueobject.CurrencyCode;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class BusinessBankingQueryAdapterTest {
    @Test
    void returnsEmptyWhenNoOperatingAccountExists() {
        BusinessDepositAccountRepository repository = mock(BusinessDepositAccountRepository.class);
        when(repository.findByOrganizationIdAndEnvironment(1L, ApiEnvironment.TEST)).thenReturn(Optional.empty());

        assertTrue(new BusinessBankingQueryAdapter(repository)
                .findOperatingAccount(1L, ApiEnvironment.TEST).isEmpty());
    }

    @Test
    void doesNotExposeDepositAccountBeforeProviderActivation() {
        BusinessDepositAccountRepository repository = mock(BusinessDepositAccountRepository.class);
        BusinessDepositAccount pending = BusinessDepositAccount.request(
                10L, 1L, ApiEnvironment.LIVE, 20L, "anchor-customer", CurrencyCode.NGN);
        when(repository.findByOrganizationIdAndEnvironment(1L, ApiEnvironment.LIVE))
                .thenReturn(Optional.of(pending));

        assertTrue(new BusinessBankingQueryAdapter(repository)
                .findOperatingAccount(1L, ApiEnvironment.LIVE).isEmpty());
    }
}
