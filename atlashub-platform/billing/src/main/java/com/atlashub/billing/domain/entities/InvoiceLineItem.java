package com.atlashub.billing.domain.entities;

import com.atlashub.billing.domain.exceptions.BillingDomainException;
import com.atlashub.billing.domain.valueobject.LineItemType;
import com.atlashub.shared.domain.entities.AggregateRoot;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class InvoiceLineItem extends AggregateRoot<Long> {

    private final Long id;
    private final Long invoiceId;
    private final String description;
    private final Integer quantity;
    private final BigDecimal unitPrice;
    private final BigDecimal totalAmount;
    private final LineItemType lineItemType;

    public InvoiceLineItem(
            Long id,
            Long invoiceId,
            String description,
            Integer quantity,
            BigDecimal unitPrice,
            BigDecimal totalAmount,
            LineItemType lineItemType
    ) {
        this.id = id;
        this.invoiceId = invoiceId;
        this.description = description;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.totalAmount = totalAmount;
        this.lineItemType = lineItemType;
    }

    public static InvoiceLineItem create(
            Long id,
            Long invoiceId,
            String description,
            Integer quantity,
            BigDecimal unitPrice,
            LineItemType lineItemType
    ) {
        if (description == null || description.isBlank()) {
            throw new BillingDomainException("Description cannot be blank");
        }
        if (quantity == null || quantity <= 0) {
            throw new BillingDomainException("Quantity must be greater than zero");
        }
        if (unitPrice == null || unitPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new BillingDomainException("Unit price cannot be negative");
        }
        if (lineItemType == null) {
            throw new BillingDomainException("Line item type cannot be null");
        }

        BigDecimal total = unitPrice.multiply(BigDecimal.valueOf(quantity));
        return new InvoiceLineItem(
                id,
                invoiceId,
                description,
                quantity,
                unitPrice,
                total,
                lineItemType
        );
    }

    @Override
    public Long getId() {
        return id;
    }
}
