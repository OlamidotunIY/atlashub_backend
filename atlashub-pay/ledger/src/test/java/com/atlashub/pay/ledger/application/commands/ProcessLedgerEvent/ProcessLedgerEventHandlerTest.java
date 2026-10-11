package com.atlashub.pay.ledger.application.commands.ProcessLedgerEvent;

import com.atlashub.pay.ledger.application.commands.CreateLedgerAccount.CreateLedgerAccountCommand;
import com.atlashub.pay.ledger.application.commands.CreateLedgerAccount.CreateLedgerAccountHandler;
import com.atlashub.pay.ledger.application.commands.PostLedgerTransaction.PostLedgerTransactionCommand;
import com.atlashub.pay.ledger.application.commands.PostLedgerTransaction.PostLedgerTransactionHandler;
import com.atlashub.pay.ledger.domain.entities.LedgerAccount;
import com.atlashub.pay.ledger.domain.repositories.LedgerAccountRepository;
import com.atlashub.pay.ledger.domain.valueobject.LedgerAccountType;
import com.atlashub.pay.ledger.domain.valueobject.NormalBalance;
import com.atlashub.shared.application.security.ApiEnvironment;
import com.atlashub.shared.domain.valueobject.CurrencyCode;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ProcessLedgerEventHandlerTest {
    @Test
    void registrationCreatesOnlyThreeFoundationAccounts() {
        var create = mock(CreateLedgerAccountHandler.class);
        var post = mock(PostLedgerTransactionHandler.class);
        var accounts = mock(LedgerAccountRepository.class);
        when(accounts.findByOrganizationIdAndEnvironmentAndAccountTypeAndCurrency(
                anyLong(), any(), any(), any())).thenReturn(Optional.empty());

        new ProcessLedgerEventHandler(create, post, accounts).execute(new ProcessLedgerEventCommand(
                ProcessLedgerEventCommand.Action.BOOTSTRAP_ORGANIZATION, 1L, "TEST", null, null,
                null, null, "SYSTEM", null, null, "NGN"));

        verify(create, times(3)).execute(any(CreateLedgerAccountCommand.class));
    }

    @Test
    void confirmedSettlementClearsPendingReceivableWithoutCreditingOperatingAgain() {
        var create = mock(CreateLedgerAccountHandler.class);
        var post = mock(PostLedgerTransactionHandler.class);
        var accounts = mock(LedgerAccountRepository.class);
        LedgerAccount operating = account(1L, LedgerAccountType.OPERATING, NormalBalance.CREDIT);
        LedgerAccount clearing = account(2L, LedgerAccountType.PROVIDER_CLEARING, NormalBalance.DEBIT);
        when(accounts.findByOrganizationIdAndEnvironmentAndAccountTypeAndCurrency(
                10L, ApiEnvironment.LIVE, LedgerAccountType.OPERATING, CurrencyCode.NGN))
                .thenReturn(Optional.of(operating));
        when(accounts.findByOrganizationIdAndEnvironmentAndAccountTypeAndCurrency(
                10L, ApiEnvironment.LIVE, LedgerAccountType.PROVIDER_CLEARING, CurrencyCode.NGN))
                .thenReturn(Optional.of(clearing));

        new ProcessLedgerEventHandler(create, post, accounts).execute(new ProcessLedgerEventCommand(
                ProcessLedgerEventCommand.Action.PROVIDER_SETTLED, 10L, "LIVE", null, null, null,
                "settlement-1", "SETTLEMENT", "PAYSTACK", new BigDecimal("9500"), "NGN"));

        ArgumentCaptor<PostLedgerTransactionCommand> command =
                ArgumentCaptor.forClass(PostLedgerTransactionCommand.class);
        verify(post).execute(command.capture());
        assertEquals(operating.getId(), command.getValue().entries().getFirst().accountId());
        assertEquals("DEBIT", command.getValue().entries().getFirst().entryType());
        assertEquals(clearing.getId(), command.getValue().entries().getLast().accountId());
        assertEquals("CREDIT", command.getValue().entries().getLast().entryType());
    }

    private LedgerAccount account(Long id, LedgerAccountType type, NormalBalance normalBalance) {
        return LedgerAccount.create(id, 10L, ApiEnvironment.LIVE, type, null, null, null,
                CurrencyCode.NGN, normalBalance);
    }
}
