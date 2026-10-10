package com.atlashub.commerce.storefront.domain.valueobject;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class StorefrontValueObjectsTest {

    @Test
    @DisplayName("Should verify OrderType enum values")
    void orderType_values() {
        assertEquals(6, OrderType.values().length);
        assertNotNull(OrderType.valueOf("POS_RETAIL"));
        assertNotNull(OrderType.valueOf("ONLINE"));
    }

    @Test
    @DisplayName("Should verify OrderStatus enum values")
    void orderStatus_values() {
        assertEquals(7, OrderStatus.values().length);
        assertNotNull(OrderStatus.valueOf("PENDING"));
        assertNotNull(OrderStatus.valueOf("COMPLETED"));
    }

    @Test
    @DisplayName("Should verify PaymentMethod enum values")
    void paymentMethod_values() {
        assertEquals(6, PaymentMethod.values().length);
        assertNotNull(PaymentMethod.valueOf("CASH"));
        assertNotNull(PaymentMethod.valueOf("CARD"));
    }

    @Test
    @DisplayName("Should verify TableStatus enum values")
    void tableStatus_values() {
        assertEquals(4, TableStatus.values().length);
        assertNotNull(TableStatus.valueOf("AVAILABLE"));
        assertNotNull(TableStatus.valueOf("OCCUPIED"));
    }

    @Test
    @DisplayName("Should verify KotStatus enum values")
    void kotStatus_values() {
        assertEquals(4, KotStatus.values().length);
        assertNotNull(KotStatus.valueOf("PENDING"));
        assertNotNull(KotStatus.valueOf("READY"));
    }

    @Test
    @DisplayName("Should verify TillStatus enum values")
    void tillStatus_values() {
        assertEquals(2, TillStatus.values().length);
        assertNotNull(TillStatus.valueOf("OPEN"));
        assertNotNull(TillStatus.valueOf("CLOSED"));
    }

    @Test
    @DisplayName("Should verify CreditStatus enum values")
    void creditStatus_values() {
        assertEquals(4, CreditStatus.values().length);
        assertNotNull(CreditStatus.valueOf("WITHIN_LIMIT"));
        assertNotNull(CreditStatus.valueOf("SETTLED"));
    }

    @Test
    @DisplayName("Should verify DepositStatus enum values")
    void depositStatus_values() {
        assertEquals(3, DepositStatus.values().length);
        assertNotNull(DepositStatus.valueOf("ACTIVE"));
        assertNotNull(DepositStatus.valueOf("FULFILLED"));
    }

    @Test
    @DisplayName("Should verify RefundMethod enum values")
    void refundMethod_values() {
        assertEquals(4, RefundMethod.values().length);
        assertNotNull(RefundMethod.valueOf("ORIGINAL_PAYMENT"));
        assertNotNull(RefundMethod.valueOf("WALLET"));
    }
}
