package com.atlashub.commerce.catalog.domain.entities;

import com.atlashub.commerce.catalog.domain.exceptions.InvalidPurchaseOrderStateException;
import com.atlashub.shared.domain.valueobject.Money;
import lombok.Getter;

@Getter
public class PurchaseOrderItem {

    private final Long id;
    private final Long purchaseOrderId;
    private final Long productId;
    private final int quantityOrdered;
    private int quantityReceived;
    private final Money unitCost;

    public PurchaseOrderItem(Long id, Long purchaseOrderId, Long productId,
                             int quantityOrdered, int quantityReceived, Money unitCost) {
        this.id = id;
        this.purchaseOrderId = purchaseOrderId;
        this.productId = productId;
        this.quantityOrdered = quantityOrdered;
        this.quantityReceived = quantityReceived;
        this.unitCost = unitCost;
        validateInvariants();
    }

    public static PurchaseOrderItem create(Long id, Long purchaseOrderId, Long productId,
                                           int quantityOrdered, Money unitCost) {
        return new PurchaseOrderItem(id, purchaseOrderId, productId, quantityOrdered, 0, unitCost);
    }

    public void receive(int quantity) {
        if (quantity <= 0) {
            throw new InvalidPurchaseOrderStateException("Received quantity must be greater than zero");
        }
        if (this.quantityReceived + quantity > this.quantityOrdered) {
            throw new InvalidPurchaseOrderStateException(
                    "Received quantity cannot exceed ordered quantity (" + this.quantityOrdered + ")");
        }
        this.quantityReceived += quantity;
    }

    public boolean isFullyReceived() {
        return this.quantityReceived >= this.quantityOrdered;
    }

    private void validateInvariants() {
        if (id == null) {
            throw new InvalidPurchaseOrderStateException("Item id cannot be null");
        }
        if (purchaseOrderId == null) {
            throw new InvalidPurchaseOrderStateException("Purchase order id cannot be null");
        }
        if (productId == null) {
            throw new InvalidPurchaseOrderStateException("Product id is required for purchase order item");
        }
        if (quantityOrdered <= 0) {
            throw new InvalidPurchaseOrderStateException("Quantity ordered must be greater than zero");
        }
        if (quantityReceived < 0) {
            throw new InvalidPurchaseOrderStateException("Quantity received cannot be negative");
        }
        if (unitCost == null || unitCost.amount().signum() < 0) {
            throw new InvalidPurchaseOrderStateException("Unit cost cannot be null or negative");
        }
    }
}
