package com.atlashub.commerce.catalog.domain.entities;

import com.atlashub.commerce.catalog.domain.exceptions.InvalidProductStateException;
import com.atlashub.commerce.catalog.domain.valueobject.PriceLevel;
import com.atlashub.shared.domain.valueobject.Money;
import lombok.Getter;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Getter
public class ProductPrice {

    private final Long id;
    private final Long productId;
    private final Long variantId;
    private final PriceLevel priceLevel;
    private Money costPrice;
    private Money sellingPrice;
    private BigDecimal markup;

    public ProductPrice(Long id, Long productId, Long variantId, PriceLevel priceLevel,
                        Money costPrice, Money sellingPrice, BigDecimal markup) {
        this.id = id;
        this.productId = productId;
        this.variantId = variantId;
        this.priceLevel = priceLevel;
        this.costPrice = costPrice;
        this.sellingPrice = sellingPrice;
        this.markup = markup;
        validateInvariants();
    }

    public static ProductPrice create(Long id, Long productId, Long variantId, PriceLevel priceLevel,
                                      Money costPrice, Money sellingPrice) {
        BigDecimal markup = calculateMarkup(costPrice, sellingPrice);
        return new ProductPrice(id, productId, variantId, priceLevel, costPrice, sellingPrice, markup);
    }

    public void updatePrice(Money newCostPrice, Money newSellingPrice) {
        this.costPrice = newCostPrice;
        this.sellingPrice = newSellingPrice;
        this.markup = calculateMarkup(newCostPrice, newSellingPrice);
        validateInvariants();
    }

    private static BigDecimal calculateMarkup(Money cost, Money selling) {
        if (cost == null || selling == null || cost.amount().signum() <= 0) {
            return BigDecimal.ZERO;
        }
        return selling.amount().subtract(cost.amount())
                .divide(cost.amount(), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
    }

    private void validateInvariants() {
        if (id == null) {
            throw new InvalidProductStateException("Price id cannot be null");
        }
        if (productId == null) {
            throw new InvalidProductStateException("Product id cannot be null for price");
        }
        if (priceLevel == null) {
            throw new InvalidProductStateException("Price level is required");
        }
        if (sellingPrice == null) {
            throw new InvalidProductStateException("Selling price is required");
        }
        if (costPrice != null && costPrice.amount().signum() < 0) {
            throw new InvalidProductStateException("Cost price cannot be negative");
        }
        if (sellingPrice.amount().signum() < 0) {
            throw new InvalidProductStateException("Selling price cannot be negative");
        }
        if (costPrice != null && costPrice.currency() != sellingPrice.currency()) {
            throw new InvalidProductStateException("Cost price and selling price currencies must match");
        }
    }
}
