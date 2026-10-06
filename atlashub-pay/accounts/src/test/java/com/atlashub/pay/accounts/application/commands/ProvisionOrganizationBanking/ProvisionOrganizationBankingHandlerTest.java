package com.atlashub.pay.accounts.application.commands.ProvisionOrganizationBanking;

import com.atlashub.pay.accounts.domain.entities.BankingProviderRequest;
import com.atlashub.pay.accounts.domain.entities.BusinessDepositAccount;
import com.atlashub.pay.accounts.domain.entities.OrganizationBankingProfile;
import com.atlashub.pay.accounts.domain.repositories.BankingProviderRequestRepository;
import com.atlashub.pay.accounts.domain.repositories.BusinessDepositAccountRepository;
import com.atlashub.pay.accounts.domain.repositories.OrganizationBankingProfileRepository;
import com.atlashub.shared.application.security.ApiEnvironment;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ProvisionOrganizationBankingHandlerTest {
    @Test
    void provisions_a_test_profile_with_an_environment_scoped_request() {
        OrganizationBankingProfileRepository profiles = mock(OrganizationBankingProfileRepository.class);
        BusinessDepositAccountRepository deposits = mock(BusinessDepositAccountRepository.class);
        BankingProviderRequestRepository requests = mock(BankingProviderRequestRepository.class);
        when(profiles.findByOrganizationIdAndEnvironment(10L, ApiEnvironment.TEST)).thenReturn(Optional.empty());
        when(deposits.findByOrganizationIdAndEnvironment(10L, ApiEnvironment.TEST)).thenReturn(Optional.empty());
        when(profiles.nextIdentity()).thenReturn(1L);
        when(deposits.nextIdentity()).thenReturn(2L);
        when(requests.nextIdentity()).thenReturn(3L);
        when(profiles.save(any())).thenAnswer(call -> call.getArgument(0));
        when(deposits.save(any())).thenAnswer(call -> call.getArgument(0));

        new ProvisionOrganizationBankingHandler(profiles, deposits, requests)
                .execute(new ProvisionOrganizationBankingCommand(10L, "sandbox-customer", "TEST"));

        ArgumentCaptor<BankingProviderRequest> request = ArgumentCaptor.forClass(BankingProviderRequest.class);
        verify(requests).save(request.capture());
        assertEquals("TEST", request.getValue().getApiEnvironment());
        assertEquals("sandbox-customer", request.getValue().getAnchorCustomerId());
    }

    @Test
    void does_not_duplicate_an_existing_provider_request() {
        OrganizationBankingProfileRepository profiles = mock(OrganizationBankingProfileRepository.class);
        BusinessDepositAccountRepository deposits = mock(BusinessDepositAccountRepository.class);
        BankingProviderRequestRepository requests = mock(BankingProviderRequestRepository.class);
        OrganizationBankingProfile profile = OrganizationBankingProfile.create(1L, 10L, ApiEnvironment.LIVE, "live-customer");
        BusinessDepositAccount deposit = mock(BusinessDepositAccount.class);
        when(deposit.getId()).thenReturn(2L);
        when(profiles.findByOrganizationIdAndEnvironment(10L, ApiEnvironment.LIVE)).thenReturn(Optional.of(profile));
        when(deposits.findByOrganizationIdAndEnvironment(10L, ApiEnvironment.LIVE)).thenReturn(Optional.of(deposit));
        when(requests.findByRequestReferenceAndApiEnvironment("org-banking-deposit-10-live", "LIVE"))
                .thenReturn(Optional.of(mock(BankingProviderRequest.class)));

        new ProvisionOrganizationBankingHandler(profiles, deposits, requests)
                .execute(new ProvisionOrganizationBankingCommand(10L, "live-customer", "LIVE"));

        verify(requests, never()).save(any());
    }
}
