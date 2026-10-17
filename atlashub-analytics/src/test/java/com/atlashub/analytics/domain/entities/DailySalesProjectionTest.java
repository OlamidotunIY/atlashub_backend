package com.atlashub.analytics.domain.entities;

import com.atlashub.analytics.domain.exceptions.AnalyticsInvariantException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DailySalesProjectionTest {

    @Test
    @DisplayName("Should create DailySalesProjection with valid inputs and default zeroes")
    void shouldCreateDailySalesProjection() {
        DailySalesProjection projection = DailySalesProjection.create(
                1L,
                100L,
                "live",
                200L,
                LocalDate.of(2026, 10, 17)
        );

        assertNotNull(projection);
        assertEquals(1L, projection.getId());
        assertEquals(100L, projection.getOrganizationId());
        assertEquals("live", projection.getEnvironment());
        assertEquals(200L, projection.getOutletId());
        assertEquals(LocalDate.of(2026, 10, 17), projection.getMetricDate());
        assertEquals(0, projection.getTotalTransactions());
        assertEquals(BigDecimal.ZERO, projection.getTotalGross());
    }

    @Test
    @DisplayName("Should throw AnalyticsInvariantException when organization ID is null")
    void shouldThrowWhenOrgIdIsNull() {
        assertThrows(AnalyticsInvariantException.class, () ->
                DailySalesProjection.create(1L, null, "live", 200L, LocalDate.now())
        );
    }

    @Test
    @DisplayName("Should apply sale correctly and increment transaction count")
    void shouldApplySale() {
        DailySalesProjection projection = DailySalesProjection.create(
                1L,
                100L,
                "live",
                200L,
                LocalDate.of(2026, 10, 17)
        );

        projection.applySale(
                BigDecimal.valueOf(1000),
                BigDecimal.valueOf(50),
                BigDecimal.valueOf(75),
                BigDecimal.valueOf(1025),
                "CARD"
        );

        assertEquals(1, projection.getTotalTransactions());
        assertEquals(BigDecimal.valueOf(1000), projection.getTotalGross());
        assertEquals(BigDecimal.valueOf(50), projection.getTotalDiscounts());
        assertEquals(BigDecimal.valueOf(75), projection.getTotalTax());
        assertEquals(BigDecimal.valueOf(1025), projection.getTotalNet());
        assertEquals(BigDecimal.valueOf(1025), projection.getCardSales());
        assertEquals(BigDecimal.ZERO, projection.getCashSales());
    }

    @Test
    @DisplayName("Should apply refund and subtract amounts without falling below zero")
    void shouldApplyRefund() {
        DailySalesProjection projection = DailySalesProjection.create(
                1L,
                100L,
                "live",
                200L,
                LocalDate.of(2026, 10, 17)
        );

        projection.applySale(
                BigDecimal.valueOf(1000),
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.valueOf(1000),
                "CASH"
        );

        projection.applyRefund(
                BigDecimal.valueOf(400),
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.valueOf(400),
                "CASH"
        );

        assertEquals(BigDecimal.valueOf(600), projection.getTotalGross());
        assertEquals(BigDecimal.valueOf(600), projection.getTotalNet());
        assertEquals(BigDecimal.valueOf(600), projection.getCashSales());
    }
}
