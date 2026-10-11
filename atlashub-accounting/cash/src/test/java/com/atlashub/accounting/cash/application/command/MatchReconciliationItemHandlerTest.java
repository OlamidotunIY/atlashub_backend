package com.atlashub.accounting.cash.application.command;

import com.atlashub.accounting.cash.application.command.MatchReconciliationItem.MatchReconciliationItemCommand;
import com.atlashub.accounting.cash.application.command.MatchReconciliationItem.MatchReconciliationItemHandler;
import com.atlashub.accounting.cash.domain.entities.BankReconciliation;
import com.atlashub.accounting.cash.domain.entities.BankStatementLine;
import com.atlashub.accounting.cash.domain.exceptions.BankReconciliationNotFoundException;
import com.atlashub.accounting.cash.domain.repositories.BankReconciliationRepository;
import com.atlashub.accounting.cash.domain.valueobject.StatementEntryType;
import com.atlashub.shared.domain.valueobject.CurrencyCode;
import com.atlashub.shared.domain.valueobject.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MatchReconciliationItemHandlerTest {

    private BankReconciliationRepository repository;
    private MatchReconciliationItemHandler handler;

    @BeforeEach
    void setUp() {
        repository = mock(BankReconciliationRepository.class);
        handler = new MatchReconciliationItemHandler(repository);
    }

    @Test
    void execute_matchesItemSuccessfully() {
        BankStatementLine bankLine = BankStatementLine.create(
                101L, 1L, LocalDate.now(), "Deposit", Money.of("1000.00", CurrencyCode.NGN), StatementEntryType.CREDIT
        );
        BankReconciliation recon = BankReconciliation.create(
                1L, 10L, "1001", LocalDate.now(),
                Money.of("1000.00", CurrencyCode.NGN),
                Money.of("1000.00", CurrencyCode.NGN),
                List.of(bankLine),
                List.of(201L)
        );

        when(repository.findById(1L)).thenReturn(Optional.of(recon));
        when(repository.nextIdentity()).thenReturn(501L);

        MatchReconciliationItemCommand command = new MatchReconciliationItemCommand(10L, 1L, 101L, 201L);
        handler.execute(command);

        assertEquals(1, recon.getMatchedItems().size());
        assertEquals(501L, recon.getMatchedItems().get(0).getId());
        assertEquals(0, recon.getUnmatchedBankItems().size());
        assertEquals(0, recon.getUnmatchedBookItems().size());
        verify(repository).save(recon);
    }

    @Test
    void execute_reconciliationNotFound_throwsException() {
        when(repository.findById(999L)).thenReturn(Optional.empty());

        MatchReconciliationItemCommand command = new MatchReconciliationItemCommand(10L, 999L, 101L, 201L);

        assertThrows(BankReconciliationNotFoundException.class, () -> handler.execute(command));
    }

    @Test
    void execute_organizationMismatch_throwsNotFoundException() {
        BankReconciliation recon = BankReconciliation.create(
                1L, 10L, "1001", LocalDate.now(),
                Money.of("1000.00", CurrencyCode.NGN),
                Money.of("1000.00", CurrencyCode.NGN),
                List.of(),
                List.of()
        );

        when(repository.findById(1L)).thenReturn(Optional.of(recon));

        MatchReconciliationItemCommand command = new MatchReconciliationItemCommand(999L, 1L, 101L, 201L);

        assertThrows(BankReconciliationNotFoundException.class, () -> handler.execute(command));
    }
}
