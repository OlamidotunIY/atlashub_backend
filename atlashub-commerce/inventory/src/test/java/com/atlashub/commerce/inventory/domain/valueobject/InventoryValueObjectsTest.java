package com.atlashub.commerce.inventory.domain.valueobject;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class InventoryValueObjectsTest {

    @Test
    @DisplayName("AdjustmentReason enum constants should be defined")
    void adjustmentReason_constantsAreDefined() {
        assertNotNull(AdjustmentReason.DAMAGE);
        assertNotNull(AdjustmentReason.EXPIRY);
        assertNotNull(AdjustmentReason.THEFT);
        assertNotNull(AdjustmentReason.CORRECTION);
        assertNotNull(AdjustmentReason.INITIAL_COUNT);
    }

    @Test
    @DisplayName("StockCountStatus enum constants should be defined")
    void stockCountStatus_constantsAreDefined() {
        assertNotNull(StockCountStatus.OPEN);
        assertNotNull(StockCountStatus.RECONCILED);
    }

    @Test
    @DisplayName("TransferStatus enum constants should be defined")
    void transferStatus_constantsAreDefined() {
        assertNotNull(TransferStatus.REQUESTED);
        assertNotNull(TransferStatus.APPROVED);
        assertNotNull(TransferStatus.DISPATCHED);
        assertNotNull(TransferStatus.RECEIVED);
        assertNotNull(TransferStatus.CANCELLED);
    }

    @Test
    @DisplayName("ReturnStatus enum constants should be defined")
    void returnStatus_constantsAreDefined() {
        assertNotNull(ReturnStatus.PENDING);
        assertNotNull(ReturnStatus.APPROVED);
        assertNotNull(ReturnStatus.REFUNDED);
        assertNotNull(ReturnStatus.REJECTED);
    }

    @Test
    @DisplayName("RefundMethod enum constants should be defined")
    void refundMethod_constantsAreDefined() {
        assertNotNull(RefundMethod.CASH);
        assertNotNull(RefundMethod.CREDIT_NOTE);
        assertNotNull(RefundMethod.WALLET);
    }

    @Test
    @DisplayName("ReservationStatus enum constants should be defined")
    void reservationStatus_constantsAreDefined() {
        assertNotNull(ReservationStatus.ACTIVE);
        assertNotNull(ReservationStatus.FULFILLED);
        assertNotNull(ReservationStatus.RELEASED);
        assertNotNull(ReservationStatus.FAILED);
    }
}
