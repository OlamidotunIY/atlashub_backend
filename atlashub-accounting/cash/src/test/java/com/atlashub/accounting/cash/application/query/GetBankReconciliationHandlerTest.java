package com.atlashub.accounting.cash.application.query;

import com.atlashub.accounting.cash.application.query.GetBankReconciliation.BankReconciliationResult;
import com.atlashub.accounting.cash.application.query.GetBankReconciliation.GetBankReconciliationHandler;
import com.atlashub.accounting.cash.application.query.GetBankReconciliation.GetBankReconciliationQuery;
import com.atlashub.accounting.cash.domain.entities.BankReconciliation;
import com.atlashub.accounting.cash.domain.entities.BankStatementLine;
import com.atlashub.accounting.cash.domain.exceptions.BankReconciliationNotFoundException;
import com.atlashub.accounting.cash.domain.repositories.BankReconciliationRepository;
import com.atlashub.accounting.cash.domain.valueobject.ReconciliationStatus;
import com.atlashub.accounting.cash.domain.valueobject.StatementEntryType;
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
import static org.mockito.Mockito.when;

class GetBankReconciliationHandlerTest {

    private BankReconciliationRepository repository;
    private GetBankReconciliationHandler handler;

    @BeforeEach
    void setUp() {
        repository = mock(BankReconciliationRepository.class);
        handler = new GetBankReconciliationHandler(repository);
    }

    @Test
    void execute_reconciliationFound_returnsFullResult() {
        LocalDate statementDate = LocalDate.now();
        BankStatementLine line = BankStatementLine.create(
                101L, 1L, statementDate, "Deposit", Money.of("1000.00", CurrencyCode.NGN), StatementEntryType.CREDIT
        );
        BankReconciliation recon = BankReconciliation.create(
                1L, 10L, "1001", statementDate,
                Money.of("5000.00", CurrencyCode.NGN),
                Money.of("5000.00", CurrencyCode.NGN),
                List.of(line),
                List.of(201L)
        );

        when(repository.findById(1L)).thenReturn(Optional.of(recon));

        GetBankReconciliationQuery query = new GetBankReconciliationQuery(10L, 1L);
        BankReconciliationResult result = handler.execute(query);

        assertNotNull(result);
        assertEquals(1L, result.id());
        assertEquals(10L, result.organizationId());
        assertEquals("1001", result.bankAccountCode());
        assertEquals(statementDate, result.statementDate());
        assertEquals(ReconciliationStatus.DRAFT, result.status());
        assertEquals(1, result.unmatchedBankItems().size());
        assertEquals(101L, result.unmatchedBankItems().get(0).id());
        assertEquals(1, result.unmatchedBookItems().size());
        assertEquals(201L, result.unmatchedBookItems().get(0));
    }

    @Test
    void execute_notFound_throwsException() {
        when(repository.findById(999L)).thenReturn(Optional.empty());

        GetBankReconciliationQuery query = new GetBankReconciliationQuery(10L, 999L);

        assertThrows(BankReconciliationNotFoundException.class, () -> handler.execute(query));
    }

    @Test
    void execute_orgMismatch_throwsException() {
        BankReconciliation recon = BankReconciliation.create(
                1L, 10L, "1001", LocalDate.now(),
                Money.of("5000.00", CurrencyCode.NGN),
                Money.of("5000.00", CurrencyCode.NGN),
                List.of(),
                List.of()
        );

        when(repository.findById(1L)).thenReturn(Optional.of(recon));

        GetBankReconciliationQuery query = new GetBankReconciliationQuery(999L, 1L);

        assertThrows(BankReconciliationNotFoundException.class, () -> handler.execute(query));
    }
}
