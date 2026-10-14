package com.atlashub.accounting.cash.presentation.rest;

import com.atlashub.accounting.cash.application.command.FinalizeReconciliation.FinalizeReconciliationCommand;
import com.atlashub.accounting.cash.application.command.FinalizeReconciliation.FinalizeReconciliationHandler;
import com.atlashub.accounting.cash.application.command.MatchReconciliationItem.MatchReconciliationItemCommand;
import com.atlashub.accounting.cash.application.command.MatchReconciliationItem.MatchReconciliationItemHandler;
import com.atlashub.accounting.cash.application.command.StartBankReconciliation.StartBankReconciliationCommand;
import com.atlashub.accounting.cash.application.command.StartBankReconciliation.StartBankReconciliationHandler;
import com.atlashub.accounting.cash.application.query.GetBankReconciliation.BankReconciliationResult;
import com.atlashub.accounting.cash.application.query.GetBankReconciliation.BankStatementLineResult;
import com.atlashub.accounting.cash.application.query.GetBankReconciliation.GetBankReconciliationHandler;
import com.atlashub.accounting.cash.application.query.GetBankReconciliation.GetBankReconciliationQuery;
import com.atlashub.accounting.cash.application.query.GetBankReconciliation.ReconciliationItemResult;
import com.atlashub.accounting.cash.application.query.ListBankReconciliations.BankReconciliationSummaryResult;
import com.atlashub.accounting.cash.application.query.ListBankReconciliations.ListBankReconciliationsHandler;
import com.atlashub.accounting.cash.application.query.ListBankReconciliations.ListBankReconciliationsQuery;
import com.atlashub.accounting.cash.domain.valueobject.ReconciliationStatus;
import com.atlashub.accounting.cash.domain.valueobject.StatementEntryType;
import com.atlashub.accounting.cash.presentation.dto.BankReconciliationResponse;
import com.atlashub.accounting.cash.presentation.dto.BankStatementLineDto;
import com.atlashub.accounting.cash.presentation.dto.FinalizeReconciliationRequest;
import com.atlashub.accounting.cash.presentation.dto.MatchReconciliationItemRequest;
import com.atlashub.accounting.cash.presentation.dto.StartBankReconciliationRequest;
import com.atlashub.accounting.cash.presentation.dto.StartBankReconciliationResponse;
import com.atlashub.shared.application.dto.ApiResponse;
import com.atlashub.shared.application.security.AuthenticatedPrincipal;
import com.atlashub.shared.domain.valueobject.CurrencyCode;
import com.atlashub.shared.domain.valueobject.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BankReconciliationControllerTest {

    private StartBankReconciliationHandler startHandler;
    private MatchReconciliationItemHandler matchHandler;
    private FinalizeReconciliationHandler finalizeHandler;
    private GetBankReconciliationHandler getHandler;
    private ListBankReconciliationsHandler listHandler;
    private BankReconciliationController controller;

    private final AuthenticatedPrincipal principal = new AuthenticatedPrincipal(
            1001L, 10L, "TEST", "session-1", "token-1", ZonedDateTime.now());

    @BeforeEach
    void setUp() {
        startHandler = mock(StartBankReconciliationHandler.class);
        matchHandler = mock(MatchReconciliationItemHandler.class);
        finalizeHandler = mock(FinalizeReconciliationHandler.class);
        getHandler = mock(GetBankReconciliationHandler.class);
        listHandler = mock(ListBankReconciliationsHandler.class);

        controller = new BankReconciliationController(
                startHandler,
                matchHandler,
                finalizeHandler,
                getHandler,
                listHandler
        );
    }

    @Test
    @DisplayName("Should start bank reconciliation successfully")
    void shouldStartBankReconciliation() {
        when(startHandler.execute(any())).thenReturn(200L);

        StartBankReconciliationRequest request = new StartBankReconciliationRequest(
                "1010-BANK",
                LocalDate.of(2026, 10, 1),
                new BigDecimal("500000.00"),
                "NGN",
                List.of(new BankStatementLineDto(
                        LocalDate.of(2026, 10, 1),
                        "Deposit from ATM",
                        new BigDecimal("100000.00"),
                        StatementEntryType.CREDIT
                ))
        );

        ResponseEntity<ApiResponse<StartBankReconciliationResponse>> response = controller.startReconciliation(principal, request);

        assertNotNull(response.getBody());
        assertEquals(200L, response.getBody().data().reconciliationId());

        ArgumentCaptor<StartBankReconciliationCommand> captor = ArgumentCaptor.forClass(StartBankReconciliationCommand.class);
        verify(startHandler).execute(captor.capture());

        StartBankReconciliationCommand command = captor.getValue();
        assertEquals(10L, command.organizationId());
        assertEquals("1010-BANK", command.bankAccountCode());
        assertEquals(1, command.bankLines().size());
    }

    @Test
    @DisplayName("Should get bank reconciliation by ID")
    void shouldGetBankReconciliationById() {
        ZonedDateTime now = ZonedDateTime.now();
        BankReconciliationResult result = new BankReconciliationResult(
                200L,
                10L,
                "1010-BANK",
                LocalDate.of(2026, 10, 1),
                Money.of(new BigDecimal("500000.00"), CurrencyCode.NGN),
                Money.of(new BigDecimal("500000.00"), CurrencyCode.NGN),
                Money.zero(CurrencyCode.NGN),
                ReconciliationStatus.DRAFT,
                List.of(new ReconciliationItemResult(1L, 200L, 101L, 301L)),
                List.of(new BankStatementLineResult(102L, 200L, LocalDate.of(2026, 10, 1), "Bank charge",
                        Money.of(new BigDecimal("50.00"), CurrencyCode.NGN), StatementEntryType.DEBIT)),
                List.of(302L),
                now,
                null
        );

        when(getHandler.execute(any())).thenReturn(result);

        ResponseEntity<ApiResponse<BankReconciliationResponse>> response = controller.getReconciliationById(principal, 200L);

        assertNotNull(response.getBody());
        BankReconciliationResponse data = response.getBody().data();
        assertEquals(200L, data.id());
        assertEquals("1010-BANK", data.bankAccountCode());
        assertEquals(1, data.matchedItems().size());
        assertEquals(1, data.unmatchedBankItems().size());
        assertEquals(1, data.unmatchedBookItems().size());

        ArgumentCaptor<GetBankReconciliationQuery> captor = ArgumentCaptor.forClass(GetBankReconciliationQuery.class);
        verify(getHandler).execute(captor.capture());
        assertEquals(10L, captor.getValue().organizationId());
        assertEquals(200L, captor.getValue().reconciliationId());
    }

    @Test
    @DisplayName("Should list bank reconciliations for active organization")
    void shouldListBankReconciliations() {
        ZonedDateTime now = ZonedDateTime.now();
        List<BankReconciliationSummaryResult> summaryResults = List.of(
                new BankReconciliationSummaryResult(
                        200L,
                        10L,
                        "1010-BANK",
                        LocalDate.of(2026, 10, 1),
                        Money.of(new BigDecimal("500000.00"), CurrencyCode.NGN),
                        Money.of(new BigDecimal("500000.00"), CurrencyCode.NGN),
                        Money.zero(CurrencyCode.NGN),
                        ReconciliationStatus.RECONCILED,
                        5,
                        0,
                        0,
                        now,
                        now
                )
        );

        when(listHandler.execute(any())).thenReturn(summaryResults);

        ResponseEntity<ApiResponse<List<BankReconciliationSummaryResult>>> response =
                controller.listReconciliations(principal, ReconciliationStatus.RECONCILED);

        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().data().size());

        ArgumentCaptor<ListBankReconciliationsQuery> captor = ArgumentCaptor.forClass(ListBankReconciliationsQuery.class);
        verify(listHandler).execute(captor.capture());
        assertEquals(10L, captor.getValue().organizationId());
        assertEquals(ReconciliationStatus.RECONCILED, captor.getValue().status());
    }

    @Test
    @DisplayName("Should match reconciliation item")
    void shouldMatchReconciliationItem() {
        MatchReconciliationItemRequest request = new MatchReconciliationItemRequest(101L, 301L);

        ResponseEntity<ApiResponse<Void>> response = controller.matchItem(principal, 200L, request);

        assertNotNull(response.getBody());
        assertEquals("Reconciliation item matched", response.getBody().message());

        ArgumentCaptor<MatchReconciliationItemCommand> captor = ArgumentCaptor.forClass(MatchReconciliationItemCommand.class);
        verify(matchHandler).execute(captor.capture());
        assertEquals(10L, captor.getValue().organizationId());
        assertEquals(200L, captor.getValue().reconciliationId());
        assertEquals(101L, captor.getValue().bankLineId());
        assertEquals(301L, captor.getValue().journalLineId());
    }

    @Test
    @DisplayName("Should finalize bank reconciliation")
    void shouldFinalizeBankReconciliation() {
        FinalizeReconciliationRequest request = new FinalizeReconciliationRequest(true);

        ResponseEntity<ApiResponse<Void>> response = controller.finalizeReconciliation(principal, 200L, request);

        assertNotNull(response.getBody());
        assertEquals("Bank reconciliation finalized", response.getBody().message());

        ArgumentCaptor<FinalizeReconciliationCommand> captor = ArgumentCaptor.forClass(FinalizeReconciliationCommand.class);
        verify(finalizeHandler).execute(captor.capture());
        assertEquals(10L, captor.getValue().organizationId());
        assertEquals(200L, captor.getValue().reconciliationId());
        assertEquals(true, captor.getValue().forceFinalize());
    }
}
