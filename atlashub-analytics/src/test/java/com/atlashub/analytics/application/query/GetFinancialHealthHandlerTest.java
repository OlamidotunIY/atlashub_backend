package com.atlashub.analytics.application.query;

import com.atlashub.analytics.application.query.GetFinancialHealth.FinancialHealthResult;
import com.atlashub.analytics.application.query.GetFinancialHealth.GetFinancialHealthHandler;
import com.atlashub.analytics.application.query.GetFinancialHealth.GetFinancialHealthQuery;
import com.atlashub.analytics.domain.entities.FinancialHealthProjection;
import com.atlashub.analytics.domain.repositories.FinancialHealthProjectionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GetFinancialHealthHandlerTest {

    private FinancialHealthProjectionRepository repository;
    private GetFinancialHealthHandler handler;

    @BeforeEach
    void setUp() {
        repository = mock(FinancialHealthProjectionRepository.class);
        handler = new GetFinancialHealthHandler(repository);
    }

    @Test
    @DisplayName("Should return FinancialHealthResult from projection")
    void shouldReturnFinancialHealth() {
        LocalDate period = LocalDate.of(2026, 9, 30);
        FinancialHealthProjection projection = FinancialHealthProjection.create(
                1L, 100L, "live", period
        );
        projection.updateMetrics(
                BigDecimal.valueOf(200000),
                BigDecimal.valueOf(80000),
                BigDecimal.valueOf(40000),
                BigDecimal.valueOf(500000),
                BigDecimal.valueOf(30000),
                BigDecimal.valueOf(20000),
                "RECONCILED"
        );

        when(repository.findByOrgEnvAndPeriodEnd(eq(100L), eq("live"), eq(period)))
                .thenReturn(Optional.of(projection));

        GetFinancialHealthQuery query = new GetFinancialHealthQuery(100L, "live", period);
        FinancialHealthResult result = handler.execute(query);

        assertNotNull(result);
        assertEquals(100L, result.organizationId());
        assertEquals(BigDecimal.valueOf(200000), result.revenue());
        assertEquals(BigDecimal.valueOf(120000), result.grossProfit());
        assertEquals(BigDecimal.valueOf(80000), result.netOperatingIncome());
        assertEquals("RECONCILED", result.reconciliationStatus());
    }
}
