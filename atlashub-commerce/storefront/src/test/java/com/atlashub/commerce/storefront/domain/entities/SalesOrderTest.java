package com.atlashub.commerce.storefront.domain.entities;

import com.atlashub.commerce.storefront.domain.events.LayawayCreatedEvent;
import com.atlashub.commerce.storefront.domain.events.OnlineOrderCreatedEvent;
import com.atlashub.commerce.storefront.domain.events.PosSaleCompletedEvent;
import com.atlashub.commerce.storefront.domain.events.PosSaleFailedEvent;
import com.atlashub.commerce.storefront.domain.events.PosSaleRefundedEvent;
import com.atlashub.commerce.storefront.domain.events.SalesOrderCreatedEvent;
import com.atlashub.commerce.storefront.domain.events.SalesOrderPaymentInitiatedEvent;
import com.atlashub.commerce.storefront.domain.exceptions.InvalidOrderStateException;
import com.atlashub.commerce.storefront.domain.valueobject.OrderStatus;
import com.atlashub.commerce.storefront.domain.valueobject.OrderType;
import com.atlashub.commerce.storefront.domain.valueobject.PaymentMethod;
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

class SalesOrderTest {

    private SalesOrderItem createItem(Long id, Long productId, int qty, BigDecimal unitPrice) {
        return SalesOrderItem.create(
                id,
                1L,
                productId,
                null,
                qty,
                Money.of(unitPrice, CurrencyCode.NGN),
                Money.zero(CurrencyCode.NGN),
                Money.zero(CurrencyCode.NGN)
        );
    }

    @Test
    @DisplayName("Should create sales order and register SalesOrderCreatedEvent")
    void create_shouldInitializeAndRegisterEvent() {
        SalesOrderItem item = createItem(10L, 100L, 2, new BigDecimal("5000.00"));
        SalesOrder order = SalesOrder.create(
                1L,
                10L,
                20L,
                null,
                300L,
                400L,
                50L,
                OrderType.POS_RETAIL,
                PaymentMethod.CARD,
                List.of(item),
                null
        );

        assertEquals(1L, order.getId());
        assertEquals(OrderStatus.PENDING, order.getStatus());
        assertEquals(0, order.getTotalGross().amount().compareTo(new BigDecimal("10000.00")));
        assertEquals(0, order.getTotalNet().amount().compareTo(new BigDecimal("10000.00")));
        assertEquals(1, order.getItems().size());
        assertEquals(1, order.peekDomainEvents().size());
        assertTrue(order.peekDomainEvents().getFirst() instanceof SalesOrderCreatedEvent);
    }

    @Test
    @DisplayName("Should register OnlineOrderCreatedEvent for online orders")
    void create_shouldRegisterOnlineOrderCreatedEvent_whenTypeIsOnline() {
        SalesOrderItem item = createItem(10L, 100L, 1, new BigDecimal("25000.00"));
        SalesOrder order = SalesOrder.create(
                2L,
                10L,
                20L,
                null,
                300L,
                400L,
                null,
                OrderType.ONLINE,
                PaymentMethod.CARD,
                List.of(item),
                "123 Victoria Island, Lagos"
        );

        assertEquals(OrderType.ONLINE, order.getType());
        assertEquals(2, order.peekDomainEvents().size());
        assertTrue(order.peekDomainEvents().stream().anyMatch(e -> e instanceof OnlineOrderCreatedEvent));
    }

    @Test
    @DisplayName("Should add item and recalculate totals")
    void addItem_shouldRecalculateTotals() {
        SalesOrder order = SalesOrder.create(
                1L,
                10L,
                20L,
                null,
                null,
                400L,
                50L,
                OrderType.POS_RETAIL,
                PaymentMethod.CASH,
                null,
                null
        );

        SalesOrderItem item1 = createItem(10L, 100L, 2, new BigDecimal("5000.00"));
        order.addItem(item1);

        assertEquals(1, order.getItems().size());
        assertEquals(0, order.getTotalGross().amount().compareTo(new BigDecimal("10000.00")));

        SalesOrderItem item2 = createItem(11L, 101L, 1, new BigDecimal("3000.00"));
        order.addItem(item2);

        assertEquals(2, order.getItems().size());
        assertEquals(0, order.getTotalGross().amount().compareTo(new BigDecimal("13000.00")));
        assertEquals(0, order.getTotalNet().amount().compareTo(new BigDecimal("13000.00")));
    }

    @Test
    @DisplayName("Should apply discount and recalculate net total")
    void applyDiscount_shouldReduceNetTotal() {
        SalesOrderItem item = createItem(10L, 100L, 2, new BigDecimal("5000.00"));
        SalesOrder order = SalesOrder.create(
                1L,
                10L,
                20L,
                null,
                null,
                400L,
                50L,
                OrderType.POS_RETAIL,
                PaymentMethod.CASH,
                List.of(item),
                null
        );

        order.applyDiscount(99L, Money.of(new BigDecimal("1500.00"), CurrencyCode.NGN));

        assertEquals(99L, order.getDiscountId());
        assertEquals(0, order.getTotalDiscount().amount().compareTo(new BigDecimal("1500.00")));
        assertEquals(0, order.getTotalNet().amount().compareTo(new BigDecimal("8500.00")));
    }

