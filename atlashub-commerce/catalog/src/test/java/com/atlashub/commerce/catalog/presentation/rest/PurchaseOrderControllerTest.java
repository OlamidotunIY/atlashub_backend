package com.atlashub.commerce.catalog.presentation.rest;

import com.atlashub.commerce.catalog.application.commands.CreatePurchaseOrder.CreatePurchaseOrderCommand;
import com.atlashub.commerce.catalog.application.commands.CreatePurchaseOrder.CreatePurchaseOrderHandler;
import com.atlashub.commerce.catalog.application.commands.CreatePurchaseOrder.CreatePurchaseOrderResult;
import com.atlashub.commerce.catalog.application.commands.ReceivePurchaseOrder.ReceivePurchaseOrderCommand;
import com.atlashub.commerce.catalog.application.commands.ReceivePurchaseOrder.ReceivePurchaseOrderHandler;
import com.atlashub.commerce.catalog.application.commands.SendPurchaseOrder.SendPurchaseOrderCommand;
import com.atlashub.commerce.catalog.application.commands.SendPurchaseOrder.SendPurchaseOrderHandler;
import com.atlashub.commerce.catalog.application.queries.ListPurchaseOrders.ListPurchaseOrdersHandler;
import com.atlashub.commerce.catalog.application.queries.ListPurchaseOrders.ListPurchaseOrdersQuery;
import com.atlashub.commerce.catalog.application.queries.ListPurchaseOrders.PurchaseOrderResult;
import com.atlashub.commerce.catalog.domain.valueobject.PurchaseOrderStatus;
import com.atlashub.commerce.catalog.presentation.dto.CreatePurchaseOrderRequest;
import com.atlashub.commerce.catalog.presentation.dto.CreatePurchaseOrderResponse;
import com.atlashub.commerce.catalog.presentation.dto.PurchaseOrderItemRequest;
import com.atlashub.commerce.catalog.presentation.dto.ReceivePurchaseOrderRequest;
import com.atlashub.commerce.catalog.presentation.dto.ReceivedItemRequest;
import com.atlashub.shared.application.dto.ApiResponse;
import com.atlashub.shared.application.security.ApiEnvironment;
import com.atlashub.shared.application.security.AuthenticatedPrincipal;
import com.atlashub.shared.domain.valueobject.CurrencyCode;
import com.atlashub.shared.domain.valueobject.PageResult;
import org.junit.jupiter.api.BeforeEach;
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
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PurchaseOrderControllerTest {

    @Mock
    private CreatePurchaseOrderHandler createPurchaseOrderHandler;

    @Mock
    private SendPurchaseOrderHandler sendPurchaseOrderHandler;

    @Mock
    private ReceivePurchaseOrderHandler receivePurchaseOrderHandler;

    @Mock
    private ListPurchaseOrdersHandler listPurchaseOrdersHandler;

    @InjectMocks
    private PurchaseOrderController controller;

    private AuthenticatedPrincipal principal;

    @BeforeEach
    void setUp() {
        principal = new AuthenticatedPrincipal(
                1L,
                10L,
                "LIVE",
                "sess-1",
                "tok-1",
                ZonedDateTime.now().plusHours(1)
        );
    }

    @Test
    @DisplayName("Should create purchase order and return 201 Created")
    void shouldCreatePurchaseOrder() {
        CreatePurchaseOrderRequest request = new CreatePurchaseOrderRequest(
                100L,
                200L,
                LocalDate.now().plusDays(7),
                CurrencyCode.NGN,
                List.of(new PurchaseOrderItemRequest(55L, 10, new BigDecimal("250.00")))
        );

        when(createPurchaseOrderHandler.execute(any(CreatePurchaseOrderCommand.class)))
                .thenReturn(new CreatePurchaseOrderResult(88L));

        ResponseEntity<ApiResponse<CreatePurchaseOrderResponse>> response = controller.createPurchaseOrder(principal, request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().status());
        assertEquals(88L, response.getBody().data().purchaseOrderId());

        ArgumentCaptor<CreatePurchaseOrderCommand> captor = ArgumentCaptor.forClass(CreatePurchaseOrderCommand.class);
        verify(createPurchaseOrderHandler).execute(captor.capture());
        assertEquals(10L, captor.getValue().organizationId());
        assertEquals(100L, captor.getValue().outletId());
        assertEquals(200L, captor.getValue().supplierId());
        assertEquals(1, captor.getValue().items().size());
    }

    @Test
    @DisplayName("Should send purchase order")
    void shouldSendPurchaseOrder() {
        ResponseEntity<ApiResponse<Void>> response = controller.sendPurchaseOrder(88L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().status());

        ArgumentCaptor<SendPurchaseOrderCommand> captor = ArgumentCaptor.forClass(SendPurchaseOrderCommand.class);
        verify(sendPurchaseOrderHandler).execute(captor.capture());
        assertEquals(88L, captor.getValue().purchaseOrderId());
    }

    @Test
    @DisplayName("Should receive purchase order items")
    void shouldReceivePurchaseOrder() {
        ReceivePurchaseOrderRequest request = new ReceivePurchaseOrderRequest(
                List.of(new ReceivedItemRequest(55L, 10))
        );

        ResponseEntity<ApiResponse<Void>> response = controller.receivePurchaseOrder(88L, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().status());

        ArgumentCaptor<ReceivePurchaseOrderCommand> captor = ArgumentCaptor.forClass(ReceivePurchaseOrderCommand.class);
        verify(receivePurchaseOrderHandler).execute(captor.capture());
        assertEquals(88L, captor.getValue().purchaseOrderId());
        assertEquals(1, captor.getValue().receivedItems().size());
    }

    @Test
    @DisplayName("Should list purchase orders")
    void shouldListPurchaseOrders() {
        PageResult<PurchaseOrderResult> page = new PageResult<>(List.of(), 0, 20, 0, 0);
        when(listPurchaseOrdersHandler.execute(any(ListPurchaseOrdersQuery.class))).thenReturn(page);

        ResponseEntity<ApiResponse<PageResult<PurchaseOrderResult>>> response =
                controller.listPurchaseOrders(principal, PurchaseOrderStatus.SENT, 0, 20);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().status());
        assertEquals(page, response.getBody().data());
    }
}
