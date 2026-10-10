package com.atlashub.commerce.storefront.domain.entities;

import com.atlashub.commerce.storefront.domain.exceptions.InvalidOrderStateException;
import lombok.Getter;

import java.util.Objects;

@Getter
public class KotItem {

    private final Long id;
    private final Long kotId;
    private final Long productId;
    private final String name;
    private final Integer quantity;

    public KotItem(Long id, Long kotId, Long productId, String name, Integer quantity) {
        this.id = id;
        this.kotId = kotId;
        this.productId = productId;
        this.name = name;
        this.quantity = quantity;
    }

    public static KotItem create(Long id, Long kotId, Long productId, String name, Integer quantity) {
        Objects.requireNonNull(productId, "Product ID must not be null");
        Objects.requireNonNull(name, "Item name must not be null");
        if (quantity == null || quantity <= 0) {
            throw new InvalidOrderStateException("KOT item quantity must be greater than zero");
        }
        return new KotItem(id, kotId, productId, name, quantity);
    }
}
