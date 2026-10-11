package com.atlashub.accounting.cash.application.query;

import com.atlashub.accounting.cash.application.query.ListBankReconciliations.BankReconciliationSummaryResult;
import com.atlashub.accounting.cash.application.query.ListBankReconciliations.ListBankReconciliationsHandler;
import com.atlashub.accounting.cash.application.query.ListBankReconciliations.ListBankReconciliationsQuery;
import com.atlashub.accounting.cash.domain.entities.BankReconciliation;
import com.atlashub.accounting.cash.domain.repositories.BankReconciliationRepository;
import com.atlashub.accounting.cash.domain.valueobject.ReconciliationStatus;
import com.atlashub.shared.domain.valueobject.CurrencyCode;
import com.atlashub.shared.domain.valueobject.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ListBankReconciliationsHandlerTest {

    private BankReconciliationRepository repository;
    private ListBankReconciliationsHandler handler;

    @BeforeEach
    void setUp() {
        repository = mock(BankReconciliationRepository.class);
        handler = new ListBankReconciliationsHandler(repository);
    }

    @Test
    void execute_withStatusFilter_returnsFilteredList() {
        BankReconciliation recon = BankReconciliation.create(
                1L, 10L, "1001", LocalDate.now(),
                Money.of("5000.00", CurrencyCode.NGN),
                Money.of("5000.00", CurrencyCode.NGN),
                List.of(),
                List.of()
        );

        when(repository.findByOrganizationIdAndStatus(10L, ReconciliationStatus.DRAFT))
                .thenReturn(List.of(recon));

        ListBankReconciliationsQuery query = new ListBankReconciliationsQuery(10L, ReconciliationStatus.DRAFT);
        List<BankReconciliationSummaryResult> results = handler.execute(query);

        assertEquals(1, results.size());
        assertEquals(1L, results.get(0).id());
        assertEquals("1001", results.get(0).bankAccountCode());
        assertEquals(ReconciliationStatus.DRAFT, results.get(0).status());
    }

    @Test
    void execute_withoutStatusFilter_returnsAllForOrganization() {
        BankReconciliation recon1 = BankReconciliation.create(
                1L, 10L, "1001", LocalDate.now(),
                Money.of("5000.00", CurrencyCode.NGN),
                Money.of("5000.00", CurrencyCode.NGN),
                List.of(),
                List.of()
        );
        BankReconciliation recon2 = BankReconciliation.create(
                2L, 10L, "1002", LocalDate.now(),
                Money.of("3000.00", CurrencyCode.NGN),
                Money.of("3000.00", CurrencyCode.NGN),
                List.of(),
                List.of()
        );

        when(repository.findByOrganizationId(10L)).thenReturn(List.of(recon1, recon2));

        ListBankReconciliationsQuery query = new ListBankReconciliationsQuery(10L, null);
        List<BankReconciliationSummaryResult> results = handler.execute(query);

        assertEquals(2, results.size());
    }

    @Test
    void execute_emptyList_returnsEmpty() {
        when(repository.findByOrganizationId(10L)).thenReturn(List.of());

        ListBankReconciliationsQuery query = new ListBankReconciliationsQuery(10L, null);
        List<BankReconciliationSummaryResult> results = handler.execute(query);

        assertEquals(0, results.size());
    }
}
