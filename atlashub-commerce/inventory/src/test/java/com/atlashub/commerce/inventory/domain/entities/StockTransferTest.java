package com.atlashub.commerce.inventory.domain.entities;

import com.atlashub.commerce.inventory.domain.events.StockTransferApprovedEvent;
import com.atlashub.commerce.inventory.domain.events.StockTransferReceivedEvent;
import com.atlashub.commerce.inventory.domain.exceptions.InvalidTransferStateException;
import com.atlashub.commerce.inventory.domain.valueobject.TransferStatus;
import com.atlashub.shared.domain.event.DomainEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StockTransferTest {

    @Test
    @DisplayName("Should create stock transfer with status REQUESTED")
    void create_initializesCorrectly() {
        StockTransfer transfer = StockTransfer.create(1L, 10L, 20L, 30L);

        assertEquals(1L, transfer.getId());
        assertEquals(10L, transfer.getOrganizationId());
        assertEquals(20L, transfer.getSourceOutletId());
        assertEquals(30L, transfer.getDestinationOutletId());
        assertEquals(TransferStatus.REQUESTED, transfer.getStatus());
        assertTrue(transfer.getItems().isEmpty());
        assertNotNull(transfer.getRequestedAt());
        assertNull(transfer.getReceivedAt());
    }

    @Test
    @DisplayName("Should throw exception when source and destination outlets are identical")
    void create_sameOutlet_throwsException() {
        assertThrows(InvalidTransferStateException.class, () ->
                StockTransfer.create(1L, 10L, 20L, 20L)
        );
    }

    @Test
    @DisplayName("Should complete transfer lifecycle: request -> approve -> dispatch -> receive")
    void lifecycle_success() {
        StockTransfer transfer = StockTransfer.create(1L, 10L, 20L, 30L);
        transfer.addItem(100L, 500L, 10);

        assertEquals(1, transfer.getItems().size());
        assertEquals(10, transfer.getItems().get(0).getQuantityRequested());
        assertNull(transfer.getItems().get(0).getQuantityReceived());

        transfer.approve();
        assertEquals(TransferStatus.APPROVED, transfer.getStatus());
        List<DomainEvent<?>> events1 = transfer.pullDomainEvents();
        assertEquals(1, events1.size());
        assertTrue(events1.get(0) instanceof StockTransferApprovedEvent);

        transfer.dispatch();
        assertEquals(TransferStatus.DISPATCHED, transfer.getStatus());

        transfer.receive(Map.of(500L, 9));
        assertEquals(TransferStatus.RECEIVED, transfer.getStatus());
        assertNotNull(transfer.getReceivedAt());
        assertEquals(9, transfer.getItems().get(0).getQuantityReceived());
        List<DomainEvent<?>> events2 = transfer.pullDomainEvents();
        assertEquals(1, events2.size());
        assertTrue(events2.get(0) instanceof StockTransferReceivedEvent);
    }

    @Test
    @DisplayName("Should throw exception when approving empty transfer")
    void approve_whenEmpty_throwsException() {
        StockTransfer transfer = StockTransfer.create(1L, 10L, 20L, 30L);

        assertThrows(InvalidTransferStateException.class, transfer::approve);
    }

    @Test
    @DisplayName("Should allow cancellation from REQUESTED or APPROVED status")
    void cancel_validStates_cancelsTransfer() {
        StockTransfer transfer1 = StockTransfer.create(1L, 10L, 20L, 30L);
        transfer1.cancel();
        assertEquals(TransferStatus.CANCELLED, transfer1.getStatus());

        StockTransfer transfer2 = StockTransfer.create(2L, 10L, 20L, 30L);
        transfer2.addItem(100L, 500L, 5);
        transfer2.approve();
        transfer2.cancel();
        assertEquals(TransferStatus.CANCELLED, transfer2.getStatus());
    }

    @Test
    @DisplayName("Should throw exception when cancelling already dispatched transfer")
    void cancel_whenDispatched_throwsException() {
        StockTransfer transfer = StockTransfer.create(1L, 10L, 20L, 30L);
        transfer.addItem(100L, 500L, 5);
        transfer.approve();
        transfer.dispatch();

        assertThrows(InvalidTransferStateException.class, transfer::cancel);
    }
}
