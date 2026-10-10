package com.atlashub.commerce.inventory.domain.entities;

import com.atlashub.commerce.inventory.domain.exceptions.InvalidReturnStateException;
import lombok.Getter;

import java.util.Objects;

@Getter
public class ReturnItem {

    private final Long id;
    private final Long returnId;
    private final Long productId;
    private final Integer quantity;

    public ReturnItem(Long id, Long returnId, Long productId, Integer quantity) {
        this.id = id;
        this.returnId = returnId;
        this.productId = productId;
        this.quantity = quantity;
    }

    public static ReturnItem create(Long id, Long returnId, Long productId, int quantity) {
        Objects.requireNonNull(id, "Item ID must not be null");
        Objects.requireNonNull(returnId, "Return ID must not be null");
        Objects.requireNonNull(productId, "Product ID must not be null");
        if (quantity <= 0) {
            throw new InvalidReturnStateException("Return item quantity must be positive");
        }

        return new ReturnItem(id, returnId, productId, quantity);
    }

    public Long getId() {
        return id;
    }
}
