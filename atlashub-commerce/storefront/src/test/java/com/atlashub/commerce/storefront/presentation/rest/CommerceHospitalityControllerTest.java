package com.atlashub.commerce.storefront.presentation.rest;

import com.atlashub.commerce.storefront.application.commands.ClearTable.ClearTableCommand;
import com.atlashub.commerce.storefront.application.commands.ClearTable.ClearTableHandler;
import com.atlashub.commerce.storefront.application.commands.MarkKotReady.MarkKotReadyCommand;
import com.atlashub.commerce.storefront.application.commands.MarkKotReady.MarkKotReadyHandler;
import com.atlashub.commerce.storefront.application.commands.MarkKotServed.MarkKotServedCommand;
import com.atlashub.commerce.storefront.application.commands.MarkKotServed.MarkKotServedHandler;
import com.atlashub.commerce.storefront.application.commands.OccupyTable.OccupyTableCommand;
import com.atlashub.commerce.storefront.application.commands.OccupyTable.OccupyTableHandler;
import com.atlashub.commerce.storefront.application.commands.RequestTableBill.RequestTableBillCommand;
import com.atlashub.commerce.storefront.application.commands.RequestTableBill.RequestTableBillHandler;
import com.atlashub.commerce.storefront.application.commands.SendKitchenOrder.SendKitchenOrderCommand;
import com.atlashub.commerce.storefront.application.commands.SendKitchenOrder.SendKitchenOrderHandler;
import com.atlashub.commerce.storefront.application.commands.SendKitchenOrder.SendKitchenOrderResult;
import com.atlashub.commerce.storefront.application.queries.ListActiveTables.ListActiveTablesHandler;
import com.atlashub.commerce.storefront.application.queries.ListActiveTables.ListActiveTablesQuery;
import com.atlashub.commerce.storefront.application.queries.ListActiveTables.TableResult;
import com.atlashub.commerce.storefront.domain.valueobject.TableStatus;
import com.atlashub.commerce.storefront.presentation.dto.KotItemRequest;
import com.atlashub.commerce.storefront.presentation.dto.OccupyTableRequest;
import com.atlashub.commerce.storefront.presentation.dto.SendKitchenOrderRequest;
import com.atlashub.commerce.storefront.presentation.dto.SendKitchenOrderResponse;
import com.atlashub.shared.application.dto.ApiResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CommerceHospitalityControllerTest {

    @Mock
    private OccupyTableHandler occupyTableHandler;

    @Mock
    private RequestTableBillHandler requestTableBillHandler;

    @Mock
    private ClearTableHandler clearTableHandler;

    @Mock
    private ListActiveTablesHandler listActiveTablesHandler;

    @Mock
    private SendKitchenOrderHandler sendKitchenOrderHandler;

    @Mock
    private MarkKotReadyHandler markKotReadyHandler;

    @Mock
    private MarkKotServedHandler markKotServedHandler;

    @InjectMocks
    private CommerceHospitalityController controller;

    @Test
    @DisplayName("Should occupy table and return 200")
    void occupyTable_shouldReturn200() {
        OccupyTableRequest request = new OccupyTableRequest(888L, 4);

        ResponseEntity<ApiResponse<Void>> response = controller.occupyTable(12L, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().status());

        ArgumentCaptor<OccupyTableCommand> captor = ArgumentCaptor.forClass(OccupyTableCommand.class);
        verify(occupyTableHandler).execute(captor.capture());
        assertEquals(12L, captor.getValue().tableId());
        assertEquals(888L, captor.getValue().salesOrderId());
        assertEquals(4, captor.getValue().covers());
    }

    @Test
    @DisplayName("Should request bill for table and return 200")
    void requestBill_shouldReturn200() {
        ResponseEntity<ApiResponse<Void>> response = controller.requestBill(12L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().status());

        ArgumentCaptor<RequestTableBillCommand> captor = ArgumentCaptor.forClass(RequestTableBillCommand.class);
        verify(requestTableBillHandler).execute(captor.capture());
        assertEquals(12L, captor.getValue().tableId());
    }

    @Test
    @DisplayName("Should clear table and return 200")
    void clearTable_shouldReturn200() {
        ResponseEntity<ApiResponse<Void>> response = controller.clearTable(12L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().status());

        ArgumentCaptor<ClearTableCommand> captor = ArgumentCaptor.forClass(ClearTableCommand.class);
        verify(clearTableHandler).execute(captor.capture());
        assertEquals(12L, captor.getValue().tableId());
    }

    @Test
    @DisplayName("Should list active tables and return 200")
    void listActiveTables_shouldReturn200() {
        TableResult tableResult = new TableResult(12L, 10L, 20L, "T-1", 4, TableStatus.AVAILABLE, null);
        when(listActiveTablesHandler.execute(any(ListActiveTablesQuery.class)))
                .thenReturn(List.of(tableResult));

        ResponseEntity<ApiResponse<List<TableResult>>> response = controller.listActiveTables(20L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().status());
        assertEquals(1, response.getBody().data().size());

        ArgumentCaptor<ListActiveTablesQuery> captor = ArgumentCaptor.forClass(ListActiveTablesQuery.class);
        verify(listActiveTablesHandler).execute(captor.capture());
        assertEquals(20L, captor.getValue().outletId());
    }

    @Test
    @DisplayName("Should send kitchen order ticket and return 201")
    void sendKitchenOrder_shouldReturn201() {
        SendKitchenOrderRequest request = new SendKitchenOrderRequest(
                888L,
                12L,
                20L,
                List.of(new KotItemRequest(500L, "Jollof Rice", 2))
        );

        when(sendKitchenOrderHandler.execute(any(SendKitchenOrderCommand.class)))
                .thenReturn(new SendKitchenOrderResult(666L));

        ResponseEntity<ApiResponse<SendKitchenOrderResponse>> response = controller.sendKitchenOrder(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().status());
        assertEquals(666L, response.getBody().data().kotId());

        ArgumentCaptor<SendKitchenOrderCommand> captor = ArgumentCaptor.forClass(SendKitchenOrderCommand.class);
        verify(sendKitchenOrderHandler).execute(captor.capture());
        assertEquals(888L, captor.getValue().salesOrderId());
        assertEquals(12L, captor.getValue().tableId());
        assertEquals(1, captor.getValue().items().size());
    }

    @Test
    @DisplayName("Should mark KOT ready and return 200")
    void markKotReady_shouldReturn200() {
        ResponseEntity<ApiResponse<Void>> response = controller.markKotReady(666L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().status());

        ArgumentCaptor<MarkKotReadyCommand> captor = ArgumentCaptor.forClass(MarkKotReadyCommand.class);
        verify(markKotReadyHandler).execute(captor.capture());
        assertEquals(666L, captor.getValue().kotId());
    }

    @Test
    @DisplayName("Should mark KOT served and return 200")
    void markKotServed_shouldReturn200() {
        ResponseEntity<ApiResponse<Void>> response = controller.markKotServed(666L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().status());

        ArgumentCaptor<MarkKotServedCommand> captor = ArgumentCaptor.forClass(MarkKotServedCommand.class);
        verify(markKotServedHandler).execute(captor.capture());
        assertEquals(666L, captor.getValue().kotId());
    }
}
