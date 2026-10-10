package com.atlashub.commerce.inventory.application.commands;

import com.atlashub.commerce.inventory.application.commands.ReleaseReservedStock.ReleaseReservedStockCommand;
import com.atlashub.commerce.inventory.application.commands.ReleaseReservedStock.ReleaseReservedStockHandler;
import com.atlashub.commerce.inventory.domain.entities.Inventory;
import com.atlashub.commerce.inventory.domain.entities.StockReservation;
import com.atlashub.commerce.inventory.domain.entities.StockReservationItem;
import com.atlashub.commerce.inventory.domain.repositories.InventoryRepository;
import com.atlashub.commerce.inventory.domain.repositories.StockReservationRepository;
import com.atlashub.commerce.inventory.domain.valueobject.ReservationStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReleaseReservedStockHandlerTest {

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private StockReservationRepository stockReservationRepository;

    private ReleaseReservedStockHandler handler;

    @BeforeEach
    void setUp() {
        handler = new ReleaseReservedStockHandler(inventoryRepository, stockReservationRepository);
    }

    @Test
    @DisplayName("Should release reserved stock and mark reservation as released")
    void shouldReleaseReservedStockSuccessfully() {
        Inventory inventory = Inventory.create(1L, 10L, 20L, 100L, null, 10, 2, 1);
        inventory.reserveStock(3);

        StockReservationItem item = StockReservationItem.create(1L, 1001L, 100L, 3);
        StockReservation reservation = StockReservation.create(1001L, 500L, 10L, 20L, List.of(item));

        when(stockReservationRepository.findBySalesOrderId(500L)).thenReturn(Optional.of(reservation));
        when(inventoryRepository.findByOrganizationIdAndOutletIdAndProductId(10L, 20L, 100L))
                .thenReturn(Optional.of(inventory));

        handler.execute(new ReleaseReservedStockCommand(500L));

        assertEquals(10, inventory.getQuantity());
        assertEquals(0, inventory.getReservedQuantity());
        assertEquals(ReservationStatus.RELEASED, reservation.getStatus());
        verify(inventoryRepository).save(inventory);
        verify(stockReservationRepository).save(reservation);
    }

    @Test
    @DisplayName("Should be idempotent when reservation is already released")
    void shouldBeIdempotentWhenReservationAlreadyReleased() {
        StockReservationItem item = StockReservationItem.create(1L, 1001L, 100L, 3);
        StockReservation reservation = StockReservation.create(1001L, 500L, 10L, 20L, List.of(item));
        reservation.release();

        when(stockReservationRepository.findBySalesOrderId(500L)).thenReturn(Optional.of(reservation));

        handler.execute(new ReleaseReservedStockCommand(500L));

        verify(inventoryRepository, never()).save(any());
        verify(stockReservationRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should do nothing when reservation is not found")
    void shouldDoNothingWhenReservationNotFound() {
        when(stockReservationRepository.findBySalesOrderId(500L)).thenReturn(Optional.empty());

        handler.execute(new ReleaseReservedStockCommand(500L));

        verify(inventoryRepository, never()).save(any());
        verify(stockReservationRepository, never()).save(any());
    }
}
