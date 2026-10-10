package com.atlashub.commerce.catalog.domain.entities;

import com.atlashub.commerce.catalog.domain.exceptions.InvalidProductStateException;
import com.atlashub.commerce.catalog.domain.valueobject.PriceLevel;
import com.atlashub.shared.domain.valueobject.Money;
import lombok.Getter;

@Getter
public class CustomerPriceOverride {

    private final Long id;
    private final Long productId;
    private final Long customerId;
    private final PriceLevel priceLevel;
    private Money price;

    public CustomerPriceOverride(Long id, Long productId, Long customerId, PriceLevel priceLevel, Money price) {
        this.id = id;
        this.productId = productId;
        this.customerId = customerId;
        this.priceLevel = priceLevel;
        this.price = price;
        validateInvariants();
    }

    public static CustomerPriceOverride create(Long id, Long productId, Long customerId, PriceLevel priceLevel, Money price) {
        return new CustomerPriceOverride(id, productId, customerId, priceLevel, price);
    }

    public void updatePrice(Money newPrice) {
        this.price = newPrice;
        validateInvariants();
    }

    private void validateInvariants() {
        if (id == null) {
            throw new InvalidProductStateException("Customer price override id cannot be null");
        }
        if (productId == null) {
            throw new InvalidProductStateException("Product id cannot be null for price override");
        }
        if (customerId == null) {
            throw new InvalidProductStateException("Customer id is required for price override");
        }
        if (priceLevel == null) {
            throw new InvalidProductStateException("Price level is required for price override");
        }
        if (price == null || price.amount().signum() < 0) {
            throw new InvalidProductStateException("Price cannot be null or negative");
        }
    }
}
