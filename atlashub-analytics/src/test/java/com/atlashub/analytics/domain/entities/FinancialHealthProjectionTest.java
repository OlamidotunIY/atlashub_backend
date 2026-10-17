package com.atlashub.analytics.domain.entities;

import com.atlashub.analytics.domain.exceptions.AnalyticsInvariantException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FinancialHealthProjectionTest {

    @Test
    @DisplayName("Should create FinancialHealthProjection and enforce invariants")
    void shouldCreateFinancialHealthProjection() {
        FinancialHealthProjection projection = FinancialHealthProjection.create(
                1L,
                100L,
                "live",
                LocalDate.of(2026, 9, 30)
        );

        assertNotNull(projection);
        assertEquals(1L, projection.getId());
        assertEquals(100L, projection.getOrganizationId());
        assertEquals("RECONCILED", projection.getReconciliationStatus());

        assertThrows(AnalyticsInvariantException.class, () ->
                FinancialHealthProjection.create(2L, null, "live", LocalDate.now())
        );
    }

    @Test
    @DisplayName("Should update metrics and calculate gross profit and margin")
    void shouldUpdateMetrics() {
        FinancialHealthProjection projection = FinancialHealthProjection.create(
                1L,
                100L,
                "live",
                LocalDate.of(2026, 9, 30)
        );

        projection.updateMetrics(
                BigDecimal.valueOf(100000),
                BigDecimal.valueOf(40000),
                BigDecimal.valueOf(25000),
                BigDecimal.valueOf(150000),
                BigDecimal.valueOf(20000),
                BigDecimal.valueOf(15000),
                "RECONCILED"
        );

        assertEquals(BigDecimal.valueOf(60000), projection.getGrossProfit());
        assertEquals(new BigDecimal("60.0000"), projection.getGrossMargin());
        assertEquals(BigDecimal.valueOf(35000), projection.getNetOperatingIncome());
        assertEquals(BigDecimal.valueOf(150000), projection.getCashBalance());
    }
}
