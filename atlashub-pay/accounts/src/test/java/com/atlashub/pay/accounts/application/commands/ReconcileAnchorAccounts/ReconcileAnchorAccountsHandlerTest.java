package com.atlashub.pay.accounts.application.commands.ReconcileAnchorAccounts;

import com.atlashub.pay.accounts.application.commands.ApplyAnchorAccountStatus.ApplyAnchorAccountStatusCommand;
import com.atlashub.pay.accounts.application.commands.ApplyAnchorAccountStatus.ApplyAnchorAccountStatusHandler;
import com.atlashub.pay.accounts.domain.entities.BusinessDepositAccount;
import com.atlashub.pay.accounts.domain.ports.AnchorBankingPort;
import com.atlashub.pay.accounts.domain.repositories.BusinessDepositAccountRepository;
import com.atlashub.pay.accounts.domain.repositories.BusinessSubAccountRepository;
import com.atlashub.pay.accounts.domain.repositories.ReservedAccountRepository;
import com.atlashub.shared.application.security.ApiEnvironment;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class ReconcileAnchorAccountsHandlerTest {
    @Test
    void applies_authoritative_active_deposit_status() {
        BusinessDepositAccountRepository deposits = mock(BusinessDepositAccountRepository.class);
        BusinessSubAccountRepository subAccounts = mock(BusinessSubAccountRepository.class);
        ReservedAccountRepository reserved = mock(ReservedAccountRepository.class);
        AnchorBankingPort anchor = mock(AnchorBankingPort.class);
        ApplyAnchorAccountStatusHandler apply = mock(ApplyAnchorAccountStatusHandler.class);
        BusinessDepositAccount account = mock(BusinessDepositAccount.class);
        when(account.getAnchorAccountId()).thenReturn("anchor-account");
        when(account.getEnvironment()).thenReturn(ApiEnvironment.TEST);
        when(deposits.findPendingReconciliation(20)).thenReturn(List.of(account));
        when(subAccounts.findPendingReconciliation(20)).thenReturn(List.of());
        when(reserved.findPendingReconciliation(20)).thenReturn(List.of());
        when(anchor.fetchDepositAccount("anchor-account", "TEST")).thenReturn(
                new AnchorBankingPort.AccountDetails("anchor-account", "Tolu Store", "1234567890",
                        "******7890", "Anchor Bank", "090000", "NGN", "ACTIVE"));

        new ReconcileAnchorAccountsHandler(deposits, subAccounts, reserved, anchor, apply)
                .execute(new ReconcileAnchorAccountsCommand(20));

        ArgumentCaptor<ApplyAnchorAccountStatusCommand> command =
                ArgumentCaptor.forClass(ApplyAnchorAccountStatusCommand.class);
        verify(apply).execute(command.capture());
        assertEquals("TEST", command.getValue().environment());
        assertEquals("DEPOSIT_ACCOUNT", command.getValue().resourceType());
    }
}
