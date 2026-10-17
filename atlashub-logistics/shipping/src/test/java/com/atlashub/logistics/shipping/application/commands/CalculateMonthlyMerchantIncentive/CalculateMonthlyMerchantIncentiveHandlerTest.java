package com.atlashub.logistics.shipping.application.commands.CalculateMonthlyMerchantIncentive;

import com.atlashub.logistics.shipping.domain.entities.MerchantShippingIncentive;
import com.atlashub.logistics.shipping.domain.entities.Shipment;
import com.atlashub.logistics.shipping.domain.repositories.MerchantShippingIncentiveRepository;
import com.atlashub.logistics.shipping.domain.repositories.ShipmentRepository;
import com.atlashub.logistics.shipping.domain.valueobject.Address;
import com.atlashub.logistics.shipping.domain.valueobject.DeliveryTier;
import com.atlashub.logistics.shipping.domain.valueobject.Dimensions;
import com.atlashub.logistics.shipping.domain.valueobject.ShipmentType;
import com.atlashub.logistics.shipping.domain.valueobject.TrackingMilestone;
import com.atlashub.shared.domain.valueobject.CurrencyCode;
import com.atlashub.shared.domain.valueobject.Money;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CalculateMonthlyMerchantIncentiveHandlerTest {

    @Mock
    private MerchantShippingIncentiveRepository incentiveRepository;

    @Mock
    private ShipmentRepository shipmentRepository;

    private CalculateMonthlyMerchantIncentiveHandler handler;

    @BeforeEach
    void setUp() {
        handler = new CalculateMonthlyMerchantIncentiveHandler(incentiveRepository, shipmentRepository);
    }

    @Test
    @DisplayName("Should aggregate net savings and calculate 10% monthly rebate for merchant")
    void shouldCalculateRebate() {
        ZonedDateTime now = ZonedDateTime.now();
        ZonedDateTime start = now.minusDays(15);
        ZonedDateTime end = now.plusDays(15);

        Shipment shipment1 = Shipment.create(
                1L, 100L, 10L, "TRK-1", ShipmentType.CUSTOMER_ORDER, DeliveryTier.STANDARD,
                new Address("Street", "City", "Lagos", "Nigeria", "100001"),
                new Address("Street", "City", "Lagos", "Nigeria", "100001"),
                100L, 10L,
                BigDecimal.ONE, new Dimensions(BigDecimal.TEN, BigDecimal.TEN, BigDecimal.TEN),
                Money.of(new BigDecimal("5000"), CurrencyCode.NGN),
                Money.of(new BigDecimal("3000"), CurrencyCode.NGN),
                Money.of(new BigDecimal("2500"), CurrencyCode.NGN),
                now.plusHours(12), now.plusHours(36), now.plusHours(6), "corr-inc-1"
        );
        shipment1.advanceMilestone(TrackingMilestone.PICKED_UP_FROM_STORE, "Hub", "Lagos", "Picked up", null, null, now, "corr-adv-1");
        shipment1.setAllocatedCost(Money.of(new BigDecimal("1500"), CurrencyCode.NGN)); // Net savings = 1000
        shipment1.completeDelivery(shipment1.getDeliveryCode(), "https://cdn.atlashub.com/pod1.jpg", now, "corr-comp-1");

        Shipment shipment2 = Shipment.create(
                2L, 100L, 10L, "TRK-2", ShipmentType.CUSTOMER_ORDER, DeliveryTier.STANDARD,
                new Address("Street", "City", "Lagos", "Nigeria", "100001"),
                new Address("Street", "City", "Lagos", "Nigeria", "100001"),
                100L, 10L,
                BigDecimal.ONE, new Dimensions(BigDecimal.TEN, BigDecimal.TEN, BigDecimal.TEN),
                Money.of(new BigDecimal("6000"), CurrencyCode.NGN),
                Money.of(new BigDecimal("5000"), CurrencyCode.NGN),
                Money.of(new BigDecimal("4500"), CurrencyCode.NGN),
                now.plusHours(12), now.plusHours(36), now.plusHours(6), "corr-inc-2"
        );
        shipment2.advanceMilestone(TrackingMilestone.PICKED_UP_FROM_STORE, "Hub", "Lagos", "Picked up", null, null, now, "corr-adv-2");
        shipment2.setAllocatedCost(Money.of(new BigDecimal("2500"), CurrencyCode.NGN)); // Net savings = 2000
        shipment2.completeDelivery(shipment2.getDeliveryCode(), "https://cdn.atlashub.com/pod2.jpg", now, "corr-comp-2");

        when(shipmentRepository.findDeliveredOrdersForMonth(100L, start, end)).thenReturn(List.of(shipment1, shipment2));
        when(incentiveRepository.nextIdentity()).thenReturn(501L);

        CalculateMonthlyMerchantIncentiveCommand command = new CalculateMonthlyMerchantIncentiveCommand(
                100L, "2026-10", start, end, "corr-calc-1"
        );

        CalculateMonthlyMerchantIncentiveResult result = handler.execute(command);

        assertNotNull(result);
        assertEquals(501L, result.incentiveId());
        assertEquals("2026-10", result.billingYearMonth());
        assertEquals(2, result.eligibleShipmentCount());
        // Total savings = 1000 + 2000 = 3000. 10% rebate = 300.00
        assertEquals(new BigDecimal("300.0000"), result.rebateAmount().amount());

        verify(incentiveRepository).save(any(MerchantShippingIncentive.class));
    }
}
