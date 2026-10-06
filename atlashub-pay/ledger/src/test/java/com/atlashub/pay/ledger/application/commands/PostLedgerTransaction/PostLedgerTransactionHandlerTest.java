package com.atlashub.pay.ledger.application.commands.PostLedgerTransaction;

import com.atlashub.pay.ledger.domain.entities.LedgerAccount;
import com.atlashub.pay.ledger.domain.repositories.LedgerAccountRepository;
import com.atlashub.pay.ledger.domain.repositories.LedgerTransactionRepository;
import com.atlashub.pay.ledger.domain.valueobject.*;
import com.atlashub.shared.application.security.ApiEnvironment;
import com.atlashub.shared.domain.valueobject.CurrencyCode;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class PostLedgerTransactionHandlerTest {
    @Test
    void rejects_accounts_from_another_api_environment() {
        LedgerTransactionRepository transactions = mock(LedgerTransactionRepository.class);
        LedgerAccountRepository accounts = mock(LedgerAccountRepository.class);
        LedgerAccount debit = account(1L, ApiEnvironment.TEST, LedgerAccountType.PROVIDER_CLEARING, NormalBalance.DEBIT);
        LedgerAccount credit = account(2L, ApiEnvironment.TEST, LedgerAccountType.OPERATING, NormalBalance.CREDIT);
        when(transactions.findByReferenceAndEnvironment("ref-1", ApiEnvironment.LIVE)).thenReturn(java.util.Optional.empty());
        when(accounts.findAllByIdInWithLock(List.of(1L, 2L))).thenReturn(List.of(debit, credit));

        PostLedgerTransactionHandler handler = new PostLedgerTransactionHandler(transactions, accounts);
        PostLedgerTransactionCommand command = new PostLedgerTransactionCommand(
                10L, "LIVE", "ref-1", "CARD_CHARGE", "charge-1", "charge", "NGN",
                List.of(new PostLedgerTransactionCommand.LedgerEntryRequest(1L, "DEBIT", BigDecimal.TEN),
                        new PostLedgerTransactionCommand.LedgerEntryRequest(2L, "CREDIT", BigDecimal.TEN)));

        assertThrows(IllegalArgumentException.class, () -> handler.execute(command));
        verify(transactions, never()).save(any());
    }

    private LedgerAccount account(Long id, ApiEnvironment environment, LedgerAccountType type, NormalBalance normal) {
        return LedgerAccount.create(id, 10L, environment, type, null, null, null, CurrencyCode.NGN, normal);
    }
}
