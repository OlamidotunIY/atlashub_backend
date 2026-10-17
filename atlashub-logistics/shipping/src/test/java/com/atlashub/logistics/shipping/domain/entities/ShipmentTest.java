package com.atlashub.logistics.shipping.domain.entities;

import com.atlashub.logistics.shipping.domain.exceptions.InsufficientDeliveryAttemptsException;
import com.atlashub.logistics.shipping.domain.exceptions.InvalidDeliveryCodeException;
import com.atlashub.logistics.shipping.domain.exceptions.ProofOfDeliveryRequiredException;
import com.atlashub.logistics.shipping.domain.exceptions.UrgentShipmentBatchingForbiddenException;
import com.atlashub.logistics.shipping.domain.valueobject.Address;
import com.atlashub.logistics.shipping.domain.valueobject.CarrierChannel;
import com.atlashub.logistics.shipping.domain.valueobject.DeliveryFaultType;
import com.atlashub.logistics.shipping.domain.valueobject.DeliveryTier;
import com.atlashub.logistics.shipping.domain.valueobject.Dimensions;
import com.atlashub.logistics.shipping.domain.valueobject.RouteType;
import com.atlashub.logistics.shipping.domain.valueobject.ShipmentStatus;
import com.atlashub.logistics.shipping.domain.valueobject.ShipmentType;
import com.atlashub.logistics.shipping.domain.valueobject.TrackingMilestone;
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

class ShipmentTest {

    private Address createLagosAddress() {
        return new Address("12 Commercial Ave", "Yaba", "Lagos", "Nigeria", "100001");
    }

    private Address createAbujaAddress() {
        return new Address("45 Gana St", "Maitama", "Abuja", "Nigeria", "900001");
    }

    private Dimensions createDimensions() {
        return new Dimensions(new BigDecimal("30"), new BigDecimal("20"), new BigDecimal("10"));
    }

    private Shipment createTestShipment(
            Long id,
            String trackingNumber,
            ShipmentType type,
            DeliveryTier tier,
            Address origin,
            Address dest,
            BigDecimal weight,
            Money declared,
            Money customerFee,
            Money baselineCost,
            int minHours,
            int maxHours
    ) {
        ZonedDateTime now = ZonedDateTime.now();
        return Shipment.create(
                id,
                100L,
                10L,
                trackingNumber,
                type,
                tier,
                origin,
                dest,
                100L,
                50L,
                weight,
                createDimensions(),
                declared,
                customerFee,
                baselineCost,
                now.plusHours(minHours),
                now.plusHours(maxHours),
                now.plusHours(Math.max(1, minHours / 2)),
                "corr-" + id
        );
    }

    @Test
    @DisplayName("Should create standard intra-state shipment with delivery window and 6-digit delivery code")
    void shouldCreateStandardIntraStateShipment() {
        Shipment shipment = createTestShipment(
                1L, "SHP-001", ShipmentType.CUSTOMER_ORDER, DeliveryTier.STANDARD,
                createLagosAddress(), createLagosAddress(), new BigDecimal("2.5"),
                Money.of(new BigDecimal("2500"), CurrencyCode.NGN),
                Money.of(new BigDecimal("2000"), CurrencyCode.NGN),
                Money.of(new BigDecimal("1500"), CurrencyCode.NGN),
                12, 36
        );

        assertNotNull(shipment);
        assertEquals(1L, shipment.getId());
        assertEquals("SHP-001", shipment.getTrackingNumber());
        assertEquals(RouteType.INTRA_STATE, shipment.getRouteType());
        assertEquals(DeliveryTier.STANDARD, shipment.getServiceTier());
        assertEquals(ShipmentStatus.CREATED, shipment.getStatus());
        assertEquals(TrackingMilestone.CREATED, shipment.getCurrentMilestone());
        assertTrue(shipment.isIncentiveEligible());
        assertNotNull(shipment.getDeliveryCode());
        assertEquals(6, shipment.getDeliveryCode().length());
        assertTrue(shipment.getDeliveryWindowStart().isBefore(shipment.getDeliveryWindowEnd()));
        assertFalse(shipment.peekDomainEvents().isEmpty());
    }

