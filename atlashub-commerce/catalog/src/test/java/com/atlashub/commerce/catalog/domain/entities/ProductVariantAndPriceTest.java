package com.atlashub.commerce.catalog.domain.entities;

import com.atlashub.commerce.catalog.domain.exceptions.InvalidProductStateException;
import com.atlashub.commerce.catalog.domain.exceptions.InvalidPurchaseOrderStateException;
import com.atlashub.commerce.catalog.domain.valueobject.PriceLevel;
import com.atlashub.shared.domain.valueobject.CurrencyCode;
import com.atlashub.shared.domain.valueobject.Money;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProductVariantAndPriceTest {

    @Test
    @DisplayName("productVariant_creationAndStateTransitions")
    void productVariant_creationAndStateTransitions() {
        ProductVariant variant = ProductVariant.create(1L, 100L, "SKU-RED-XL", Map.of("color", "red", "size", "XL"));

        assertThat(variant.getId()).isEqualTo(1L);
        assertThat(variant.getProductId()).isEqualTo(100L);
        assertThat(variant.getSku()).isEqualTo("SKU-RED-XL");
        assertThat(variant.getAttributes()).containsEntry("color", "red");
        assertThat(variant.isActive()).isTrue();

        variant.deactivate();
        assertThat(variant.isActive()).isFalse();

        variant.activate();
        assertThat(variant.isActive()).isTrue();
    }

    @Test
    @DisplayName("productPrice_calculatesMarkupCorrectly")
    void productPrice_calculatesMarkupCorrectly() {
        Money cost = new Money(BigDecimal.valueOf(1000), CurrencyCode.NGN);
        Money selling = new Money(BigDecimal.valueOf(1500), CurrencyCode.NGN);

        ProductPrice price = ProductPrice.create(1L, 100L, null, PriceLevel.RETAIL, cost, selling);

        assertThat(price.getPriceLevel()).isEqualTo(PriceLevel.RETAIL);
        // (1500 - 1000) / 1000 * 100 = 50.00%
        assertThat(price.getMarkup()).isEqualByComparingTo(BigDecimal.valueOf(50));

        Money newSelling = new Money(BigDecimal.valueOf(2000), CurrencyCode.NGN);
        price.updatePrice(cost, newSelling);
        // (2000 - 1000) / 1000 * 100 = 100.00%
        assertThat(price.getMarkup()).isEqualByComparingTo(BigDecimal.valueOf(100));
    }

    @Test
    @DisplayName("productPrice_negativePrice_throwsInvalidProductStateException")
    void productPrice_negativePrice_throwsInvalidProductStateException() {
        Money cost = new Money(BigDecimal.valueOf(1000), CurrencyCode.NGN);
        Money negativeSelling = new Money(BigDecimal.valueOf(-10), CurrencyCode.NGN);

        assertThatThrownBy(() -> ProductPrice.create(1L, 100L, null, PriceLevel.RETAIL, cost, negativeSelling))
                .isInstanceOf(InvalidProductStateException.class)
                .hasMessageContaining("Selling price cannot be negative");
    }

    @Test
    @DisplayName("customerPriceOverride_creationAndUpdate")
    void customerPriceOverride_creationAndUpdate() {
        Money initialPrice = new Money(BigDecimal.valueOf(1200), CurrencyCode.NGN);
        CustomerPriceOverride override = CustomerPriceOverride.create(1L, 100L, 500L, PriceLevel.WHOLESALE, initialPrice);

        assertThat(override.getCustomerId()).isEqualTo(500L);
        assertThat(override.getPrice().amount()).isEqualByComparingTo(BigDecimal.valueOf(1200));

        Money updatedPrice = new Money(BigDecimal.valueOf(1100), CurrencyCode.NGN);
        override.updatePrice(updatedPrice);
        assertThat(override.getPrice().amount()).isEqualByComparingTo(BigDecimal.valueOf(1100));
    }

    @Test
    @DisplayName("purchaseOrderItem_receiveValidatesQuantities")
    void purchaseOrderItem_receiveValidatesQuantities() {
        Money cost = new Money(BigDecimal.valueOf(500), CurrencyCode.NGN);
        PurchaseOrderItem item = PurchaseOrderItem.create(1L, 10L, 100L, 10, cost);

        assertThat(item.isFullyReceived()).isFalse();

        item.receive(4);
        assertThat(item.getQuantityReceived()).isEqualTo(4);
        assertThat(item.isFullyReceived()).isFalse();

        item.receive(6);
        assertThat(item.getQuantityReceived()).isEqualTo(10);
        assertThat(item.isFullyReceived()).isTrue();

        assertThatThrownBy(() -> item.receive(1))
                .isInstanceOf(InvalidPurchaseOrderStateException.class)
                .hasMessageContaining("cannot exceed ordered quantity");

        assertThatThrownBy(() -> item.receive(0))
                .isInstanceOf(InvalidPurchaseOrderStateException.class)
                .hasMessageContaining("must be greater than zero");
    }
}
