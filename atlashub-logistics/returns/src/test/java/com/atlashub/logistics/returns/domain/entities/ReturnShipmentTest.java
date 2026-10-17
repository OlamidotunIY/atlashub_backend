package com.atlashub.logistics.returns.domain.entities;

import com.atlashub.logistics.returns.domain.events.ReturnShipmentCreatedEvent;
import com.atlashub.logistics.returns.domain.events.ReturnShipmentInTransitEvent;
import com.atlashub.logistics.returns.domain.events.ReturnShipmentReceivedEvent;
import com.atlashub.logistics.returns.domain.exceptions.InsufficientDeliveryAttemptsForRtoException;
import com.atlashub.logistics.returns.domain.valueobject.Address;
import com.atlashub.logistics.returns.domain.valueobject.ReturnFaultType;
import com.atlashub.logistics.returns.domain.valueobject.ReturnReasonType;
import com.atlashub.logistics.returns.domain.valueobject.ReturnStatus;
import com.atlashub.shared.domain.valueobject.CurrencyCode;
import com.atlashub.shared.domain.valueobject.Money;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReturnShipmentTest {

    private final Address origin = new Address("45 Customer St", "Ikeja", "Lagos", "Nigeria", "100001");
    private final Money itemAmount = Money.of(new BigDecimal("10000"), CurrencyCode.NGN);
    private final Money shippingFee = Money.of(new BigDecimal("2000"), CurrencyCode.NGN);
    private final Money returnFee = Money.of(new BigDecimal("1500"), CurrencyCode.NGN);
    private final Money declaredVal = Money.of(new BigDecimal("10000"), CurrencyCode.NGN);

    @Test
    @DisplayName("3-Part Policy: Customer Fault RTO requires at least 2 delivery attempts and deducts return fee")
    void shouldEnforceCustomerFaultPolicy() {
        ReturnShipment shipment = ReturnShipment.create(
                1L, 100L, 501L, 701L, 999L,
                origin, 10L, "RET-001",
                ReturnReasonType.FAILED_DELIVERY_RTO,
                ReturnFaultType.CUSTOMER_FAULT,
                2, // 2 confirmed attempts
                itemAmount, shippingFee, returnFee, declaredVal,
                "corr-ret-1"
        );

        assertNotNull(shipment);
        assertFalse(shipment.isRefundCustomerShipping());
        // Refund = itemAmount (10,000) - returnFee (1,500) = 8,500
        assertEquals(0, new BigDecimal("8500").compareTo(shipment.getCustomerRefundAmount().amount()));
        assertEquals(0, BigDecimal.ZERO.compareTo(shipment.getMerchantPenaltyAmount().amount()));
        assertEquals(0, BigDecimal.ZERO.compareTo(shipment.getMerchantCompensationAmount().amount()));

        assertFalse(shipment.peekDomainEvents().isEmpty());
        assertTrue(shipment.peekDomainEvents().get(0) instanceof ReturnShipmentCreatedEvent);
    }

    @Test
    @DisplayName("3-Part Policy: Customer Fault RTO throws InsufficientDeliveryAttemptsForRtoException if attempts < 2")
    void shouldRejectCustomerFaultWithLessThanTwoAttempts() {
        assertThrows(InsufficientDeliveryAttemptsForRtoException.class, () ->
                ReturnShipment.create(
                        2L, 100L, 502L, 702L, 999L,
                        origin, 10L, "RET-002",
                        ReturnReasonType.FAILED_DELIVERY_RTO,
                        ReturnFaultType.CUSTOMER_FAULT,
                        1, // Only 1 attempt -> violation!
                        itemAmount, shippingFee, returnFee, declaredVal,
                        "corr-ret-2"
                )
        );
    }

    @Test
    @DisplayName("3-Part Policy: Merchant Fault grants full refund + shipping, and penalizes merchant")
    void shouldEnforceMerchantFaultPolicy() {
        ReturnShipment shipment = ReturnShipment.create(
                3L, 100L, 503L, 703L, 999L,
                origin, 10L, "RET-003",
                ReturnReasonType.CUSTOMER_RETURN,
                ReturnFaultType.MERCHANT_FAULT,
                0,
                itemAmount, shippingFee, returnFee, declaredVal,
                "corr-ret-3"
        );

        assertTrue(shipment.isRefundCustomerShipping());
        // Refund = itemAmount (10,000) + shippingFee (2,000) = 12,000
        assertEquals(0, new BigDecimal("12000").compareTo(shipment.getCustomerRefundAmount().amount()));
        // Merchant Penalty = shippingFee (2,000) + returnFee (1,500) = 3,500
        assertEquals(0, new BigDecimal("3500").compareTo(shipment.getMerchantPenaltyAmount().amount()));
        assertEquals(0, BigDecimal.ZERO.compareTo(shipment.getMerchantCompensationAmount().amount()));
    }

    @Test
    @DisplayName("3-Part Policy: Carrier Fault grants full refund + shipping, and compensates merchant")
    void shouldEnforceCarrierFaultPolicy() {
        ReturnShipment shipment = ReturnShipment.create(
                4L, 100L, 504L, 704L, 999L,
                origin, 10L, "RET-004",
                ReturnReasonType.CUSTOMER_RETURN,
                ReturnFaultType.CARRIER_FAULT,
                0,
                itemAmount, shippingFee, returnFee, declaredVal,
                "corr-ret-4"
        );

        assertTrue(shipment.isRefundCustomerShipping());
        // Refund = itemAmount (10,000) + shippingFee (2,000) = 12,000
        assertEquals(0, new BigDecimal("12000").compareTo(shipment.getCustomerRefundAmount().amount()));
        assertEquals(0, BigDecimal.ZERO.compareTo(shipment.getMerchantPenaltyAmount().amount()));
        // Merchant compensated up to declaredValue (10,000)
        assertEquals(0, new BigDecimal("10000").compareTo(shipment.getMerchantCompensationAmount().amount()));
    }

    @Test
    @DisplayName("Should progress return lifecycle to RECEIVED and record event")
    void shouldProgressReturnLifecycle() {
        ReturnShipment shipment = ReturnShipment.create(
                5L, 100L, 505L, 705L, 999L,
                origin, 10L, "RET-005",
                ReturnReasonType.CUSTOMER_RETURN,
                ReturnFaultType.CUSTOMER_FAULT,
                0,
                itemAmount, shippingFee, returnFee, declaredVal,
                "corr-ret-5"
        );

        shipment.markInTransit("corr-tr");
        assertEquals(ReturnStatus.IN_TRANSIT, shipment.getStatus());

        ZonedDateTime now = ZonedDateTime.now();
        shipment.markReceived("Item inspected in original sealed condition", now, "corr-rec");
        assertEquals(ReturnStatus.RECEIVED, shipment.getStatus());
        assertEquals("Item inspected in original sealed condition", shipment.getInspectionNotes());
        assertEquals(now, shipment.getReceivedAt());

        boolean hasReceivedEvent = shipment.peekDomainEvents().stream()
                .anyMatch(e -> e instanceof ReturnShipmentReceivedEvent);
        assertTrue(hasReceivedEvent);
    }
}
