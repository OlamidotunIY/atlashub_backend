package com.atlashub.commerce.inventory.domain.exceptions;

import com.atlashub.shared.domain.exception.NotFoundException;

public class InventoryNotFoundException extends NotFoundException {

    public InventoryNotFoundException(Long id) {
        super("Inventory not found: " + id);
    }

    public InventoryNotFoundException(Long productId, Long outletId) {
        super("Inventory not found for product " + productId + " at outlet " + outletId);
    }
}
