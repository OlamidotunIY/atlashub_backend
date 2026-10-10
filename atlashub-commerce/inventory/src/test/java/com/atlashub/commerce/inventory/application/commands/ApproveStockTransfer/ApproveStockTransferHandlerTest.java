package com.atlashub.commerce.inventory.application.commands.ApproveStockTransfer;

import com.atlashub.commerce.inventory.domain.entities.StockTransfer;
import com.atlashub.commerce.inventory.domain.exceptions.StockTransferNotFoundException;
import com.atlashub.commerce.inventory.domain.repositories.StockTransferRepository;
import com.atlashub.commerce.inventory.domain.valueobject.TransferStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ApproveStockTransferHandlerTest {

    private StockTransferRepository stockTransferRepository;
    private ApproveStockTransferHandler handler;

    @BeforeEach
    void setUp() {
        stockTransferRepository = mock(StockTransferRepository.class);
        handler = new ApproveStockTransferHandler(stockTransferRepository);
    }

    @Test
    @DisplayName("Should approve stock transfer successfully")
    void shouldApproveStockTransferSuccessfully() {
        StockTransfer transfer = StockTransfer.create(1L, 10L, 20L, 30L);
        transfer.addItem(101L, 500L, 10);

        when(stockTransferRepository.findById(1L)).thenReturn(Optional.of(transfer));

        ApproveStockTransferCommand command = new ApproveStockTransferCommand(1L);
        Void result = handler.execute(command);

        assertNull(result);
        assertEquals(TransferStatus.APPROVED, transfer.getStatus());
        verify(stockTransferRepository).save(transfer);
    }

    @Test
    @DisplayName("Should throw StockTransferNotFoundException when transfer is missing")
    void shouldThrowWhenTransferNotFound() {
        when(stockTransferRepository.findById(1L)).thenReturn(Optional.empty());

        ApproveStockTransferCommand command = new ApproveStockTransferCommand(1L);
        assertThrows(StockTransferNotFoundException.class, () -> handler.execute(command));
    }
}
