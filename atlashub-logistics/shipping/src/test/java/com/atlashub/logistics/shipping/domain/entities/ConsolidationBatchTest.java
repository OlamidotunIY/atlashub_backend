package com.atlashub.logistics.shipping.domain.entities;

import com.atlashub.logistics.shipping.domain.valueobject.BatchStatus;
import com.atlashub.logistics.shipping.domain.valueobject.CarrierChannel;
import com.atlashub.logistics.shipping.domain.valueobject.RouteType;
import com.atlashub.shared.domain.valueobject.CurrencyCode;
import com.atlashub.shared.domain.valueobject.Money;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConsolidationBatchTest {

    @Test
    @DisplayName("Should create open consolidation batch and detect route type")
    void shouldCreateOpenConsolidationBatch() {
        ConsolidationBatch batch = ConsolidationBatch.create(
                1L,
                "BATCH-202610-001",
                "Lagos",
                "Ikeja",
                "Abuja",
                "Maitama",
                CurrencyCode.NGN
        );

        assertNotNull(batch);
        assertEquals(1L, batch.getId());
        assertEquals("BATCH-202610-001", batch.getBatchReference());
        assertEquals(RouteType.INTER_STATE, batch.getRouteType());
        assertEquals(BatchStatus.OPEN, batch.getStatus());
        assertEquals(BigDecimal.ZERO, batch.getTotalWeightKg());
    }

    @Test
    @DisplayName("Should add shipments to batch and aggregate weight and baseline cost")
    void shouldAddShipmentsToBatch() {
        ConsolidationBatch batch = ConsolidationBatch.create(
                1L,
                "BATCH-202610-002",
                "Lagos",
                "Ikeja",
                "Lagos",
                "Yaba",
                CurrencyCode.NGN
        );

        assertEquals(RouteType.INTRA_STATE, batch.getRouteType());

        ZonedDateTime now = ZonedDateTime.now();
        batch.addShipment(101L, new BigDecimal("2.5"), Money.of(new BigDecimal("2000"), CurrencyCode.NGN), now.plusHours(24));
        batch.addShipment(102L, new BigDecimal("7.5"), Money.of(new BigDecimal("4000"), CurrencyCode.NGN), now.plusHours(12));

        assertEquals(2, batch.getShipmentIds().size());
        assertEquals(0, new BigDecimal("10.0").compareTo(batch.getTotalWeightKg()));
        assertEquals(0, new BigDecimal("6000").compareTo(batch.getTotalBaselineCost().amount()));
        assertEquals(now.plusHours(12), batch.getLatestDispatchAt());
    }

    @Test
    @DisplayName("Should dispatch batch and proportionally allocate carrier cost by weight")
    void shouldDispatchBatchAndAllocateCost() {
        ConsolidationBatch batch = ConsolidationBatch.create(
                1L,
                "BATCH-202610-003",
                "Lagos",
                "Ikeja",
                "Abuja",
                "Maitama",
                CurrencyCode.NGN
        );

        ZonedDateTime now = ZonedDateTime.now();
        batch.addShipment(101L, new BigDecimal("20.0"), Money.of(new BigDecimal("15000"), CurrencyCode.NGN), now.plusHours(24));
        batch.addShipment(102L, new BigDecimal("80.0"), Money.of(new BigDecimal("35000"), CurrencyCode.NGN), now.plusHours(24));

        Money actualCost = Money.of(new BigDecimal("30000"), CurrencyCode.NGN);
        batch.dispatchBatch(CarrierChannel.PARTNER_3PL, "GIG_LOGISTICS", actualCost, now);

        assertEquals(BatchStatus.DISPATCHED, batch.getStatus());
        assertEquals(CarrierChannel.PARTNER_3PL, batch.getCarrierChannel());
        assertEquals("GIG_LOGISTICS", batch.getCarrierCode());

        // Shipment 101 has 20% of weight -> 20% of 30,000 = 6,000
        Money allocated101 = batch.allocateCostForWeight(new BigDecimal("20.0"));
        assertEquals(0, new BigDecimal("6000").compareTo(allocated101.amount()));

        // Shipment 102 has 80% of weight -> 80% of 30,000 = 24,000
        Money allocated102 = batch.allocateCostForWeight(new BigDecimal("80.0"));
        assertEquals(0, new BigDecimal("24000").compareTo(allocated102.amount()));
    }
}
