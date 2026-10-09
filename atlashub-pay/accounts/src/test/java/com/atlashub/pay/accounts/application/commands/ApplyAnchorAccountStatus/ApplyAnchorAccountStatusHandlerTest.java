package com.atlashub.pay.accounts.application.commands.ApplyAnchorAccountStatus;

import com.atlashub.pay.accounts.domain.entities.*;
import com.atlashub.pay.accounts.domain.repositories.*;
import com.atlashub.pay.accounts.domain.valueobject.ConfirmedBankingDetails;
import com.atlashub.shared.application.security.ApiEnvironment;
import com.atlashub.shared.domain.valueobject.CurrencyCode;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ApplyAnchorAccountStatusHandlerTest {
    @Test
    void active_deposit_activates_banking_without_creating_an_fbo_subaccount() {
        BusinessDepositAccountRepository deposits = mock(BusinessDepositAccountRepository.class);
        BusinessSubAccountRepository subAccounts = mock(BusinessSubAccountRepository.class);
        ReservedAccountRepository reserved = mock(ReservedAccountRepository.class);
        OrganizationBankingProfileRepository profiles = mock(OrganizationBankingProfileRepository.class);
        BankingProviderRequestRepository requests = mock(BankingProviderRequestRepository.class);
        OrganizationBankingProfile profile = OrganizationBankingProfile.create(
                1L, 10L, ApiEnvironment.LIVE, "customer");
        BusinessDepositAccount deposit = BusinessDepositAccount.request(
                2L, 10L, ApiEnvironment.LIVE, 1L, "customer", CurrencyCode.NGN);
        deposit.markSubmitted("anchor-deposit");
        profile.linkDepositAccount(2L);
        when(deposits.findByAnchorAccountIdAndEnvironment("anchor-deposit", ApiEnvironment.LIVE))
                .thenReturn(Optional.of(deposit));
        when(profiles.findById(1L)).thenReturn(Optional.of(profile));
        ApplyAnchorAccountStatusHandler handler = new ApplyAnchorAccountStatusHandler(
                deposits, subAccounts, reserved, profiles, requests);
        ApplyAnchorAccountStatusCommand command = new ApplyAnchorAccountStatusCommand(
                "DEPOSIT_ACCOUNT", "LIVE", "anchor-deposit", "ACTIVE",
                new ConfirmedBankingDetails("Tolu Store", "1234567890", "******7890", "Anchor", "090000"), null);

        handler.execute(command);

        verify(subAccounts, never()).save(any());
        verify(requests, never()).save(any(BankingProviderRequest.class));
        verify(profiles).save(profile);
    }
}