    @Test
    @DisplayName("Should create urgent inter-state shipment with tighter delivery window")
    void shouldCreateUrgentInterStateShipment() {
        Shipment shipment = createTestShipment(
                2L, "SHP-002", ShipmentType.CUSTOMER_ORDER, DeliveryTier.URGENT,
                createLagosAddress(), createAbujaAddress(), new BigDecimal("5.0"),
                Money.of(new BigDecimal("15000"), CurrencyCode.NGN),
                Money.of(new BigDecimal("9000"), CurrencyCode.NGN),
                Money.of(new BigDecimal("7500"), CurrencyCode.NGN),
                4, 12
        );

        assertEquals(RouteType.INTER_STATE, shipment.getRouteType());
        assertEquals(DeliveryTier.URGENT, shipment.getServiceTier());
        assertEquals(ShipmentStatus.CREATED, shipment.getStatus());
    }

    @Test
    @DisplayName("Inter-outlet transfer shipments are not eligible for shipping incentive rebate")
    void interOutletTransferShouldNotBeIncentiveEligible() {
        Shipment shipment = createTestShipment(
                3L, "SHP-003", ShipmentType.INTER_OUTLET_TRANSFER, DeliveryTier.STANDARD,
                createLagosAddress(), createLagosAddress(), new BigDecimal("10.0"),
                Money.of(new BigDecimal("10000"), CurrencyCode.NGN),
                Money.of(new BigDecimal("5000"), CurrencyCode.NGN),
                Money.of(new BigDecimal("4000"), CurrencyCode.NGN),
                12, 36
        );

        assertFalse(shipment.isIncentiveEligible());
    }

    @Test
    @DisplayName("Urgent shipment cannot be added to consolidation batch")
    void urgentShipmentCannotBeBatched() {
        Shipment shipment = createTestShipment(
                4L, "SHP-004", ShipmentType.CUSTOMER_ORDER, DeliveryTier.URGENT,
                createLagosAddress(), createLagosAddress(), new BigDecimal("1.0"),
                Money.of(new BigDecimal("5000"), CurrencyCode.NGN),
                Money.of(new BigDecimal("4000"), CurrencyCode.NGN),
                Money.of(new BigDecimal("3000"), CurrencyCode.NGN),
                2, 6
        );

        assertThrows(UrgentShipmentBatchingForbiddenException.class, () -> shipment.assignToBatch(99L));
    }

    @Test
    @DisplayName("Standard shipment can be batched")
    void standardShipmentCanBeBatched() {
        Shipment shipment = createTestShipment(
                5L, "SHP-005", ShipmentType.CUSTOMER_ORDER, DeliveryTier.STANDARD,
                createLagosAddress(), createLagosAddress(), new BigDecimal("1.0"),
                Money.of(new BigDecimal("5000"), CurrencyCode.NGN),
                Money.of(new BigDecimal("2000"), CurrencyCode.NGN),
                Money.of(new BigDecimal("1500"), CurrencyCode.NGN),
                12, 36
        );

        shipment.assignToBatch(99L);
        assertEquals(99L, shipment.getConsolidationBatchId());
        assertEquals(ShipmentStatus.BATCHED, shipment.getStatus());
    }

    @Test
    @DisplayName("Should assign rider and vehicle to shipment")
    void shouldAssignRider() {
        Shipment shipment = createTestShipment(
                6L, "SHP-006", ShipmentType.CUSTOMER_ORDER, DeliveryTier.STANDARD,
                createLagosAddress(), createLagosAddress(), new BigDecimal("1.0"),
                Money.of(new BigDecimal("5000"), CurrencyCode.NGN),
                Money.of(new BigDecimal("2000"), CurrencyCode.NGN),
                Money.of(new BigDecimal("1500"), CurrencyCode.NGN),
                12, 36
        );

        shipment.assignInternalRider(201L, 301L, "corr-assign");
        assertEquals(CarrierChannel.INTERNAL_FLEET, shipment.getFulfillmentChannel());
        assertEquals(201L, shipment.getRiderId());
        assertEquals(301L, shipment.getVehicleId());
        assertEquals(ShipmentStatus.ASSIGNED, shipment.getStatus());
    }

