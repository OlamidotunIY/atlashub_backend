package com.atlashub.accounting.ar.presentation.rest;

import com.atlashub.accounting.ar.application.command.CreateCustomerReceivable.CreateCustomerReceivableCommand;
import com.atlashub.accounting.ar.application.command.CreateCustomerReceivable.CreateCustomerReceivableHandler;
import com.atlashub.accounting.ar.application.command.RecordReceivablePayment.RecordReceivablePaymentCommand;
import com.atlashub.accounting.ar.application.command.RecordReceivablePayment.RecordReceivablePaymentHandler;
import com.atlashub.accounting.ar.application.command.WriteOffReceivable.WriteOffReceivableCommand;
import com.atlashub.accounting.ar.application.command.WriteOffReceivable.WriteOffReceivableHandler;
import com.atlashub.accounting.ar.application.query.GetOpenReceivables.CustomerReceivableResult;
import com.atlashub.accounting.ar.application.query.GetOpenReceivables.GetOpenReceivablesHandler;
import com.atlashub.accounting.ar.application.query.GetOpenReceivables.GetOpenReceivablesQuery;
import com.atlashub.accounting.ar.domain.valueobject.ArStatus;
import com.atlashub.accounting.ar.presentation.dto.CreateCustomerReceivableRequest;
import com.atlashub.accounting.ar.presentation.dto.CustomerReceivableResponse;
import com.atlashub.accounting.ar.presentation.dto.RecordReceivablePaymentRequest;
import com.atlashub.accounting.ar.presentation.dto.WriteOffRequest;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AccountingArControllerTest {

    private CreateCustomerReceivableHandler createHandler;
    private RecordReceivablePaymentHandler paymentHandler;
    private WriteOffReceivableHandler writeOffHandler;
    private GetOpenReceivablesHandler getOpenHandler;
    private AccountingArController controller;

    private final AuthenticatedPrincipal principal = new AuthenticatedPrincipal(
            1001L, 10L, "TEST", "session-1", "token-1", ZonedDateTime.now());

    @BeforeEach
    void setUp() {
        createHandler = mock(CreateCustomerReceivableHandler.class);
        paymentHandler = mock(RecordReceivablePaymentHandler.class);
        writeOffHandler = mock(WriteOffReceivableHandler.class);
        getOpenHandler = mock(GetOpenReceivablesHandler.class);
        controller = new AccountingArController(createHandler, paymentHandler, writeOffHandler, getOpenHandler);
    }

    @Test
    @DisplayName("Should create customer receivable and return 201 Created")
    void shouldCreateCustomerReceivable() {
        when(createHandler.execute(any())).thenReturn(601L);

        CreateCustomerReceivableRequest request = new CreateCustomerReceivableRequest(
                200L,
                300L,
                new BigDecimal("120000.00"),
                "NGN",
                LocalDate.now().plusDays(30)
        );

        ResponseEntity<ApiResponse<Long>> response = controller.createReceivable(principal, request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(601L, response.getBody().data());

        ArgumentCaptor<CreateCustomerReceivableCommand> captor = ArgumentCaptor.forClass(CreateCustomerReceivableCommand.class);
        verify(createHandler).execute(captor.capture());

        CreateCustomerReceivableCommand command = captor.getValue();
        assertEquals(10L, command.organizationId());
        assertEquals(200L, command.customerId());
        assertEquals(300L, command.salesOrderId());
        assertEquals(Money.of(new BigDecimal("120000.00"), CurrencyCode.NGN), command.amount());
    }

    @Test
    @DisplayName("Should get open customer receivables")
    void shouldGetOpenReceivables() {
        CustomerReceivableResult result = new CustomerReceivableResult(
                601L,
                10L,
                200L,
                300L,
                Money.of(new BigDecimal("120000.00"), CurrencyCode.NGN),
                Money.of(new BigDecimal("40000.00"), CurrencyCode.NGN),
                Money.of(new BigDecimal("80000.00"), CurrencyCode.NGN),
                LocalDate.now().plusDays(30),
                ArStatus.PARTIALLY_COLLECTED,
                ZonedDateTime.now()
        );

        when(getOpenHandler.execute(any())).thenReturn(List.of(result));

        ResponseEntity<ApiResponse<List<CustomerReceivableResponse>>> response =
                controller.getOpenReceivables(principal);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().data().size());
        assertEquals(601L, response.getBody().data().getFirst().id());
        assertThat(response.getBody().data().getFirst().outstandingBalance()).isEqualByComparingTo(new BigDecimal("80000.00"));

        ArgumentCaptor<GetOpenReceivablesQuery> captor = ArgumentCaptor.forClass(GetOpenReceivablesQuery.class);
        verify(getOpenHandler).execute(captor.capture());

        GetOpenReceivablesQuery query = captor.getValue();
        assertEquals(10L, query.organizationId());
    }

    @Test
    @DisplayName("Should record customer payment")
    void shouldRecordPayment() {
        RecordReceivablePaymentRequest request = new RecordReceivablePaymentRequest(
                new BigDecimal("50000.00"),
                "NGN"
        );

        ResponseEntity<ApiResponse<Void>> response = controller.recordPayment(principal, 601L, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Payment recorded successfully", response.getBody().message());

        ArgumentCaptor<RecordReceivablePaymentCommand> captor = ArgumentCaptor.forClass(RecordReceivablePaymentCommand.class);
        verify(paymentHandler).execute(captor.capture());

        RecordReceivablePaymentCommand command = captor.getValue();
        assertEquals(601L, command.receivableId());
        assertEquals(Money.of(new BigDecimal("50000.00"), CurrencyCode.NGN), command.amountPaid());
    }

    @Test
    @DisplayName("Should write off receivable")
    void shouldWriteOffReceivable() {
        WriteOffRequest request = new WriteOffRequest("Uncollectible customer bankruptcy");

        ResponseEntity<ApiResponse<Void>> response = controller.writeOff(principal, 601L, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Receivable written off successfully", response.getBody().message());

        ArgumentCaptor<WriteOffReceivableCommand> captor = ArgumentCaptor.forClass(WriteOffReceivableCommand.class);
        verify(writeOffHandler).execute(captor.capture());

        WriteOffReceivableCommand command = captor.getValue();
        assertEquals(601L, command.receivableId());
        assertEquals("Uncollectible customer bankruptcy", command.reason());
    }
}
