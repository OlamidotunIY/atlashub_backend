package com.atlashub.commerce.catalog.domain.entities;

import com.atlashub.commerce.catalog.domain.exceptions.InvalidProductStateException;
import lombok.Getter;

import java.util.Collections;
import java.util.Map;

@Getter
public class ProductVariant {

    private final Long id;
    private final Long productId;
    private final String sku;
    private final Map<String, String> attributes;
    private boolean active;

    public ProductVariant(Long id, Long productId, String sku, Map<String, String> attributes, boolean active) {
        this.id = id;
        this.productId = productId;
        this.sku = sku;
        this.attributes = attributes != null ? Map.copyOf(attributes) : Collections.emptyMap();
        this.active = active;
        validateInvariants();
    }

    public static ProductVariant create(Long id, Long productId, String sku, Map<String, String> attributes) {
        return new ProductVariant(id, productId, sku, attributes, true);
    }

    public void activate() {
        this.active = true;
    }

    public void deactivate() {
        this.active = false;
    }

    private void validateInvariants() {
        if (id == null) {
            throw new InvalidProductStateException("Variant id cannot be null");
        }
        if (productId == null) {
            throw new InvalidProductStateException("Product id cannot be null for variant");
        }
        if (sku == null || sku.isBlank()) {
            throw new InvalidProductStateException("Variant SKU is required");
        }
    }
}