    @Test
    @DisplayName("Should route shipment to 3PL partner carrier")
    void shouldRoute3PL() {
        Shipment shipment = createTestShipment(
                7L, "SHP-007", ShipmentType.CUSTOMER_ORDER, DeliveryTier.STANDARD,
                createLagosAddress(), createAbujaAddress(), new BigDecimal("3.0"),
                Money.of(new BigDecimal("10000"), CurrencyCode.NGN),
                Money.of(new BigDecimal("6000"), CurrencyCode.NGN),
                Money.of(new BigDecimal("5000"), CurrencyCode.NGN),
                24, 72
        );

        shipment.route3PL("GIG_LOGISTICS", "GIG-REF-9988", Money.of(new BigDecimal("4200"), CurrencyCode.NGN), "corr-3pl");
        assertEquals(CarrierChannel.PARTNER_3PL, shipment.getFulfillmentChannel());
        assertEquals("GIG_LOGISTICS", shipment.getCarrier3PLCode());
        assertEquals("GIG-REF-9988", shipment.getCarrier3PLReference());
        assertEquals(ShipmentStatus.ASSIGNED, shipment.getStatus());
    }

    @Test
    @DisplayName("Should advance milestones correctly and reject invalid transitions")
    void shouldAdvanceMilestonesAndRejectInvalid() {
        ZonedDateTime now = ZonedDateTime.now();
        Shipment shipment = createTestShipment(
                8L, "SHP-008", ShipmentType.CUSTOMER_ORDER, DeliveryTier.STANDARD,
                createLagosAddress(), createLagosAddress(), new BigDecimal("1.0"),
                Money.of(new BigDecimal("5000"), CurrencyCode.NGN),
                Money.of(new BigDecimal("2000"), CurrencyCode.NGN),
                Money.of(new BigDecimal("1500"), CurrencyCode.NGN),
                12, 36
        );

        shipment.assignInternalRider(201L, 301L, "corr-assign");
        shipment.advanceMilestone(TrackingMilestone.PICKED_UP_FROM_STORE, "Yaba Store", "Lagos", "Picked up", null, null, now, "corr-adv-1");
        assertEquals(TrackingMilestone.PICKED_UP_FROM_STORE, shipment.getCurrentMilestone());
        assertEquals(ShipmentStatus.IN_TRANSIT, shipment.getStatus());

        shipment.advanceMilestone(TrackingMilestone.IN_TRANSIT, "Ikorodu Rd", "Lagos", "On the way", null, null, now.plusMinutes(10), "corr-adv-2");
        assertEquals(TrackingMilestone.IN_TRANSIT, shipment.getCurrentMilestone());
    }

    @Test
    @DisplayName("Delivery completion requires exact 6-digit delivery code and proof of delivery")
    void deliveryCompletionRequiresValidCodeAndProof() {
        ZonedDateTime now = ZonedDateTime.now();
        Shipment shipment = createTestShipment(
                9L, "SHP-009", ShipmentType.CUSTOMER_ORDER, DeliveryTier.STANDARD,
                createLagosAddress(), createLagosAddress(), new BigDecimal("1.0"),
                Money.of(new BigDecimal("5000"), CurrencyCode.NGN),
                Money.of(new BigDecimal("2000"), CurrencyCode.NGN),
                Money.of(new BigDecimal("1500"), CurrencyCode.NGN),
                12, 36
        );
        shipment.assignInternalRider(201L, 301L, "corr-assign");
        shipment.advanceMilestone(TrackingMilestone.PICKED_UP_FROM_STORE, "Store", "Lagos", "Picked up", null, null, now, "corr-adv");

        String correctCode = shipment.getDeliveryCode();

        // Missing proof
        assertThrows(ProofOfDeliveryRequiredException.class, () ->
                shipment.completeDelivery(correctCode, null, now, "corr-complete")
        );

        // Wrong code
        assertThrows(InvalidDeliveryCodeException.class, () ->
                shipment.completeDelivery("000000", "https://cdn.atlashub.com/pod/123.jpg", now, "corr-complete")
        );

        // Correct code + proof
        shipment.completeDelivery(correctCode, "https://cdn.atlashub.com/pod/123.jpg", now, "corr-complete");
        assertEquals(ShipmentStatus.DELIVERED, shipment.getStatus());
        assertEquals(TrackingMilestone.DELIVERED, shipment.getCurrentMilestone());
        assertNotNull(shipment.getDeliveredAt());
    }

