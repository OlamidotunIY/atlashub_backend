package com.atlashub.commerce.inventory.domain.entities;

import com.atlashub.commerce.inventory.domain.events.LowStockResolvedEvent;
import com.atlashub.commerce.inventory.domain.events.StockAdjustedEvent;
import com.atlashub.commerce.inventory.domain.exceptions.InsufficientStockException;
import com.atlashub.commerce.inventory.domain.exceptions.InvalidInventoryOperationException;
import com.atlashub.commerce.inventory.domain.valueobject.AdjustmentReason;
import com.atlashub.shared.domain.entities.AggregateRoot;
import lombok.Getter;

import java.util.Objects;

@Getter
public class Inventory extends AggregateRoot<Long> {

    private final Long id;
    private final Long organizationId;
    private final Long outletId;
    private final Long productId;
    private final Long variantId;
    private Integer quantity;
    private Integer reservedQuantity;
    private Integer reorderLevel;
    private Integer safeStock;

    public Inventory(Long id, Long organizationId, Long outletId, Long productId, Long variantId,
                     Integer quantity, Integer reservedQuantity, Integer reorderLevel, Integer safeStock) {
        this.id = id;
        this.organizationId = organizationId;
        this.outletId = outletId;
        this.productId = productId;
        this.variantId = variantId;
        this.quantity = quantity;
        this.reservedQuantity = reservedQuantity;
        this.reorderLevel = reorderLevel;
        this.safeStock = safeStock;
    }

    public static Inventory create(Long id, Long organizationId, Long outletId, Long productId,
                                   Long variantId, Integer initialQuantity, Integer reorderLevel, Integer safeStock) {
        Objects.requireNonNull(id, "Inventory ID must not be null");
        Objects.requireNonNull(organizationId, "Organization ID must not be null");
        Objects.requireNonNull(outletId, "Outlet ID must not be null");
        Objects.requireNonNull(productId, "Product ID must not be null");

        int qty = initialQuantity != null ? initialQuantity : 0;
        int reorder = reorderLevel != null ? reorderLevel : 10;
        int safe = safeStock != null ? safeStock : 5;

        if (qty < 0) {
            throw new InvalidInventoryOperationException("Initial quantity cannot be negative");
        }
        if (reorder < 0 || safe < 0) {
            throw new InvalidInventoryOperationException("Reorder level and safe stock cannot be negative");
        }

        return new Inventory(id, organizationId, outletId, productId, variantId, qty, 0, reorder, safe);
    }

    public void reserveStock(int qty) {
        if (qty <= 0) {
            throw new InvalidInventoryOperationException("Quantity to reserve must be greater than zero");
        }
        int available = quantity - reservedQuantity;
        if (available < qty) {
            throw new InsufficientStockException(productId, qty, available);
        }
        this.reservedQuantity += qty;
    }

    public void releaseReservedStock(int qty) {
        if (qty <= 0) {
            throw new InvalidInventoryOperationException("Quantity to release must be greater than zero");
        }
        if (this.reservedQuantity < qty) {
            throw new InvalidInventoryOperationException(
                    "Cannot release " + qty + " reserved stock; only " + reservedQuantity + " currently reserved"
            );
        }
        this.reservedQuantity -= qty;
    }

    public void deductStock(int qty) {
        if (qty <= 0) {
            throw new InvalidInventoryOperationException("Quantity to deduct must be greater than zero");
        }
        if (this.reservedQuantity < qty) {
            throw new InvalidInventoryOperationException(
                    "Cannot deduct " + qty + " stock; only " + reservedQuantity + " currently reserved"
            );
        }
        if (this.quantity < qty) {
            throw new InvalidInventoryOperationException(
                    "Cannot deduct " + qty + " stock; only " + quantity + " available in total"
            );
        }
        this.reservedQuantity -= qty;
        this.quantity -= qty;
    }

    public void addStock(int qty) {
        if (qty <= 0) {
            throw new InvalidInventoryOperationException("Quantity to add must be greater than zero");
        }
        boolean wasBelowReorder = this.quantity < this.reorderLevel;
        this.quantity += qty;
        if (wasBelowReorder && this.quantity >= this.reorderLevel) {
            registerEvent(LowStockResolvedEvent.of(id, organizationId, outletId, productId, this.quantity, this.reorderLevel));
        }
    }

    public void adjust(int newQty, AdjustmentReason reason, Long adjustedBy) {
        if (newQty < 0) {
            throw new InvalidInventoryOperationException("Adjusted quantity cannot be negative");
        }
        Objects.requireNonNull(reason, "AdjustmentReason must not be null");
        int previousQty = this.quantity;
        this.quantity = newQty;
        registerEvent(StockAdjustedEvent.of(
                id, organizationId, outletId, productId, previousQty, newQty, reason, adjustedBy
        ));
    }

    public boolean isLowStock() {
        return this.quantity <= this.reorderLevel;
    }

    @Override
    public Long getId() {
        return id;
    }
}
