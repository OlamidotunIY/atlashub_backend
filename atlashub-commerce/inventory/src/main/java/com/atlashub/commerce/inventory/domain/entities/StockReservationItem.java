package com.atlashub.commerce.inventory.domain.entities;

import com.atlashub.commerce.inventory.domain.exceptions.InvalidInventoryOperationException;
import lombok.Getter;

import java.util.Objects;

@Getter
public class StockReservationItem {

    private final Long id;
    private final Long reservationId;
    private final Long productId;
    private final Integer quantity;

    public StockReservationItem(Long id, Long reservationId, Long productId, Integer quantity) {
        this.id = id;
        this.reservationId = reservationId;
        this.productId = productId;
        this.quantity = quantity;
    }

    public static StockReservationItem create(Long id, Long reservationId, Long productId, Integer quantity) {
        Objects.requireNonNull(id, "StockReservationItem ID must not be null");
        Objects.requireNonNull(productId, "Product ID must not be null");
        if (quantity == null || quantity <= 0) {
            throw new InvalidInventoryOperationException("Reserved quantity must be greater than zero");
        }
        return new StockReservationItem(id, reservationId, productId, quantity);
    }
}
