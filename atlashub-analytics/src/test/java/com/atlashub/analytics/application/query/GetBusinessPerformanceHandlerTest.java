package com.atlashub.analytics.application.query;

import com.atlashub.analytics.application.query.GetBusinessPerformance.BusinessPerformanceResult;
import com.atlashub.analytics.application.query.GetBusinessPerformance.GetBusinessPerformanceHandler;
import com.atlashub.analytics.application.query.GetBusinessPerformance.GetBusinessPerformanceQuery;
import com.atlashub.analytics.domain.entities.CustomerPerformanceProjection;
import com.atlashub.analytics.domain.entities.DailySalesProjection;
import com.atlashub.analytics.domain.repositories.CustomerPerformanceProjectionRepository;
import com.atlashub.analytics.domain.repositories.DailySalesProjectionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GetBusinessPerformanceHandlerTest {

    private DailySalesProjectionRepository salesRepository;
    private CustomerPerformanceProjectionRepository customerRepository;
    private GetBusinessPerformanceHandler handler;

    @BeforeEach
    void setUp() {
        salesRepository = mock(DailySalesProjectionRepository.class);
        customerRepository = mock(CustomerPerformanceProjectionRepository.class);
        handler = new GetBusinessPerformanceHandler(salesRepository, customerRepository);
    }

    @Test
    @DisplayName("Should compose sales and customer performance into BusinessPerformanceResult")
    void shouldReturnBusinessPerformance() {
        DailySalesProjection p1 = DailySalesProjection.create(
                1L, 100L, "live", 10L, LocalDate.of(2026, 10, 10)
        );
        p1.applySale(BigDecimal.valueOf(2000), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.valueOf(2000), "CARD");

        DailySalesProjection p2 = DailySalesProjection.create(
                2L, 100L, "live", 20L, LocalDate.of(2026, 10, 12)
        );
        p2.applySale(BigDecimal.valueOf(3000), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.valueOf(3000), "CARD");

        when(salesRepository.findByOrgEnvAndDateRange(eq(100L), eq("live"), any(), any(), any()))
                .thenReturn(List.of(p1, p2));

        CustomerPerformanceProjection c1 = CustomerPerformanceProjection.create(
                1L, 100L, "live", 501L, "2026-10"
        );
        c1.applyOrder(BigDecimal.valueOf(1000));
        c1.applyOrder(BigDecimal.valueOf(1000));

        CustomerPerformanceProjection c2 = CustomerPerformanceProjection.create(
                2L, 100L, "live", 502L, "2026-10"
        );
        c2.applyOrder(BigDecimal.valueOf(3000));

        when(customerRepository.findByOrgEnvAndMonth(eq(100L), eq("live"), eq("2026-10")))
                .thenReturn(List.of(c1, c2));

        GetBusinessPerformanceQuery query = new GetBusinessPerformanceQuery(
                100L,
                "live",
                LocalDate.of(2026, 10, 17)
        );

        BusinessPerformanceResult result = handler.execute(query);

        assertNotNull(result);
        assertEquals(100L, result.organizationId());
        assertEquals(BigDecimal.valueOf(5000), result.totalGrossRevenue());
        assertEquals(BigDecimal.valueOf(5000), result.totalNetRevenue());
        assertEquals(2, result.totalTransactions());
        assertEquals(new BigDecimal("2500.00"), result.averageOrderValue());
        assertEquals(BigDecimal.valueOf(2), result.activeOutletsCount());
        assertEquals(new BigDecimal("50.0000"), result.repeatCustomerRate());
    }
}
