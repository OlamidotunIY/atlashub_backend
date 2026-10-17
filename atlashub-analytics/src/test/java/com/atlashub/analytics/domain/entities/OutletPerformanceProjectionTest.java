package com.atlashub.analytics.domain.entities;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class OutletPerformanceProjectionTest {

    @Test
    @DisplayName("Should apply sales, expenses, and calculate net profit and margin")
    void shouldCalculateOutletPerformance() {
        OutletPerformanceProjection projection = OutletPerformanceProjection.create(
                1L,
                100L,
                "live",
                200L,
                "2026-10"
        );

        assertNotNull(projection);
        projection.applySales(BigDecimal.valueOf(50000), BigDecimal.valueOf(2000), 10);
        projection.applyExpense(BigDecimal.valueOf(18000));

        assertEquals(BigDecimal.valueOf(50000), projection.getTotalSales());
        assertEquals(BigDecimal.valueOf(2000), projection.getTotalDiscounts());
        assertEquals(BigDecimal.valueOf(18000), projection.getTotalExpenses());
        assertEquals(BigDecimal.valueOf(30000), projection.getNetProfit());
        assertEquals(new BigDecimal("60.0000"), projection.getGrossMargin());
        assertEquals(10, projection.getTransactionCount());
    }
}
