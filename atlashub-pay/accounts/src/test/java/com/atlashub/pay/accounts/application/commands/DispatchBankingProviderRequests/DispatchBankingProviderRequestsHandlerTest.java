package com.atlashub.pay.accounts.application.commands.DispatchBankingProviderRequests;

import com.atlashub.pay.accounts.domain.entities.BankingProviderRequest;
import com.atlashub.pay.accounts.domain.entities.BusinessDepositAccount;
import com.atlashub.pay.accounts.domain.entities.OrganizationBankingProfile;
import com.atlashub.shared.application.port.AnchorBankingPort;
import com.atlashub.pay.accounts.domain.repositories.*;
import com.atlashub.pay.accounts.domain.valueobject.RequestStatus;
import com.atlashub.pay.accounts.domain.valueobject.RequestType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.mockito.Mockito.*;

class DispatchBankingProviderRequestsHandlerTest {
    @Test
    void dispatches_unfreeze_and_restores_the_unrestricted_profile() {
        BankingProviderRequestRepository requests = mock(BankingProviderRequestRepository.class);
        BusinessDepositAccountRepository deposits = mock(BusinessDepositAccountRepository.class);
        BusinessSubAccountRepository subAccounts = mock(BusinessSubAccountRepository.class);
        ReservedAccountRepository reserved = mock(ReservedAccountRepository.class);
        OrganizationBankingProfileRepository profiles = mock(OrganizationBankingProfileRepository.class);
        AnchorBankingPort anchor = mock(AnchorBankingPort.class);
        BankingProviderRequest request = BankingProviderRequest.createDepositLifecycle(
                1L, RequestType.UNFREEZE_DEPOSIT, 2L,
                "unfreeze-1", "LIVE", "Compliance reinstated");
        BusinessDepositAccount account = mock(BusinessDepositAccount.class);
        OrganizationBankingProfile profile = mock(OrganizationBankingProfile.class);
        when(requests.findPending(10)).thenReturn(java.util.List.of(request));
        when(deposits.findById(2L)).thenReturn(Optional.of(account));
        when(account.getAnchorAccountId()).thenReturn("anchor-account");
        when(account.getBankingProfileId()).thenReturn(3L);
        when(profiles.findById(3L)).thenReturn(Optional.of(profile));
        when(profile.getActiveRestrictions()).thenReturn(java.util.Set.of());

        new DispatchBankingProviderRequestsHandler(requests, deposits, subAccounts, reserved, profiles, anchor)
                .execute(new DispatchBankingProviderRequestsCommand(10));

        verify(anchor).unfreezeDepositAccount("anchor-account", "LIVE");
        verify(account).reactivate();
        verify(profile).reactivate();
        Assertions.assertEquals(RequestStatus.COMPLETED, request.getStatus());
    }
}
