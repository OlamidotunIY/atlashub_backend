package com.atlashub.commerce.inventory.presentation.rest;

import com.atlashub.commerce.inventory.application.commands.ApproveStockTransfer.ApproveStockTransferCommand;
import com.atlashub.commerce.inventory.application.commands.ApproveStockTransfer.ApproveStockTransferHandler;
import com.atlashub.commerce.inventory.application.commands.ReceiveStockTransfer.ReceiveStockTransferCommand;
import com.atlashub.commerce.inventory.application.commands.ReceiveStockTransfer.ReceiveStockTransferHandler;
import com.atlashub.commerce.inventory.application.commands.RequestStockTransfer.RequestStockTransferCommand;
import com.atlashub.commerce.inventory.application.commands.RequestStockTransfer.RequestStockTransferHandler;
import com.atlashub.commerce.inventory.application.commands.RequestStockTransfer.RequestStockTransferResult;
import com.atlashub.commerce.inventory.application.queries.ListStockTransfers.ListStockTransfersHandler;
import com.atlashub.commerce.inventory.application.queries.ListStockTransfers.ListStockTransfersQuery;
import com.atlashub.commerce.inventory.application.queries.ListStockTransfers.StockTransferItemResult;
import com.atlashub.commerce.inventory.application.queries.ListStockTransfers.StockTransferResult;
import com.atlashub.commerce.inventory.domain.valueobject.TransferStatus;
import com.atlashub.commerce.inventory.presentation.dto.ReceiveStockTransferRequest;
import com.atlashub.commerce.inventory.presentation.dto.ReceivedTransferItemRequest;
import com.atlashub.commerce.inventory.presentation.dto.RequestStockTransferRequest;
import com.atlashub.commerce.inventory.presentation.dto.RequestStockTransferResponse;
import com.atlashub.commerce.inventory.presentation.dto.StockTransferResponse;
import com.atlashub.commerce.inventory.presentation.dto.TransferItemRequest;
import com.atlashub.shared.application.dto.ApiResponse;
import com.atlashub.shared.application.security.AuthenticatedPrincipal;
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

import java.time.ZonedDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StockTransferControllerTest {

    @Mock
    private RequestStockTransferHandler requestStockTransferHandler;

    @Mock
    private ApproveStockTransferHandler approveStockTransferHandler;

    @Mock
    private ReceiveStockTransferHandler receiveStockTransferHandler;

    @Mock
    private ListStockTransfersHandler listStockTransfersHandler;

    @InjectMocks
    private StockTransferController controller;

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
    @DisplayName("Should request stock transfer and return 201 with transfer id")
    void requestTransfer_shouldReturn201AndTransferId() {
        AuthenticatedPrincipal principal = createPrincipal();
        RequestStockTransferRequest request = new RequestStockTransferRequest(
                20L,
                30L,
                List.of(new TransferItemRequest(100L, 5))
        );
        when(requestStockTransferHandler.execute(any(RequestStockTransferCommand.class)))
                .thenReturn(new RequestStockTransferResult(88L));

        ResponseEntity<ApiResponse<RequestStockTransferResponse>> response =
                controller.requestTransfer(principal, request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().status());
        assertEquals(88L, response.getBody().data().transferId());

        ArgumentCaptor<RequestStockTransferCommand> captor = ArgumentCaptor.forClass(RequestStockTransferCommand.class);
        verify(requestStockTransferHandler).execute(captor.capture());
        assertEquals(10L, captor.getValue().organizationId());
        assertEquals(20L, captor.getValue().sourceOutletId());
        assertEquals(30L, captor.getValue().destinationOutletId());
        assertEquals(1, captor.getValue().items().size());
    }

    @Test
    @DisplayName("Should approve stock transfer and return 200")
    void approveTransfer_shouldReturn200AndInvokeHandler() {
        ResponseEntity<ApiResponse<Void>> response = controller.approveTransfer(88L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().status());

        ArgumentCaptor<ApproveStockTransferCommand> captor = ArgumentCaptor.forClass(ApproveStockTransferCommand.class);
        verify(approveStockTransferHandler).execute(captor.capture());
        assertEquals(88L, captor.getValue().transferId());
    }

    @Test
    @DisplayName("Should receive stock transfer and return 200")
    void receiveTransfer_shouldReturn200AndInvokeHandler() {
        ReceiveStockTransferRequest request = new ReceiveStockTransferRequest(List.of(
                new ReceivedTransferItemRequest(100L, 5)
        ));

        ResponseEntity<ApiResponse<Void>> response = controller.receiveTransfer(88L, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().status());

        ArgumentCaptor<ReceiveStockTransferCommand> captor = ArgumentCaptor.forClass(ReceiveStockTransferCommand.class);
        verify(receiveStockTransferHandler).execute(captor.capture());
        assertEquals(88L, captor.getValue().transferId());
        assertEquals(1, captor.getValue().receivedItems().size());
        assertEquals(5, captor.getValue().receivedItems().getFirst().quantityReceived());
    }

    @Test
    @DisplayName("Should list stock transfers with pagination")
    void listTransfers_shouldReturn200AndPageResult() {
        AuthenticatedPrincipal principal = createPrincipal();
        StockTransferItemResult itemResult = new StockTransferItemResult(1L, 100L, 5, 5);
        StockTransferResult transferResult = new StockTransferResult(
                88L, 10L, 20L, 30L, TransferStatus.RECEIVED, List.of(itemResult), ZonedDateTime.now(), ZonedDateTime.now()
        );
        PageResult<StockTransferResult> pageResult = new PageResult<>(
                List.of(transferResult), 0, 20, 1L, 1
        );
        when(listStockTransfersHandler.execute(any(ListStockTransfersQuery.class))).thenReturn(pageResult);

        ResponseEntity<ApiResponse<PageResult<StockTransferResponse>>> response =
                controller.listTransfers(principal, TransferStatus.RECEIVED, 0, 20);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().status());
        assertEquals(1, response.getBody().data().content().size());
        assertEquals(88L, response.getBody().data().content().getFirst().transferId());
    }
}
