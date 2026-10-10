package com.atlashub.commerce.storefront.domain.entities;

import com.atlashub.commerce.storefront.domain.exceptions.InvalidOrderStateException;
import com.atlashub.shared.domain.valueobject.Money;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.Objects;

@Getter
public class SalesOrderItem {

    private final Long id;
    private final Long salesOrderId;
    private final Long productId;
    private final Long variantId;
    private final Integer quantity;
    private final Money unitPrice;
    private final Money totalPrice;
    private final Money taxAmount;
    private final Money discountAmount;

    public SalesOrderItem(
            Long id,
            Long salesOrderId,
            Long productId,
            Long variantId,
            Integer quantity,
            Money unitPrice,
            Money totalPrice,
            Money taxAmount,
            Money discountAmount
    ) {
        this.id = id;
        this.salesOrderId = salesOrderId;
        this.productId = productId;
        this.variantId = variantId;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.totalPrice = totalPrice;
        this.taxAmount = taxAmount;
        this.discountAmount = discountAmount;
    }

    public static SalesOrderItem create(
            Long id,
            Long salesOrderId,
            Long productId,
            Long variantId,
            Integer quantity,
            Money unitPrice,
            Money taxAmount,
            Money discountAmount
    ) {
        Objects.requireNonNull(productId, "Product ID must not be null");
        Objects.requireNonNull(unitPrice, "Unit price must not be null");
        if (quantity == null || quantity <= 0) {
            throw new InvalidOrderStateException("Item quantity must be greater than zero");
        }

        Money effectiveTax = taxAmount != null ? taxAmount : Money.zero(unitPrice.currency());
        Money effectiveDiscount = discountAmount != null ? discountAmount : Money.zero(unitPrice.currency());
        Money baseTotal = unitPrice.multiply(BigDecimal.valueOf(quantity));
        Money total = baseTotal.add(effectiveTax).subtract(effectiveDiscount);

        return new SalesOrderItem(
                id,
                salesOrderId,
                productId,
                variantId,
                quantity,
                unitPrice,
                total,
                effectiveTax,
                effectiveDiscount
        );
    }
}
