package com.atlashub.commerce.catalog.domain.entities;

import com.atlashub.commerce.catalog.domain.exceptions.DiscountExpiredException;
import com.atlashub.commerce.catalog.domain.exceptions.DiscountMaxUsesReachedException;
import com.atlashub.commerce.catalog.domain.exceptions.DiscountMinimumNotMetException;
import com.atlashub.commerce.catalog.domain.exceptions.InvalidProductStateException;
import com.atlashub.commerce.catalog.domain.valueobject.DiscountScope;
import com.atlashub.commerce.catalog.domain.valueobject.DiscountType;
import com.atlashub.shared.domain.valueobject.CurrencyCode;
import com.atlashub.shared.domain.valueobject.Money;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DiscountTest {

    @Test
    @DisplayName("create_percentageDiscount_appliesCorrectly")
    void create_percentageDiscount_appliesCorrectly() {
        LocalDate today = LocalDate.now();
        Discount discount = Discount.create(1L, 10L, "10% OFF", DiscountType.PERCENTAGE,
                BigDecimal.valueOf(10), DiscountScope.ORDER_LEVEL, null, 100, today.minusDays(1), today.plusDays(10));

        Money orderTotal = new Money(BigDecimal.valueOf(5000), CurrencyCode.NGN);
        BigDecimal discountAmount = discount.applyTo(orderTotal, today);

        assertThat(discountAmount).isEqualByComparingTo(BigDecimal.valueOf(500));
        assertThat(discount.getUsedCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("create_flatDiscount_appliesCorrectly")
    void create_flatDiscount_appliesCorrectly() {
        LocalDate today = LocalDate.now();
        Discount discount = Discount.create(2L, 10L, "₦1000 OFF", DiscountType.FLAT_AMOUNT,
                BigDecimal.valueOf(1000), DiscountScope.ORDER_LEVEL,
                new Money(BigDecimal.valueOf(5000), CurrencyCode.NGN), 5, today.minusDays(1), today.plusDays(5));

        Money orderTotal = new Money(BigDecimal.valueOf(6000), CurrencyCode.NGN);
        BigDecimal discountAmount = discount.applyTo(orderTotal, today);

        assertThat(discountAmount).isEqualByComparingTo(BigDecimal.valueOf(1000));
        assertThat(discount.getUsedCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("applyTo_whenInactive_throwsInvalidProductStateException")
    void applyTo_whenInactive_throwsInvalidProductStateException() {
        LocalDate today = LocalDate.now();
        Discount discount = Discount.create(3L, 10L, "Promo", DiscountType.FLAT_AMOUNT,
                BigDecimal.valueOf(500), DiscountScope.ORDER_LEVEL, null, null, today, today.plusDays(1));
        discount.deactivate();

        Money orderTotal = new Money(BigDecimal.valueOf(2000), CurrencyCode.NGN);
        assertThatThrownBy(() -> discount.applyTo(orderTotal, today))
                .isInstanceOf(InvalidProductStateException.class)
                .hasMessageContaining("Discount is inactive");

        discount.reactivate();
        assertThat(discount.isActive()).isTrue();
    }

    @Test
    @DisplayName("applyTo_whenExpired_throwsDiscountExpiredException")
    void applyTo_whenExpired_throwsDiscountExpiredException() {
        LocalDate today = LocalDate.now();
        Discount discount = Discount.create(4L, 10L, "Old Promo", DiscountType.FLAT_AMOUNT,
                BigDecimal.valueOf(500), DiscountScope.ORDER_LEVEL, null, null, today.minusDays(10), today.minusDays(1));

        Money orderTotal = new Money(BigDecimal.valueOf(2000), CurrencyCode.NGN);
        assertThatThrownBy(() -> discount.applyTo(orderTotal, today))
                .isInstanceOf(DiscountExpiredException.class);
    }

    @Test
    @DisplayName("applyTo_whenMaxUsesReached_throwsDiscountMaxUsesReachedException")
    void applyTo_whenMaxUsesReached_throwsDiscountMaxUsesReachedException() {
        LocalDate today = LocalDate.now();
        Discount discount = new Discount(5L, 10L, "Single Use", DiscountType.FLAT_AMOUNT,
                BigDecimal.valueOf(500), DiscountScope.ORDER_LEVEL, null, 1, 1, today.minusDays(1), today.plusDays(1), true);

        Money orderTotal = new Money(BigDecimal.valueOf(2000), CurrencyCode.NGN);
        assertThatThrownBy(() -> discount.applyTo(orderTotal, today))
                .isInstanceOf(DiscountMaxUsesReachedException.class);
    }

    @Test
    @DisplayName("applyTo_whenMinOrderNotMet_throwsDiscountMinimumNotMetException")
    void applyTo_whenMinOrderNotMet_throwsDiscountMinimumNotMetException() {
        LocalDate today = LocalDate.now();
        Money minimum = new Money(BigDecimal.valueOf(10000), CurrencyCode.NGN);
        Discount discount = Discount.create(6L, 10L, "Big Spender", DiscountType.FLAT_AMOUNT,
                BigDecimal.valueOf(1000), DiscountScope.ORDER_LEVEL, minimum, null, today.minusDays(1), today.plusDays(1));

        Money orderTotal = new Money(BigDecimal.valueOf(5000), CurrencyCode.NGN);
        assertThatThrownBy(() -> discount.applyTo(orderTotal, today))
                .isInstanceOf(DiscountMinimumNotMetException.class);
    }
}
