package com.atlashub.inventory.domain.model;

import com.atlashub.shared.domain.AggregateRoot;

public class ProductInventory extends AggregateRoot<Long> {
    private Long id;
    private String productId;
    private int quantity;
    
    public ProductInventory(String productId, int initialQuantity) {
        this.productId = productId;
        this.quantity = initialQuantity;
    }

    @Override
    public Long getId() {
        return id;
    }
}
