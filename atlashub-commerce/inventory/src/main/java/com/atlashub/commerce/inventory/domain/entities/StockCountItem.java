package com.atlashub.commerce.inventory.domain.entities;

import com.atlashub.commerce.inventory.domain.exceptions.InvalidInventoryOperationException;
import lombok.Getter;

import java.util.Objects;

@Getter
public class StockCountItem {

    private final Long id;
    private final Long stockCountId;
    private final Long inventoryId;
    private final Long productId;
    private final Integer systemQty;
    private Integer countedQty;

    public StockCountItem(Long id, Long stockCountId, Long inventoryId, Long productId,
                          Integer systemQty, Integer countedQty) {
        this.id = id;
        this.stockCountId = stockCountId;
        this.inventoryId = inventoryId;
        this.productId = productId;
        this.systemQty = systemQty;
        this.countedQty = countedQty;
    }

    public static StockCountItem create(Long id, Long stockCountId, Long inventoryId, Long productId, int systemQty) {
        Objects.requireNonNull(id, "Item ID must not be null");
        Objects.requireNonNull(stockCountId, "StockCount ID must not be null");
        Objects.requireNonNull(inventoryId, "Inventory ID must not be null");
        Objects.requireNonNull(productId, "Product ID must not be null");

        return new StockCountItem(id, stockCountId, inventoryId, productId, systemQty, null);
    }

    public void recordCount(int counted) {
        if (counted < 0) {
            throw new InvalidInventoryOperationException("Counted quantity cannot be negative");
        }
        this.countedQty = counted;
    }

    public Long getId() {
        return id;
    }
}
