package com.atlashub.accounting.cash.application.command;

import com.atlashub.accounting.cash.application.command.FinalizeReconciliation.FinalizeReconciliationCommand;
import com.atlashub.accounting.cash.application.command.FinalizeReconciliation.FinalizeReconciliationHandler;
import com.atlashub.accounting.cash.domain.entities.BankReconciliation;
import com.atlashub.accounting.cash.domain.exceptions.BankReconciliationNotFoundException;
import com.atlashub.accounting.cash.domain.exceptions.ReconciliationNotBalancedException;
import com.atlashub.accounting.cash.domain.repositories.BankReconciliationRepository;
import com.atlashub.accounting.cash.domain.valueobject.ReconciliationStatus;
import com.atlashub.shared.domain.valueobject.CurrencyCode;
import com.atlashub.shared.domain.valueobject.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class FinalizeReconciliationHandlerTest {

    private BankReconciliationRepository repository;
    private FinalizeReconciliationHandler handler;

    @BeforeEach
    void setUp() {
        repository = mock(BankReconciliationRepository.class);
        handler = new FinalizeReconciliationHandler(repository);
    }

    @Test
    void execute_balancedReconciliation_finalizesSuccessfully() {
        BankReconciliation recon = BankReconciliation.create(
                1L, 10L, "1001", LocalDate.now(),
                Money.of("5000.00", CurrencyCode.NGN),
                Money.of("5000.00", CurrencyCode.NGN),
                List.of(),
                List.of()
        );

        when(repository.findById(1L)).thenReturn(Optional.of(recon));

        FinalizeReconciliationCommand command = new FinalizeReconciliationCommand(10L, 1L, false);
        handler.execute(command);

        assertEquals(ReconciliationStatus.RECONCILED, recon.getStatus());
        assertNotNull(recon.getReconciledAt());
        verify(repository).save(recon);
    }

    @Test
    void execute_unbalancedReconciliationWithForce_finalizesSuccessfully() {
        BankReconciliation recon = BankReconciliation.create(
                1L, 10L, "1001", LocalDate.now(),
                Money.of("6000.00", CurrencyCode.NGN),
                Money.of("5000.00", CurrencyCode.NGN),
                List.of(),
                List.of()
        );

        when(repository.findById(1L)).thenReturn(Optional.of(recon));

        FinalizeReconciliationCommand command = new FinalizeReconciliationCommand(10L, 1L, true);
        handler.execute(command);

        assertEquals(ReconciliationStatus.RECONCILED, recon.getStatus());
        assertNotNull(recon.getReconciledAt());
        verify(repository).save(recon);
    }

    @Test
    void execute_unbalancedReconciliationWithoutForce_throwsException() {
        BankReconciliation recon = BankReconciliation.create(
                1L, 10L, "1001", LocalDate.now(),
                Money.of("6000.00", CurrencyCode.NGN),
                Money.of("5000.00", CurrencyCode.NGN),
                List.of(),
                List.of()
        );

        when(repository.findById(1L)).thenReturn(Optional.of(recon));

        FinalizeReconciliationCommand command = new FinalizeReconciliationCommand(10L, 1L, false);

        assertThrows(ReconciliationNotBalancedException.class, () -> handler.execute(command));
    }

    @Test
    void execute_reconciliationNotFound_throwsException() {
        when(repository.findById(999L)).thenReturn(Optional.empty());

        FinalizeReconciliationCommand command = new FinalizeReconciliationCommand(10L, 999L, false);

        assertThrows(BankReconciliationNotFoundException.class, () -> handler.execute(command));
    }
}
