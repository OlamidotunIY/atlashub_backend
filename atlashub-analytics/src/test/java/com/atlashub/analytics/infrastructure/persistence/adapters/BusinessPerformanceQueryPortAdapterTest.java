package com.atlashub.analytics.infrastructure.persistence.adapters;

import com.atlashub.analytics.application.query.GetBusinessPerformance.BusinessPerformanceResult;
import com.atlashub.analytics.application.query.GetBusinessPerformance.GetBusinessPerformanceHandler;
import com.atlashub.analytics.application.query.GetBusinessPerformance.GetBusinessPerformanceQuery;
import com.atlashub.shared.application.port.BusinessPerformanceQueryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class BusinessPerformanceQueryPortAdapterTest {

    private GetBusinessPerformanceHandler handler;
    private BusinessPerformanceQueryPortAdapter adapter;

    @BeforeEach
    void setUp() {
        handler = mock(GetBusinessPerformanceHandler.class);
        adapter = new BusinessPerformanceQueryPortAdapter(handler);
    }

    @Test
    @DisplayName("Should return BusinessPerformanceSnapshot through shared port contract")
    void shouldReturnSnapshot() {
        LocalDate asOf = LocalDate.of(2026, 10, 17);
        BusinessPerformanceResult result = new BusinessPerformanceResult(
                100L,
                "live",
                asOf,
                BigDecimal.valueOf(10000),
                BigDecimal.valueOf(9500),
                10L,
                BigDecimal.valueOf(950),
                BigDecimal.valueOf(40),
                BigDecimal.valueOf(3)
        );

        when(handler.execute(any(GetBusinessPerformanceQuery.class))).thenReturn(result);

        Optional<BusinessPerformanceQueryPort.BusinessPerformanceSnapshot> snapshot =
                adapter.getPerformance(100L, "live", asOf);

        assertTrue(snapshot.isPresent());
        assertEquals(100L, snapshot.get().organizationId());
        assertEquals("live", snapshot.get().environment());
        assertEquals(asOf, snapshot.get().asOfDate());
        assertEquals(BigDecimal.valueOf(10000), snapshot.get().totalGrossRevenue());
        assertEquals(10L, snapshot.get().totalTransactions());
    }

    @Test
    @DisplayName("Should return empty when organizationId is null")
    void shouldReturnEmptyWhenOrgIsNull() {
        Optional<BusinessPerformanceQueryPort.BusinessPerformanceSnapshot> snapshot =
                adapter.getPerformance(null, "live", LocalDate.now());

        assertTrue(snapshot.isEmpty());
    }
}