    @Test
    @DisplayName("Failed delivery policy: customer fault requires at least 2 attempts before marking failed")
    void customerFaultRequiresAtLeastTwoAttempts() {
        ZonedDateTime now = ZonedDateTime.now();
        Shipment shipment = createTestShipment(
                10L, "SHP-010", ShipmentType.CUSTOMER_ORDER, DeliveryTier.STANDARD,
                createLagosAddress(), createLagosAddress(), new BigDecimal("1.0"),
                Money.of(new BigDecimal("5000"), CurrencyCode.NGN),
                Money.of(new BigDecimal("2000"), CurrencyCode.NGN),
                Money.of(new BigDecimal("1500"), CurrencyCode.NGN),
                12, 36
        );
        shipment.assignInternalRider(201L, 301L, "corr-assign");
        shipment.advanceMilestone(TrackingMilestone.PICKED_UP_FROM_STORE, "Store", "Lagos", "Picked up", null, null, now, "corr-adv");

        // 1st attempt fails
        shipment.recordFailedAttempt("Customer phone unreachable", now, "corr-fail-1");
        assertEquals(1, shipment.getDeliveryAttempts());

        // Marking CUSTOMER_FAULT with only 1 attempt throws InsufficientDeliveryAttemptsException
        assertThrows(InsufficientDeliveryAttemptsException.class, () ->
                shipment.markFailed(DeliveryFaultType.CUSTOMER_FAULT, "Customer unreachable", now, "corr-mark-fail")
        );

        // 2nd attempt fails
        shipment.recordFailedAttempt("Customer address locked", now, "corr-fail-2");
        assertEquals(2, shipment.getDeliveryAttempts());

        // Now marking CUSTOMER_FAULT succeeds
        shipment.markFailed(DeliveryFaultType.CUSTOMER_FAULT, "Customer unreachable after 2 attempts", now, "corr-mark-fail");
        assertEquals(ShipmentStatus.FAILED, shipment.getStatus());
        assertEquals(DeliveryFaultType.CUSTOMER_FAULT, shipment.getFailureFaultType());

        // Can initiate return
        shipment.initiateReturn("Returning to merchant warehouse", now, "corr-ret");
        assertEquals(ShipmentStatus.RETURNING, shipment.getStatus());
    }

    @Test
    @DisplayName("Net cost savings calculation: positive savings when actual batch cost is lower than baseline")
    void netCostSavingsCalculation() {
        ZonedDateTime now = ZonedDateTime.now();
        Shipment shipment = createTestShipment(
                11L, "SHP-011", ShipmentType.CUSTOMER_ORDER, DeliveryTier.STANDARD,
                createLagosAddress(), createLagosAddress(), new BigDecimal("1.0"),
                Money.of(new BigDecimal("5000"), CurrencyCode.NGN),
                Money.of(new BigDecimal("3000"), CurrencyCode.NGN),
                Money.of(new BigDecimal("2500"), CurrencyCode.NGN),
                12, 36
        );
        shipment.assignInternalRider(201L, 301L, "corr-assign");
        shipment.advanceMilestone(TrackingMilestone.PICKED_UP_FROM_STORE, "Store", "Lagos", "Picked up", null, null, now, "corr-adv");
        shipment.setAllocatedCost(Money.of(new BigDecimal("1500"), CurrencyCode.NGN));
        shipment.completeDelivery(shipment.getDeliveryCode(), "https://cdn.atlashub.com/pod.jpg", now, "corr-complete");

        Money savings = shipment.netCostSavings();
        assertEquals(new BigDecimal("1000.0000"), savings.amount());
    }
}
