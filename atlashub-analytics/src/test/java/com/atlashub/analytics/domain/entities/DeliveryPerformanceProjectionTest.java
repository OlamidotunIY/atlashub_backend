package com.atlashub.analytics.domain.entities;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class DeliveryPerformanceProjectionTest {

    @Test
    @DisplayName("Should record delivery and compute success rate")
    void shouldRecordDelivery() {
        DeliveryPerformanceProjection projection = DeliveryPerformanceProjection.create(
                1L,
                100L,
                "live",
                "2026-10"
        );

        assertNotNull(projection);
        assertEquals(0, projection.getTotalShipments());

        projection.recordShipment(true, false, 45);
        assertEquals(1, projection.getTotalShipments());
        assertEquals(1, projection.getDelivered());
        assertEquals(new BigDecimal("100.0000"), projection.getDeliverySuccessRate());
        assertEquals(45, projection.getAvgDeliveryMinutes());

        projection.recordShipment(false, false, null);
        assertEquals(2, projection.getTotalShipments());
        assertEquals(1, projection.getDelivered());
        assertEquals(1, projection.getFailed());
        assertEquals(new BigDecimal("50.0000"), projection.getDeliverySuccessRate());
    }
}
