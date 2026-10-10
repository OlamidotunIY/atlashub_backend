package com.atlashub.commerce.inventory.application.commands.ReserveStockForOrder;

import com.atlashub.commerce.inventory.domain.valueobject.ReservationStatus;

public record ReserveStockForOrderResult(
        Long reservationId,
        ReservationStatus status,
        boolean success,
        String message
) {
}
