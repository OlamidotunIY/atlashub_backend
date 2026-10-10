package com.atlashub.commerce.inventory.application.commands;

import com.atlashub.commerce.inventory.application.commands.ReserveStockForOrder.OrderItemDto;
import com.atlashub.commerce.inventory.application.commands.ReserveStockForOrder.ReserveStockForOrderCommand;
import com.atlashub.commerce.inventory.application.commands.ReserveStockForOrder.ReserveStockForOrderHandler;
import com.atlashub.commerce.inventory.application.commands.ReserveStockForOrder.ReserveStockForOrderResult;
import com.atlashub.commerce.inventory.domain.entities.Inventory;
import com.atlashub.commerce.inventory.domain.entities.StockReservation;
import com.atlashub.commerce.inventory.domain.repositories.InventoryRepository;
import com.atlashub.commerce.inventory.domain.repositories.StockReservationRepository;
import com.atlashub.commerce.inventory.domain.valueobject.ReservationStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReserveStockForOrderHandlerTest {

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private StockReservationRepository stockReservationRepository;

    private ReserveStockForOrderHandler handler;

    @BeforeEach
    void setUp() {
        handler = new ReserveStockForOrderHandler(inventoryRepository, stockReservationRepository);
    }

    @Test
    @DisplayName("Should reserve stock successfully when available")
    void shouldReserveStockSuccessfully() {
        Inventory inventory = Inventory.create(1L, 10L, 20L, 100L, null, 10, 2, 1);
        when(stockReservationRepository.findBySalesOrderId(500L)).thenReturn(Optional.empty());
        when(stockReservationRepository.nextIdentity()).thenReturn(1001L, 2001L);
        when(inventoryRepository.findByOrganizationIdAndOutletIdAndProductId(10L, 20L, 100L))
                .thenReturn(Optional.of(inventory));

        ReserveStockForOrderCommand command = new ReserveStockForOrderCommand(
                500L, 10L, 20L, List.of(new OrderItemDto(100L, null, 3))
        );

        ReserveStockForOrderResult result = handler.execute(command);

        assertTrue(result.success());
        assertEquals(ReservationStatus.ACTIVE, result.status());
        assertEquals(3, inventory.getReservedQuantity());
        verify(inventoryRepository).save(inventory);
        verify(stockReservationRepository).save(any(StockReservation.class));
    }

    @Test
    @DisplayName("Should fail reservation when stock is insufficient")
    void shouldFailReservationWhenStockInsufficient() {
        Inventory inventory = Inventory.create(1L, 10L, 20L, 100L, null, 2, 2, 1);
        when(stockReservationRepository.findBySalesOrderId(500L)).thenReturn(Optional.empty());
        when(stockReservationRepository.nextIdentity()).thenReturn(1001L, 2001L);
        when(inventoryRepository.findByOrganizationIdAndOutletIdAndProductId(10L, 20L, 100L))
                .thenReturn(Optional.of(inventory));

        ReserveStockForOrderCommand command = new ReserveStockForOrderCommand(
                500L, 10L, 20L, List.of(new OrderItemDto(100L, null, 5))
        );

        ReserveStockForOrderResult result = handler.execute(command);

        assertFalse(result.success());
        assertEquals(ReservationStatus.FAILED, result.status());
        assertEquals(0, inventory.getReservedQuantity());
        verify(inventoryRepository, never()).save(any(Inventory.class));
        verify(stockReservationRepository).save(any(StockReservation.class));
    }

    @Test
    @DisplayName("Should fail reservation when inventory record is not found")
    void shouldFailReservationWhenInventoryNotFound() {
        when(stockReservationRepository.findBySalesOrderId(500L)).thenReturn(Optional.empty());
        when(stockReservationRepository.nextIdentity()).thenReturn(1001L, 2001L);
        when(inventoryRepository.findByOrganizationIdAndOutletIdAndProductId(10L, 20L, 100L))
                .thenReturn(Optional.empty());

        ReserveStockForOrderCommand command = new ReserveStockForOrderCommand(
                500L, 10L, 20L, List.of(new OrderItemDto(100L, null, 2))
        );

        ReserveStockForOrderResult result = handler.execute(command);

        assertFalse(result.success());
        assertEquals(ReservationStatus.FAILED, result.status());
        verify(inventoryRepository, never()).save(any(Inventory.class));
        verify(stockReservationRepository).save(any(StockReservation.class));
    }

    @Test
    @DisplayName("Should return existing result when reservation already exists (idempotency)")
    void shouldBeIdempotentWhenReservationAlreadyExists() {
        StockReservation existing = StockReservation.create(1001L, 500L, 10L, 20L, Collections.emptyList());
        when(stockReservationRepository.findBySalesOrderId(500L)).thenReturn(Optional.of(existing));

        ReserveStockForOrderCommand command = new ReserveStockForOrderCommand(
                500L, 10L, 20L, List.of(new OrderItemDto(100L, null, 2))
        );

        ReserveStockForOrderResult result = handler.execute(command);

        assertTrue(result.success());
        assertEquals(ReservationStatus.ACTIVE, result.status());
        verify(inventoryRepository, never()).save(any(Inventory.class));
    }
}
