package com.atlashub.commerce.inventory.presentation.rest;

import com.atlashub.commerce.inventory.application.commands.InitiateStockCount.InitiateStockCountCommand;
import com.atlashub.commerce.inventory.application.commands.InitiateStockCount.InitiateStockCountHandler;
import com.atlashub.commerce.inventory.application.commands.InitiateStockCount.InitiateStockCountResult;
import com.atlashub.commerce.inventory.application.commands.ReconcileStockCount.ReconcileStockCountCommand;
import com.atlashub.commerce.inventory.application.commands.ReconcileStockCount.ReconcileStockCountHandler;
import com.atlashub.commerce.inventory.presentation.dto.CountedItemRequest;
import com.atlashub.commerce.inventory.presentation.dto.InitiateStockCountRequest;
import com.atlashub.commerce.inventory.presentation.dto.InitiateStockCountResponse;
import com.atlashub.commerce.inventory.presentation.dto.ReconcileStockCountRequest;
import com.atlashub.shared.application.dto.ApiResponse;
import com.atlashub.shared.application.security.AuthenticatedPrincipal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.ZonedDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StockCountControllerTest {

    @Mock
    private InitiateStockCountHandler initiateStockCountHandler;

    @Mock
    private ReconcileStockCountHandler reconcileStockCountHandler;

    @InjectMocks
    private StockCountController controller;

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
    @DisplayName("Should initiate stock count and return 201 with stock count id")
    void initiateStockCount_shouldReturn201AndStockCountId() {
        AuthenticatedPrincipal principal = createPrincipal();
        InitiateStockCountRequest request = new InitiateStockCountRequest(20L);
        when(initiateStockCountHandler.execute(any(InitiateStockCountCommand.class)))
                .thenReturn(new InitiateStockCountResult(42L));

        ResponseEntity<ApiResponse<InitiateStockCountResponse>> response =
                controller.initiateStockCount(principal, request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().status());
        assertEquals(42L, response.getBody().data().stockCountId());

        ArgumentCaptor<InitiateStockCountCommand> captor = ArgumentCaptor.forClass(InitiateStockCountCommand.class);
        verify(initiateStockCountHandler).execute(captor.capture());
        assertEquals(10L, captor.getValue().organizationId());
        assertEquals(20L, captor.getValue().outletId());
        assertEquals(1L, captor.getValue().initiatedBy());
    }

    @Test
    @DisplayName("Should reconcile stock count and return 200")
    void reconcileStockCount_shouldReturn200AndInvokeHandler() {
        AuthenticatedPrincipal principal = createPrincipal();
        ReconcileStockCountRequest request = new ReconcileStockCountRequest(List.of(
                new CountedItemRequest(10L, 8)
        ));

        ResponseEntity<ApiResponse<Void>> response = controller.reconcileStockCount(principal, 42L, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().status());

        ArgumentCaptor<ReconcileStockCountCommand> captor = ArgumentCaptor.forClass(ReconcileStockCountCommand.class);
        verify(reconcileStockCountHandler).execute(captor.capture());
        assertEquals(42L, captor.getValue().stockCountId());
        assertEquals(1, captor.getValue().countedItems().size());
        assertEquals(8, captor.getValue().countedItems().getFirst().countedQty());
        assertEquals(1L, captor.getValue().reconciledBy());
    }
}
