package com.atlashub.commerce.catalog.domain.entities;

import com.atlashub.commerce.catalog.domain.exceptions.DiscountExpiredException;
import com.atlashub.commerce.catalog.domain.exceptions.DiscountMaxUsesReachedException;
import com.atlashub.commerce.catalog.domain.exceptions.DiscountMinimumNotMetException;
import com.atlashub.commerce.catalog.domain.exceptions.InvalidProductStateException;
import com.atlashub.commerce.catalog.domain.valueobject.DiscountScope;
import com.atlashub.commerce.catalog.domain.valueobject.DiscountType;
import com.atlashub.shared.domain.entities.AggregateRoot;
import com.atlashub.shared.domain.valueobject.Money;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
public class Discount extends AggregateRoot<Long> {

    private final Long id;
    private final Long organizationId;
    private String name;
    private final DiscountType type;
    private final BigDecimal value;
    private final DiscountScope scope;
    private final Money minOrderAmount;
    private final Integer maxUses;
    private int usedCount;
    private LocalDate validFrom;
    private LocalDate validTo;
    private boolean active;

    public Discount(Long id, Long organizationId, String name, DiscountType type,
                    BigDecimal value, DiscountScope scope, Money minOrderAmount,
                    Integer maxUses, int usedCount, LocalDate validFrom, LocalDate validTo, boolean active) {
        this.id = id;
        this.organizationId = organizationId;
        this.name = name;
        this.type = type;
        this.value = value;
        this.scope = scope;
        this.minOrderAmount = minOrderAmount;
        this.maxUses = maxUses;
        this.usedCount = usedCount;
        this.validFrom = validFrom;
        this.validTo = validTo;
        this.active = active;
        validateInvariants();
    }

    public static Discount create(Long id, Long organizationId, String name, DiscountType type,
                                  BigDecimal value, DiscountScope scope, Money minOrderAmount,
                                  Integer maxUses, LocalDate validFrom, LocalDate validTo) {
        return new Discount(id, organizationId, name, type, value, scope, minOrderAmount,
                maxUses, 0, validFrom, validTo, true);
    }

    public BigDecimal applyTo(Money orderTotal, LocalDate currentDate) {
        if (!this.active) {
            throw new InvalidProductStateException("Discount is inactive");
        }
        LocalDate date = currentDate != null ? currentDate : LocalDate.now();
        if (date.isBefore(this.validFrom) || (this.validTo != null && date.isAfter(this.validTo))) {
            throw new DiscountExpiredException();
        }
        if (this.maxUses != null && this.usedCount >= this.maxUses) {
            throw new DiscountMaxUsesReachedException();
        }
        if (this.minOrderAmount != null && orderTotal != null &&
                orderTotal.amount().compareTo(this.minOrderAmount.amount()) < 0) {
            throw new DiscountMinimumNotMetException(this.minOrderAmount);
        }
        this.usedCount++;

        if (this.type == DiscountType.PERCENTAGE) {
            if (orderTotal == null) {
                return BigDecimal.ZERO;
            }
            return orderTotal.amount().multiply(this.value).divide(BigDecimal.valueOf(100));
        } else {
            return this.value;
        }
    }

    public BigDecimal applyTo(Money orderTotal) {
        return applyTo(orderTotal, LocalDate.now());
    }

    public void deactivate() {
        this.active = false;
    }

    public void reactivate() {
        this.active = true;
    }

    public void updateValidity(LocalDate validFrom, LocalDate validTo) {
        if (validFrom == null || (validTo != null && validTo.isBefore(validFrom))) {
            throw new InvalidProductStateException("Invalid validity dates");
        }
        this.validFrom = validFrom;
        this.validTo = validTo;
    }

    private void validateInvariants() {
        if (id == null) {
            throw new InvalidProductStateException("Discount id cannot be null");
        }
        if (organizationId == null) {
            throw new InvalidProductStateException("Organization id cannot be null");
        }
        if (name == null || name.isBlank()) {
            throw new InvalidProductStateException("Discount name is required");
        }
        if (type == null) {
            throw new InvalidProductStateException("Discount type is required");
        }
        if (value == null || value.signum() < 0) {
            throw new InvalidProductStateException("Discount value cannot be null or negative");
        }
        if (type == DiscountType.PERCENTAGE && value.compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new InvalidProductStateException("Percentage discount cannot exceed 100%");
        }
        if (scope == null) {
            throw new InvalidProductStateException("Discount scope is required");
        }
        if (validFrom == null) {
            throw new InvalidProductStateException("Valid from date is required");
        }
        if (validTo != null && validTo.isBefore(validFrom)) {
            throw new InvalidProductStateException("Valid to date cannot be before valid from date");
        }
        if (maxUses != null && maxUses <= 0) {
            throw new InvalidProductStateException("Max uses must be greater than zero when specified");
        }
        if (usedCount < 0) {
            throw new InvalidProductStateException("Used count cannot be negative");
        }
    }

    @Override
    public Long getId() {
        return id;
    }
}
