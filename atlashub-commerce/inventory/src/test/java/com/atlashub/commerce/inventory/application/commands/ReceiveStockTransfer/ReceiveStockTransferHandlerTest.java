package com.atlashub.commerce.inventory.application.commands.ReceiveStockTransfer;

import com.atlashub.commerce.inventory.domain.entities.Inventory;
import com.atlashub.commerce.inventory.domain.entities.StockTransfer;
import com.atlashub.commerce.inventory.domain.exceptions.StockTransferNotFoundException;
import com.atlashub.commerce.inventory.domain.repositories.InventoryRepository;
import com.atlashub.commerce.inventory.domain.repositories.StockTransferRepository;
import com.atlashub.commerce.inventory.domain.valueobject.TransferStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ReceiveStockTransferHandlerTest {

    private StockTransferRepository stockTransferRepository;
    private InventoryRepository inventoryRepository;
    private ReceiveStockTransferHandler handler;

    @BeforeEach
    void setUp() {
        stockTransferRepository = mock(StockTransferRepository.class);
        inventoryRepository = mock(InventoryRepository.class);
        handler = new ReceiveStockTransferHandler(stockTransferRepository, inventoryRepository);
    }

    @Test
    @DisplayName("Should receive stock transfer and add stock to destination inventory")
    void shouldReceiveStockTransferAndAddDestinationStock() {
        StockTransfer transfer = StockTransfer.create(1L, 10L, 20L, 30L);
        transfer.addItem(101L, 500L, 10);
        transfer.approve();

        Inventory destInventory = Inventory.create(2L, 10L, 30L, 500L, null, 5, 10, 5);

        when(stockTransferRepository.findById(1L)).thenReturn(Optional.of(transfer));
        when(inventoryRepository.findByOrganizationIdAndOutletIdAndProductId(10L, 30L, 500L))
                .thenReturn(Optional.of(destInventory));

        ReceiveStockTransferCommand command = new ReceiveStockTransferCommand(
                1L, List.of(new ReceivedTransferItemDto(500L, 10))
        );

        Void result = handler.execute(command);
        assertNull(result);
        assertEquals(TransferStatus.RECEIVED, transfer.getStatus());
        assertEquals(15, destInventory.getQuantity());

        verify(inventoryRepository).save(destInventory);
        verify(stockTransferRepository).save(transfer);
    }

    @Test
    @DisplayName("Should throw StockTransferNotFoundException when transfer not found")
    void shouldThrowWhenStockTransferNotFound() {
        when(stockTransferRepository.findById(1L)).thenReturn(Optional.empty());

        ReceiveStockTransferCommand command = new ReceiveStockTransferCommand(1L, List.of());
        assertThrows(StockTransferNotFoundException.class, () -> handler.execute(command));
    }
}
