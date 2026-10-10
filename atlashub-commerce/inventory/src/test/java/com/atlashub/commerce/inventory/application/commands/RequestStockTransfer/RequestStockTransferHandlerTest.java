package com.atlashub.commerce.inventory.application.commands.RequestStockTransfer;

import com.atlashub.commerce.inventory.domain.entities.StockTransfer;
import com.atlashub.commerce.inventory.domain.repositories.StockTransferRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RequestStockTransferHandlerTest {

    private StockTransferRepository stockTransferRepository;
    private RequestStockTransferHandler handler;

    @BeforeEach
    void setUp() {
        stockTransferRepository = mock(StockTransferRepository.class);
        handler = new RequestStockTransferHandler(stockTransferRepository);
    }

    @Test
    @DisplayName("Should request stock transfer successfully")
    void shouldRequestStockTransferSuccessfully() {
        when(stockTransferRepository.nextIdentity()).thenReturn(10L, 101L);

        RequestStockTransferCommand command = new RequestStockTransferCommand(
                1L, 2L, 3L, List.of(new TransferItemDto(50L, 5))
        );

        RequestStockTransferResult result = handler.execute(command);
        assertNotNull(result);
        assertEquals(10L, result.transferId());
        verify(stockTransferRepository).save(any(StockTransfer.class));
    }
}