    @Test
    @DisplayName("Should initiate payment and register SalesOrderPaymentInitiatedEvent")
    void initiatePayment_shouldTransitionToPaymentPendingAndRegisterEvent() {
        SalesOrderItem item = createItem(10L, 100L, 1, new BigDecimal("5000.00"));
        SalesOrder order = SalesOrder.create(
                1L,
                10L,
                20L,
                null,
                null,
                400L,
                50L,
                OrderType.POS_RETAIL,
                PaymentMethod.CARD,
                List.of(item),
                null
        );

        order.initiatePayment("chg-ref-123");

        assertEquals(OrderStatus.PAYMENT_PENDING, order.getStatus());
        assertEquals("chg-ref-123", order.getChargeReference());
        assertTrue(order.peekDomainEvents().stream().anyMatch(e -> e instanceof SalesOrderPaymentInitiatedEvent));
    }

    @Test
    @DisplayName("Should complete payment and register PosSaleCompletedEvent")
    void completePayment_shouldTransitionToCompletedAndRegisterEvent() {
        SalesOrderItem item = createItem(10L, 100L, 1, new BigDecimal("5000.00"));
        SalesOrder order = SalesOrder.create(
                1L,
                10L,
                20L,
                null,
                null,
                400L,
                50L,
                OrderType.POS_RETAIL,
                PaymentMethod.CARD,
                List.of(item),
                null
        );

        order.initiatePayment("chg-ref-123");
        order.completePayment();

        assertEquals(OrderStatus.COMPLETED, order.getStatus());
        assertTrue(order.peekDomainEvents().stream().anyMatch(e -> e instanceof PosSaleCompletedEvent));
    }

    @Test
    @DisplayName("Should complete cash payment directly from PENDING")
    void completePayment_cash_shouldCompleteFromPending() {
        SalesOrderItem item = createItem(10L, 100L, 1, new BigDecimal("5000.00"));
        SalesOrder order = SalesOrder.create(
                1L,
                10L,
                20L,
                null,
                null,
                400L,
                50L,
                OrderType.POS_RETAIL,
                PaymentMethod.CASH,
                List.of(item),
                null
        );

        order.completePayment();

        assertEquals(OrderStatus.COMPLETED, order.getStatus());
        assertTrue(order.peekDomainEvents().stream().anyMatch(e -> e instanceof PosSaleCompletedEvent));
    }

    @Test
    @DisplayName("Should fail payment and register PosSaleFailedEvent")
    void failPayment_shouldTransitionToFailedAndRegisterEvent() {
        SalesOrderItem item = createItem(10L, 100L, 1, new BigDecimal("5000.00"));
        SalesOrder order = SalesOrder.create(
                1L,
                10L,
                20L,
                null,
                null,
                400L,
                50L,
                OrderType.POS_RETAIL,
                PaymentMethod.CARD,
                List.of(item),
                null
        );

        order.initiatePayment("chg-ref-123");
        order.failPayment("Insufficient funds");

        assertEquals(OrderStatus.FAILED, order.getStatus());
        assertTrue(order.peekDomainEvents().stream().anyMatch(e -> e instanceof PosSaleFailedEvent));
    }

    @Test
    @DisplayName("Should refund completed order and register PosSaleRefundedEvent")
    void refund_shouldTransitionToRefundedAndRegisterEvent() {
        SalesOrderItem item = createItem(10L, 100L, 1, new BigDecimal("5000.00"));
        SalesOrder order = SalesOrder.create(
                1L,
                10L,
                20L,
                null,
                null,
                400L,
                50L,
                OrderType.POS_RETAIL,
                PaymentMethod.CASH,
                List.of(item),
                null
        );

        order.completePayment();
        order.refund("Customer returned product", null, null);

        assertEquals(OrderStatus.REFUNDED, order.getStatus());
        assertTrue(order.peekDomainEvents().stream().anyMatch(e -> e instanceof PosSaleRefundedEvent));
    }

    @Test
    @DisplayName("Should throw exception when mutating order in non-PENDING status")
    void addItem_shouldThrowException_whenNotPending() {
        SalesOrderItem item = createItem(10L, 100L, 1, new BigDecimal("5000.00"));
        SalesOrder order = SalesOrder.create(
                1L,
                10L,
                20L,
                null,
                null,
                400L,
                50L,
                OrderType.POS_RETAIL,
                PaymentMethod.CASH,
                List.of(item),
                null
        );

        order.completePayment();

        SalesOrderItem newItem = createItem(11L, 101L, 1, new BigDecimal("2000.00"));
        assertThrows(InvalidOrderStateException.class, () -> order.addItem(newItem));
        assertThrows(InvalidOrderStateException.class, () -> order.applyDiscount(1L, Money.zero(CurrencyCode.NGN)));
        assertThrows(InvalidOrderStateException.class, () -> order.initiatePayment("ref"));
    }

    @Test
    @DisplayName("Should convert to layaway and register LayawayCreatedEvent")
    void convertToLayaway_shouldTransitionToLayawayAndRegisterEvent() {
        SalesOrderItem item = createItem(10L, 100L, 1, new BigDecimal("50000.00"));
        SalesOrder order = SalesOrder.create(
                1L,
                10L,
                20L,
                null,
                null,
                400L,
                50L,
                OrderType.POS_RETAIL,
                PaymentMethod.LAYAWAY,
                List.of(item),
                null
        );

        order.convertToLayaway(77L, Money.of(new BigDecimal("10000.00"), CurrencyCode.NGN));

        assertEquals(OrderStatus.LAYAWAY, order.getStatus());
        assertTrue(order.peekDomainEvents().stream().anyMatch(e -> e instanceof LayawayCreatedEvent));
    }
}
