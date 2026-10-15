package com.atlashub.accounting.ap.presentation.rest;

import com.atlashub.accounting.ap.application.command.DisputeSupplierInvoice.DisputeSupplierInvoiceCommand;
import com.atlashub.accounting.ap.application.command.DisputeSupplierInvoice.DisputeSupplierInvoiceHandler;
import com.atlashub.accounting.ap.application.command.RecordSupplierInvoice.RecordSupplierInvoiceCommand;
import com.atlashub.accounting.ap.application.command.RecordSupplierInvoice.RecordSupplierInvoiceHandler;
import com.atlashub.accounting.ap.application.query.ListSupplierInvoices.ListSupplierInvoicesHandler;
import com.atlashub.accounting.ap.application.query.ListSupplierInvoices.ListSupplierInvoicesQuery;
import com.atlashub.accounting.ap.application.query.ListSupplierInvoices.SupplierInvoiceResult;
import com.atlashub.accounting.ap.domain.valueobject.ApStatus;
import com.atlashub.accounting.ap.presentation.dto.DisputeInvoiceRequest;
import com.atlashub.accounting.ap.presentation.dto.RecordSupplierInvoiceRequest;
import com.atlashub.accounting.ap.presentation.dto.SupplierInvoiceResponse;
import com.atlashub.shared.application.dto.ApiResponse;
import com.atlashub.shared.application.security.AuthenticatedPrincipal;
import com.atlashub.shared.domain.valueobject.CurrencyCode;
import com.atlashub.shared.domain.valueobject.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpStatus;
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

class AccountingApControllerTest {

    private RecordSupplierInvoiceHandler recordHandler;
    private DisputeSupplierInvoiceHandler disputeHandler;
    private ListSupplierInvoicesHandler listHandler;
    private AccountingApController controller;

    private final AuthenticatedPrincipal principal = new AuthenticatedPrincipal(
            1001L, 10L, "TEST", "session-1", "token-1", ZonedDateTime.now());

    @BeforeEach
    void setUp() {
        recordHandler = mock(RecordSupplierInvoiceHandler.class);
        disputeHandler = mock(DisputeSupplierInvoiceHandler.class);
        listHandler = mock(ListSupplierInvoicesHandler.class);
        controller = new AccountingApController(recordHandler, disputeHandler, listHandler);
    }

    @Test
    @DisplayName("Should record supplier invoice and return 201 Created")
    void shouldRecordSupplierInvoice() {
        when(recordHandler.execute(any())).thenReturn(501L);

        RecordSupplierInvoiceRequest request = new RecordSupplierInvoiceRequest(
                200L,
                300L,
                "INV-100",
                new BigDecimal("150000.00"),
                "NGN",
                LocalDate.now().plusDays(30)
        );

        ResponseEntity<ApiResponse<Long>> response = controller.recordInvoice(principal, request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(501L, response.getBody().data());

        ArgumentCaptor<RecordSupplierInvoiceCommand> captor = ArgumentCaptor.forClass(RecordSupplierInvoiceCommand.class);
        verify(recordHandler).execute(captor.capture());

        RecordSupplierInvoiceCommand command = captor.getValue();
        assertEquals(10L, command.organizationId());
        assertEquals(200L, command.supplierId());
        assertEquals(300L, command.purchaseOrderId());
        assertEquals("INV-100", command.invoiceNumber());
        assertEquals(Money.of(new BigDecimal("150000.00"), CurrencyCode.NGN), command.amount());
    }

    @Test
    @DisplayName("Should list supplier invoices")
    void shouldListSupplierInvoices() {
        SupplierInvoiceResult result = new SupplierInvoiceResult(
                501L,
                10L,
                200L,
                300L,
                "INV-100",
                Money.of(new BigDecimal("150000.00"), CurrencyCode.NGN),
                LocalDate.now().plusDays(30),
                ApStatus.UNPAID,
                null,
                ZonedDateTime.now()
        );

        when(listHandler.execute(any())).thenReturn(List.of(result));

        ResponseEntity<ApiResponse<List<SupplierInvoiceResponse>>> response =
                controller.listInvoices(principal, 200L, ApStatus.UNPAID);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().data().size());
        assertEquals("INV-100", response.getBody().data().getFirst().invoiceNumber());

        ArgumentCaptor<ListSupplierInvoicesQuery> captor = ArgumentCaptor.forClass(ListSupplierInvoicesQuery.class);
        verify(listHandler).execute(captor.capture());

        ListSupplierInvoicesQuery query = captor.getValue();
        assertEquals(10L, query.organizationId());
        assertEquals(200L, query.supplierId());
        assertEquals(ApStatus.UNPAID, query.status());
    }

    @Test
    @DisplayName("Should dispute supplier invoice")
    void shouldDisputeSupplierInvoice() {
        DisputeInvoiceRequest request = new DisputeInvoiceRequest("Damaged goods upon arrival");

        ResponseEntity<ApiResponse<Void>> response = controller.disputeInvoice(principal, 501L, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Supplier invoice disputed successfully", response.getBody().message());

        ArgumentCaptor<DisputeSupplierInvoiceCommand> captor = ArgumentCaptor.forClass(DisputeSupplierInvoiceCommand.class);
        verify(disputeHandler).execute(captor.capture());

        DisputeSupplierInvoiceCommand command = captor.getValue();
        assertEquals(501L, command.invoiceId());
        assertEquals("Damaged goods upon arrival", command.reason());
    }
}
