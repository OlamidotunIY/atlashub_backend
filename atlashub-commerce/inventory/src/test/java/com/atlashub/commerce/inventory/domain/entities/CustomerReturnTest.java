package com.atlashub.commerce.inventory.domain.entities;

import com.atlashub.commerce.inventory.domain.events.CustomerReturnApprovedEvent;
import com.atlashub.commerce.inventory.domain.exceptions.InvalidReturnStateException;
import com.atlashub.commerce.inventory.domain.valueobject.RefundMethod;
import com.atlashub.commerce.inventory.domain.valueobject.ReturnStatus;
import com.atlashub.shared.domain.event.DomainEvent;
import com.atlashub.shared.domain.valueobject.CurrencyCode;
import com.atlashub.shared.domain.valueobject.Money;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CustomerReturnTest {

    @Test
    @DisplayName("Should create customer return with status PENDING")
    void create_initializesCorrectly() {
        CustomerReturn ret = CustomerReturn.create(
                1L, 10L, 20L, 30L, 40L,
                Money.of(new BigDecimal("1500.00"), CurrencyCode.NGN),
                "Damaged item",
                RefundMethod.WALLET
        );

        assertEquals(1L, ret.getId());
        assertEquals(10L, ret.getOrganizationId());
        assertEquals(20L, ret.getOutletId());
        assertEquals(30L, ret.getSalesOrderId());
        assertEquals(40L, ret.getCustomerId());
        assertEquals(ReturnStatus.PENDING, ret.getStatus());
        assertEquals(RefundMethod.WALLET, ret.getRefundMethod());
        assertEquals("Damaged item", ret.getReason());
        assertTrue(ret.getItems().isEmpty());
        assertNotNull(ret.getCreatedAt());
    }

    @Test
    @DisplayName("Should approve return and emit CustomerReturnApprovedEvent")
    void approve_whenPendingWithItems_transitionsToApprovedAndEmitsEvent() {
        CustomerReturn ret = CustomerReturn.create(
                1L, 10L, 20L, 30L, 40L,
                Money.of(new BigDecimal("1500.00"), CurrencyCode.NGN),
                "Damaged item",
                RefundMethod.WALLET
        );
        ret.addItem(100L, 500L, 2);

        ret.approve();

        assertEquals(ReturnStatus.APPROVED, ret.getStatus());
        List<DomainEvent<?>> events = ret.pullDomainEvents();
        assertEquals(1, events.size());
        assertTrue(events.get(0) instanceof CustomerReturnApprovedEvent);
    }

    @Test
    @DisplayName("Should throw exception when approving empty return")
    void approve_whenEmpty_throwsException() {
        CustomerReturn ret = CustomerReturn.create(
                1L, 10L, 20L, 30L, 40L,
                Money.of(new BigDecimal("1500.00"), CurrencyCode.NGN),
                "Damaged item",
                RefundMethod.WALLET
        );

        assertThrows(InvalidReturnStateException.class, ret::approve);
    }

    @Test
    @DisplayName("Should reject return with reason")
    void reject_whenPending_transitionsToRejected() {
        CustomerReturn ret = CustomerReturn.create(
                1L, 10L, 20L, 30L, 40L,
                Money.of(new BigDecimal("1500.00"), CurrencyCode.NGN),
                "Damaged item",
                RefundMethod.WALLET
        );
        ret.addItem(100L, 500L, 1);

        ret.reject("Item used beyond return window");

        assertEquals(ReturnStatus.REJECTED, ret.getStatus());
        assertEquals("Item used beyond return window", ret.getReason());
    }

    @Test
    @DisplayName("Should throw exception when mutating already approved return")
    void mutate_whenApproved_throwsException() {
        CustomerReturn ret = CustomerReturn.create(
                1L, 10L, 20L, 30L, 40L,
                Money.of(new BigDecimal("1500.00"), CurrencyCode.NGN),
                "Damaged item",
                RefundMethod.WALLET
        );
        ret.addItem(100L, 500L, 1);
        ret.approve();

        assertThrows(InvalidReturnStateException.class, ret::approve);
        assertThrows(InvalidReturnStateException.class, () -> ret.reject("reason"));
        assertThrows(InvalidReturnStateException.class, () -> ret.addItem(101L, 501L, 1));
    }
}
