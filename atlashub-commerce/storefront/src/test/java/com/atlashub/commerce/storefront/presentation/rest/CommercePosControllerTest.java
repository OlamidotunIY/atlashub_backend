package com.atlashub.commerce.storefront.presentation.rest;

import com.atlashub.commerce.storefront.application.commands.CloseTill.CloseTillCommand;
import com.atlashub.commerce.storefront.application.commands.CloseTill.CloseTillHandler;
import com.atlashub.commerce.storefront.application.commands.OpenTill.OpenTillCommand;
import com.atlashub.commerce.storefront.application.commands.OpenTill.OpenTillHandler;
import com.atlashub.commerce.storefront.application.commands.OpenTill.OpenTillResult;
import com.atlashub.commerce.storefront.application.commands.ProcessCreditSale.ProcessCreditSaleCommand;
import com.atlashub.commerce.storefront.application.commands.ProcessCreditSale.ProcessCreditSaleHandler;
import com.atlashub.commerce.storefront.application.commands.ProcessCreditSale.ProcessCreditSaleResult;
import com.atlashub.commerce.storefront.application.commands.ProcessPosCheckout.ProcessPosCheckoutCommand;
import com.atlashub.commerce.storefront.application.commands.ProcessPosCheckout.ProcessPosCheckoutHandler;
import com.atlashub.commerce.storefront.application.commands.ProcessPosCheckout.ProcessPosCheckoutResult;
import com.atlashub.commerce.storefront.application.commands.RefundPosSale.RefundPosSaleCommand;
import com.atlashub.commerce.storefront.application.commands.RefundPosSale.RefundPosSaleHandler;
import com.atlashub.commerce.storefront.application.queries.GetSalesOrder.SalesOrderResult;
import com.atlashub.commerce.storefront.application.queries.GetTillSummary.GetTillSummaryHandler;
import com.atlashub.commerce.storefront.application.queries.GetTillSummary.GetTillSummaryQuery;
import com.atlashub.commerce.storefront.application.queries.GetTillSummary.TillSummaryResult;
import com.atlashub.commerce.storefront.application.queries.ListPosTransactions.ListPosTransactionsHandler;
import com.atlashub.commerce.storefront.application.queries.ListPosTransactions.ListPosTransactionsQuery;
import com.atlashub.commerce.storefront.domain.valueobject.OrderStatus;
import com.atlashub.commerce.storefront.domain.valueobject.OrderType;
import com.atlashub.commerce.storefront.domain.valueobject.PaymentMethod;
import com.atlashub.commerce.storefront.domain.valueobject.TillStatus;
import com.atlashub.commerce.storefront.presentation.dto.CloseTillRequest;
import com.atlashub.commerce.storefront.presentation.dto.OpenTillRequest;
import com.atlashub.commerce.storefront.presentation.dto.OpenTillResponse;
import com.atlashub.commerce.storefront.presentation.dto.OrderItemRequest;
import com.atlashub.commerce.storefront.presentation.dto.ProcessCreditSaleRequest;
import com.atlashub.commerce.storefront.presentation.dto.ProcessCreditSaleResponse;
import com.atlashub.commerce.storefront.presentation.dto.ProcessPosCheckoutRequest;
import com.atlashub.commerce.storefront.presentation.dto.ProcessPosCheckoutResponse;
import com.atlashub.commerce.storefront.presentation.dto.RefundPosSaleRequest;
import com.atlashub.shared.application.dto.ApiResponse;
import com.atlashub.shared.application.security.AuthenticatedPrincipal;
import com.atlashub.shared.domain.valueobject.CurrencyCode;
import com.atlashub.shared.domain.valueobject.Money;
import com.atlashub.shared.domain.valueobject.PageResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CommercePosControllerTest {

    @Mock
    private ProcessPosCheckoutHandler processPosCheckoutHandler;

    @Mock
    private ProcessCreditSaleHandler processCreditSaleHandler;

    @Mock
    private RefundPosSaleHandler refundPosSaleHandler;

    @Mock
    private ListPosTransactionsHandler listPosTransactionsHandler;

    @Mock
    private OpenTillHandler openTillHandler;

    @Mock
    private CloseTillHandler closeTillHandler;

    @Mock
    private GetTillSummaryHandler getTillSummaryHandler;

    @InjectMocks
    private CommercePosController controller;

    private AuthenticatedPrincipal createPrincipal() {
        return new AuthenticatedPrincipal(
                1L,
                10L,
                "LIVE",
                "sess-123",
                "tok-123",
                ZonedDateTime.now().plusHours(1)
        );
    }

    @Test
    @DisplayName("Should process POS checkout and return 201")
    void checkout_shouldReturn201() {
        AuthenticatedPrincipal principal = createPrincipal();
        ProcessPosCheckoutRequest request = new ProcessPosCheckoutRequest(
                20L,
                30L,
                40L,
                OrderType.POS_RETAIL,
                List.of(new OrderItemRequest(100L, null, 2, new BigDecimal("1500.00"), CurrencyCode.NGN, null, null)),
                null,
                PaymentMethod.CASH
        );

        when(processPosCheckoutHandler.execute(any(ProcessPosCheckoutCommand.class)))
                .thenReturn(new ProcessPosCheckoutResult(888L, OrderStatus.COMPLETED));

        ResponseEntity<ApiResponse<ProcessPosCheckoutResponse>> response =
                controller.checkout(principal, request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().status());
        assertEquals(888L, response.getBody().data().salesOrderId());
        assertEquals(OrderStatus.COMPLETED, response.getBody().data().status());

        ArgumentCaptor<ProcessPosCheckoutCommand> captor = ArgumentCaptor.forClass(ProcessPosCheckoutCommand.class);
        verify(processPosCheckoutHandler).execute(captor.capture());
        assertEquals(10L, captor.getValue().organizationId());
        assertEquals(20L, captor.getValue().outletId());
        assertEquals(1L, captor.getValue().cashierId());
        assertEquals(PaymentMethod.CASH, captor.getValue().paymentMethod());
    }

    @Test
    @DisplayName("Should process credit sale and return 201")
    void processCreditSale_shouldReturn201() {
        AuthenticatedPrincipal principal = createPrincipal();
        ProcessCreditSaleRequest request = new ProcessCreditSaleRequest(
                20L,
                40L,
                List.of(new OrderItemRequest(100L, null, 1, new BigDecimal("2000.00"), CurrencyCode.NGN, null, null))
        );

        when(processCreditSaleHandler.execute(any(ProcessCreditSaleCommand.class)))
                .thenReturn(new ProcessCreditSaleResult(889L));

        ResponseEntity<ApiResponse<ProcessCreditSaleResponse>> response =
                controller.processCreditSale(principal, request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().status());
        assertEquals(889L, response.getBody().data().salesOrderId());

        ArgumentCaptor<ProcessCreditSaleCommand> captor = ArgumentCaptor.forClass(ProcessCreditSaleCommand.class);
        verify(processCreditSaleHandler).execute(captor.capture());
        assertEquals(10L, captor.getValue().organizationId());
        assertEquals(40L, captor.getValue().customerId());
    }

    @Test
    @DisplayName("Should refund POS sale and return 200")
    void refundSale_shouldReturn200() {
        RefundPosSaleRequest request = new RefundPosSaleRequest(
                888L,
                "Customer return",
                "0123456789",
                "058"
        );

        ResponseEntity<ApiResponse<Void>> response = controller.refundSale(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().status());

        ArgumentCaptor<RefundPosSaleCommand> captor = ArgumentCaptor.forClass(RefundPosSaleCommand.class);
        verify(refundPosSaleHandler).execute(captor.capture());
        assertEquals(888L, captor.getValue().salesOrderId());
        assertEquals("Customer return", captor.getValue().reason());
    }

    @Test
    @DisplayName("Should list POS transactions and return 200")
    void listTransactions_shouldReturn200() {
        PageResult<SalesOrderResult> pageResult = new PageResult<>(
                Collections.emptyList(),
                0,
                20,
                0,
                0
        );

        when(listPosTransactionsHandler.execute(any(ListPosTransactionsQuery.class)))
                .thenReturn(pageResult);

        ResponseEntity<ApiResponse<PageResult<SalesOrderResult>>> response =
                controller.listTransactions(20L, null, null, null, null, 0, 20);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().status());

        ArgumentCaptor<ListPosTransactionsQuery> captor = ArgumentCaptor.forClass(ListPosTransactionsQuery.class);
        verify(listPosTransactionsHandler).execute(captor.capture());
        assertEquals(20L, captor.getValue().outletId());
    }

    @Test
    @DisplayName("Should open till and return 201")
    void openTill_shouldReturn201() {
        AuthenticatedPrincipal principal = createPrincipal();
        OpenTillRequest request = new OpenTillRequest(
                20L,
                "Main Register",
                new BigDecimal("50000.00"),
                CurrencyCode.NGN
        );

        when(openTillHandler.execute(any(OpenTillCommand.class)))
                .thenReturn(new OpenTillResult(777L));

        ResponseEntity<ApiResponse<OpenTillResponse>> response =
                controller.openTill(principal, request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().status());
        assertEquals(777L, response.getBody().data().tillId());

        ArgumentCaptor<OpenTillCommand> captor = ArgumentCaptor.forClass(OpenTillCommand.class);
        verify(openTillHandler).execute(captor.capture());
        assertEquals(10L, captor.getValue().organizationId());
        assertEquals(1L, captor.getValue().openedBy());
    }

    @Test
    @DisplayName("Should close till and return 200")
    void closeTill_shouldReturn200() {
        AuthenticatedPrincipal principal = createPrincipal();
        CloseTillRequest request = new CloseTillRequest(
                new BigDecimal("75000.00"),
                CurrencyCode.NGN
        );

        ResponseEntity<ApiResponse<Void>> response = controller.closeTill(777L, principal, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().status());

        ArgumentCaptor<CloseTillCommand> captor = ArgumentCaptor.forClass(CloseTillCommand.class);
        verify(closeTillHandler).execute(captor.capture());
        assertEquals(777L, captor.getValue().tillId());
        assertEquals(1L, captor.getValue().closedBy());
    }

    @Test
    @DisplayName("Should get till summary and return 200")
    void getTillSummary_shouldReturn200() {
        TillSummaryResult summary = new TillSummaryResult(
                777L,
                10L,
                20L,
                "Main Register",
                Money.of(new BigDecimal("50000.00"), CurrencyCode.NGN),
                Money.of(new BigDecimal("75000.00"), CurrencyCode.NGN),
                null,
                TillStatus.OPEN,
                ZonedDateTime.now(),
                null,
                1L,
                null
        );

        when(getTillSummaryHandler.execute(any(GetTillSummaryQuery.class)))
                .thenReturn(summary);

        ResponseEntity<ApiResponse<TillSummaryResult>> response = controller.getTillSummary(777L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().status());
        assertEquals(777L, response.getBody().data().tillId());
        assertEquals(TillStatus.OPEN, response.getBody().data().status());
    }
}
