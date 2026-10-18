package com.atlashub.logistics.warehousing.domain.entities;

import com.atlashub.logistics.warehousing.domain.events.StockTransferApprovedEvent;
import com.atlashub.logistics.warehousing.domain.events.StockTransferDispatchedEvent;
import com.atlashub.logistics.warehousing.domain.events.StockTransferReceivedEvent;
import com.atlashub.logistics.warehousing.domain.events.StockTransferRequestedEvent;
import com.atlashub.logistics.warehousing.domain.exceptions.InvalidStockTransferStateException;
import com.atlashub.logistics.warehousing.domain.valueobject.Address;
import com.atlashub.logistics.warehousing.domain.valueobject.LocationType;
import com.atlashub.logistics.warehousing.domain.valueobject.StockTransferItem;
import com.atlashub.logistics.warehousing.domain.valueobject.TransferStatus;
import com.atlashub.shared.domain.valueobject.CurrencyCode;
import com.atlashub.shared.domain.valueobject.Money;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StockTransferTest {

    private final Address origin = new Address("Store 1", "Ikeja", "Lagos", "Nigeria", "100001");
    private final Address dest = new Address("Store 2", "Yaba", "Lagos", "Nigeria", "100002");
    private final StockTransferItem item = new StockTransferItem(501L, "SKU-501", "Widget A", 10, new BigDecimal("0.5"));

    @Test
    @DisplayName("Should create requested stock transfer and calculate total weight")
    void shouldCreateStockTransfer() {
        Money fee = Money.of(new BigDecimal("3500"), CurrencyCode.NGN);
        StockTransfer transfer = StockTransfer.create(
                1L,
                100L,
                10L,
                LocationType.OUTLET,
                origin,
                20L,
                LocationType.OUTLET,
                dest,
                fee,
                List.of(item),
                55L,
                "corr-tr-1"
        );

        assertNotNull(transfer);
        assertEquals(TransferStatus.REQUESTED, transfer.getStatus());
        assertEquals(0, new BigDecimal("5.0").compareTo(transfer.getTotalWeightKg()));
        assertEquals(fee, transfer.getTransferShippingFee());

        assertFalse(transfer.peekDomainEvents().isEmpty());
        assertTrue(transfer.peekDomainEvents().get(0) instanceof StockTransferRequestedEvent);
    }

    @Test
    @DisplayName("Should approve requested transfer and register StockTransferApprovedEvent")
    void shouldApproveTransfer() {
        StockTransfer transfer = StockTransfer.create(
                1L, 100L, 10L, LocationType.OUTLET, origin,
                20L, LocationType.OUTLET, dest,
                Money.of(new BigDecimal("3500"), CurrencyCode.NGN),
                List.of(item), 55L, "corr-tr-1"
        );

        transfer.approve(99L, Money.of(new BigDecimal("25000"), CurrencyCode.NGN), "corr-tr-app");

        assertEquals(TransferStatus.APPROVED, transfer.getStatus());
        assertEquals(99L, transfer.getApprovedBy());

        boolean hasApprovedEvent = transfer.peekDomainEvents().stream()
                .anyMatch(e -> e instanceof StockTransferApprovedEvent);
        assertTrue(hasApprovedEvent);
    }

    @Test
    @DisplayName("Should dispatch, receive and complete transfer lifecycle")
    void shouldCompleteTransferLifecycle() {
        StockTransfer transfer = StockTransfer.create(
                1L, 100L, 10L, LocationType.OUTLET, origin,
                20L, LocationType.OUTLET, dest,
                Money.of(new BigDecimal("3500"), CurrencyCode.NGN),
                List.of(item), 55L, "corr-tr-1"
        );

        transfer.approve(99L, Money.of(new BigDecimal("25000"), CurrencyCode.NGN), "corr-tr-app");
        transfer.markDispatched(888L, "corr-tr-disp");

        assertEquals(TransferStatus.DISPATCHED, transfer.getStatus());
        assertEquals(888L, transfer.getShipmentId());

        ZonedDateTime now = ZonedDateTime.now();
        transfer.receive(now, "corr-tr-rec");
        assertEquals(TransferStatus.RECEIVED, transfer.getStatus());
        assertEquals(now, transfer.getReceivedAt());
    }

    @Test
    @DisplayName("Should reject illegal state transitions")
    void shouldRejectIllegalTransitions() {
        StockTransfer transfer = StockTransfer.create(
                1L, 100L, 10L, LocationType.OUTLET, origin,
                20L, LocationType.OUTLET, dest,
                Money.of(new BigDecimal("3500"), CurrencyCode.NGN),
                List.of(item), 55L, "corr-tr-1"
        );

        // Cannot dispatch before approval
        assertThrows(InvalidStockTransferStateException.class, () ->
                transfer.markDispatched(888L, "corr")
        );

        // Cannot receive before dispatch
        assertThrows(InvalidStockTransferStateException.class, () ->
                transfer.receive(ZonedDateTime.now(), "corr")
        );
    }
}
