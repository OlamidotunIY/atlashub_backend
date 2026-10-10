package com.atlashub.pay.ledger.application.commands.TransferBetweenAccounts;

import com.atlashub.pay.ledger.application.commands.PostLedgerTransaction.PostLedgerTransactionCommand;
import com.atlashub.pay.ledger.application.commands.PostLedgerTransaction.PostLedgerTransactionHandler;
import com.atlashub.pay.ledger.application.commands.PostLedgerTransaction.PostLedgerTransactionResponse;
import com.atlashub.pay.ledger.domain.entities.BalanceSnapshot;
import com.atlashub.pay.ledger.domain.entities.LedgerAccount;
import com.atlashub.pay.ledger.domain.repositories.BalanceSnapshotRepository;
import com.atlashub.pay.ledger.domain.repositories.LedgerAccountRepository;
import com.atlashub.pay.ledger.domain.repositories.LedgerTransactionRepository;
import com.atlashub.pay.ledger.domain.services.BalanceCalculator;
import com.atlashub.pay.ledger.domain.valueobject.LedgerAccountType;
import com.atlashub.pay.ledger.domain.valueobject.NormalBalance;
import com.atlashub.shared.application.security.ApiEnvironment;
import com.atlashub.shared.domain.valueobject.CurrencyCode;
import com.atlashub.shared.domain.valueobject.Money;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransferBetweenAccountsHandlerTest {
    @Mock private LedgerAccountRepository accounts;
    @Mock private BalanceSnapshotRepository snapshots;
    @Mock private LedgerTransactionRepository transactions;
    @Mock private BalanceCalculator calculator;
    @Mock private PostLedgerTransactionHandler postHandler;

    @Test
    void postsBalancedTransferBetweenOwnedBusinessAccounts() {
        LedgerAccount source = LedgerAccount.create(1L, 42L, ApiEnvironment.TEST,
                LedgerAccountType.OPERATING, null, null, null, CurrencyCode.NGN, NormalBalance.DEBIT);
        LedgerAccount destination = LedgerAccount.create(2L, 42L, ApiEnvironment.TEST,
                LedgerAccountType.PAYROLL_RESERVE, null, null, null, CurrencyCode.NGN, NormalBalance.DEBIT);
        when(accounts.findById(1L)).thenReturn(Optional.of(source));
        when(accounts.findById(2L)).thenReturn(Optional.of(destination));
        ZonedDateTime now = ZonedDateTime.now();
        when(snapshots.findLatestByAccountId(1L)).thenReturn(Optional.of(BalanceSnapshot.create(
                10L, 1L, Money.of(new BigDecimal("5000"), CurrencyCode.NGN), now)));
        when(transactions.findByAccountIdAndEnvironmentAndPostedAtAfter(1L, ApiEnvironment.TEST, now))
                .thenReturn(List.of());
        when(calculator.calculateRunningBalance(any(), any(), any(), any()))
                .thenReturn(new BigDecimal("5000"));
        when(postHandler.execute(any())).thenReturn(new PostLedgerTransactionResponse(77L, "MOVE-1", now));

        var result = new TransferBetweenAccountsHandler(accounts, snapshots, transactions, calculator, postHandler)
                .execute(new TransferBetweenAccountsCommand(42L, 7L, "TEST", 1L, 2L,
                        new BigDecimal("1250"), "NGN", "MOVE-1", "Fund payroll"));

        assertEquals(77L, result.transactionId());
        ArgumentCaptor<PostLedgerTransactionCommand> command =
                ArgumentCaptor.forClass(PostLedgerTransactionCommand.class);
        verify(postHandler).execute(command.capture());
        assertEquals("DEBIT", command.getValue().entries().get(0).entryType());
        assertEquals("CREDIT", command.getValue().entries().get(1).entryType());
        assertEquals(new BigDecimal("1250"), command.getValue().entries().get(0).amount());
    }
}
