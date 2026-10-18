package com.atlashub.analytics.application.query;

import com.atlashub.analytics.application.query.GetSalesSummary.DailySalesSummaryResult;
import com.atlashub.analytics.application.query.GetSalesSummary.GetSalesSummaryHandler;
import com.atlashub.analytics.application.query.GetSalesSummary.GetSalesSummaryQuery;
import com.atlashub.analytics.domain.entities.DailySalesProjection;
import com.atlashub.analytics.domain.repositories.DailySalesProjectionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GetSalesSummaryHandlerTest {

    private DailySalesProjectionRepository repository;
    private GetSalesSummaryHandler handler;

    @BeforeEach
    void setUp() {
        repository = mock(DailySalesProjectionRepository.class);
        handler = new GetSalesSummaryHandler(repository);
    }

    @Test
    @DisplayName("Should return mapped list of DailySalesSummaryResult")
    void shouldReturnSalesSummary() {
        DailySalesProjection projection = DailySalesProjection.create(
                1L, 100L, "live", 200L, LocalDate.of(2026, 10, 15)
        );
        projection.applySale(BigDecimal.valueOf(1000), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.valueOf(1000), "CASH");

        when(repository.findByOrgEnvAndDateRange(eq(100L), eq("live"), any(), any(), any()))
                .thenReturn(List.of(projection));

        GetSalesSummaryQuery query = new GetSalesSummaryQuery(
                100L,
                "live",
                200L,
                LocalDate.of(2026, 10, 1),
                LocalDate.of(2026, 10, 17)
        );

        List<DailySalesSummaryResult> results = handler.execute(query);

        assertFalse(results.isEmpty());
        assertEquals(1, results.size());
        DailySalesSummaryResult result = results.get(0);
        assertEquals(LocalDate.of(2026, 10, 15), result.date());
        assertEquals(1, result.totalTransactions());
        assertEquals(BigDecimal.valueOf(1000), result.totalGross());
        assertEquals(BigDecimal.valueOf(1000), result.cashSales());
    }
}
