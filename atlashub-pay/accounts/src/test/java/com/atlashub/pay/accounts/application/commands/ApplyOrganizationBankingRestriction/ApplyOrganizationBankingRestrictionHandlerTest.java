package com.atlashub.pay.accounts.application.commands.ApplyOrganizationBankingRestriction;

import com.atlashub.pay.accounts.domain.entities.BankingProviderRequest;
import com.atlashub.pay.accounts.domain.entities.BusinessDepositAccount;
import com.atlashub.pay.accounts.domain.entities.OrganizationBankingProfile;
import com.atlashub.pay.accounts.domain.repositories.BankingProviderRequestRepository;
import com.atlashub.pay.accounts.domain.repositories.BusinessDepositAccountRepository;
import com.atlashub.pay.accounts.domain.repositories.OrganizationBankingProfileRepository;
import com.atlashub.pay.accounts.domain.repositories.ReservedAccountRepository;
import com.atlashub.pay.accounts.domain.valueobject.BankingRestrictionType;
import com.atlashub.shared.application.security.ApiEnvironment;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ApplyOrganizationBankingRestrictionHandlerTest {
    @Test
    void restricts_live_banking_and_queues_a_freeze_request() {
        OrganizationBankingProfileRepository profiles = mock(OrganizationBankingProfileRepository.class);
        BusinessDepositAccountRepository deposits = mock(BusinessDepositAccountRepository.class);
        ReservedAccountRepository reserved = mock(ReservedAccountRepository.class);
        BankingProviderRequestRepository requests = mock(BankingProviderRequestRepository.class);
        OrganizationBankingProfile profile = OrganizationBankingProfile.create(1L, 10L, ApiEnvironment.LIVE, "customer");
        BusinessDepositAccount deposit = mock(BusinessDepositAccount.class);
        when(deposit.getId()).thenReturn(2L);
        when(deposit.getAnchorAccountId()).thenReturn("anchor-account");
        when(profiles.findByOrganizationIdAndEnvironment(10L, ApiEnvironment.LIVE)).thenReturn(Optional.of(profile));
        when(reserved.findByOrganizationIdAndEnvironment(10L, ApiEnvironment.LIVE)).thenReturn(List.of());
        when(deposits.findByOrganizationIdAndEnvironment(10L, ApiEnvironment.LIVE)).thenReturn(Optional.of(deposit));
        when(requests.nextIdentity()).thenReturn(3L);

        new ApplyOrganizationBankingRestrictionHandler(profiles, deposits, reserved, requests)
                .execute(new ApplyOrganizationBankingRestrictionCommand(
                        10L, true, "COMPLIANCE", "Compliance suspended", "event-1"));

        assertTrue(profile.getActiveRestrictions().contains(BankingRestrictionType.COMPLIANCE));
        ArgumentCaptor<BankingProviderRequest> request = ArgumentCaptor.forClass(BankingProviderRequest.class);
        verify(requests).save(request.capture());
        assertEquals(BankingProviderRequest.RequestType.FREEZE_DEPOSIT, request.getValue().getRequestType());
    }
}
