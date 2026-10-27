package com.atlashub.analytics.application.query;

import com.atlashub.analytics.application.query.GetBusinessOverview.GetBusinessOverviewHandler;
import com.atlashub.analytics.application.query.GetBusinessOverview.GetBusinessOverviewQuery;
import com.atlashub.analytics.domain.entities.AnalyticsMetricEvent;
import com.atlashub.analytics.domain.entities.InventoryHealthProjection;
import com.atlashub.analytics.domain.repositories.AnalyticsMetricEventRepository;
import com.atlashub.analytics.domain.repositories.DailySalesProjectionRepository;
import com.atlashub.analytics.domain.repositories.DeliveryPerformanceProjectionRepository;
import com.atlashub.analytics.domain.repositories.FinancialHealthProjectionRepository;
import com.atlashub.analytics.domain.repositories.HeadcountProjectionRepository;
import com.atlashub.analytics.domain.repositories.InventoryHealthProjectionRepository;
import com.atlashub.analytics.domain.repositories.OutletPerformanceProjectionRepository;
import com.atlashub.analytics.domain.repositories.PayrollCostProjectionRepository;
import com.atlashub.analytics.domain.repositories.ProductDemandProjectionRepository;
import com.atlashub.analytics.domain.repositories.TransactionVolumeProjectionRepository;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.prepost.PreAuthorize;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class GetBusinessOverviewHandlerTest {
    private final DailySalesProjectionRepository sales = mock(DailySalesProjectionRepository.class);
    private final TransactionVolumeProjectionRepository payments = mock(TransactionVolumeProjectionRepository.class);
    private final HeadcountProjectionRepository people = mock(HeadcountProjectionRepository.class);
    private final PayrollCostProjectionRepository payroll = mock(PayrollCostProjectionRepository.class);
    private final OutletPerformanceProjectionRepository outlets = mock(OutletPerformanceProjectionRepository.class);
    private final ProductDemandProjectionRepository products = mock(ProductDemandProjectionRepository.class);
    private final DeliveryPerformanceProjectionRepository logistics = mock(DeliveryPerformanceProjectionRepository.class);
    private final InventoryHealthProjectionRepository inventory = mock(InventoryHealthProjectionRepository.class);
    private final FinancialHealthProjectionRepository finance = mock(FinancialHealthProjectionRepository.class);
    private final AnalyticsMetricEventRepository activity = mock(AnalyticsMetricEventRepository.class);

    @Test
    void enforces_report_read_permission_at_the_handler_boundary() throws Exception {
        PreAuthorize annotation = GetBusinessOverviewHandler.class
                .getMethod("execute", GetBusinessOverviewQuery.class).getAnnotation(PreAuthorize.class);

        assertNotNull(annotation);
        assertEquals("hasAuthority('analytics:reports:read')", annotation.value());
    }

    @Test
    void scopes_all_reads_and_returns_attention_and_recent_activity() {
        stubEmptyReads();
        when(inventory.findByOrgEnvAndOutlet(9L, "sandbox", null)).thenReturn(List.of(
                InventoryHealthProjection.create(1L, 9L, "sandbox", 2L, 3L)));
        when(activity.findRecent(9L, "sandbox", 12)).thenReturn(List.of(
                AnalyticsMetricEvent.create(1L, "event-1", 9L, "sandbox", "STOCK_ADJUSTED", "commerce",
                        null, "NGN", "{}", ZonedDateTime.parse("2026-10-18T10:00:00Z"))));

        var result = handler().execute(new GetBusinessOverviewQuery(9L, "sandbox", LocalDate.of(2026, 10, 18)));

        assertEquals(0, result.sales().transactions());
        assertEquals(1, result.inventory().outOfStockCount());
        assertEquals("OUT_OF_STOCK", result.attention().getFirst().code());
        assertEquals("event-1", result.recentActivity().getFirst().eventId());
        verify(sales).findByOrgEnvAndDateRange(9L, "sandbox", null,
                LocalDate.of(2026, 9, 19), LocalDate.of(2026, 10, 18));
        verify(products).findTopProducts(9L, "sandbox", "2026-10");
    }

    private GetBusinessOverviewHandler handler() {
        return new GetBusinessOverviewHandler(sales, payments, people, payroll, outlets, products, logistics,
                inventory, finance, activity);
    }

    private void stubEmptyReads() {
        when(sales.findByOrgEnvAndDateRange(anyLong(), anyString(), isNull(), any(), any())).thenReturn(List.of());
        when(payments.findByOrgEnvAndMonth(anyLong(), anyString(), anyString())).thenReturn(Optional.empty());
        when(people.findByOrgEnv(anyLong(), anyString())).thenReturn(Optional.empty());
        when(payroll.findByOrgEnvAndMonth(anyLong(), anyString(), anyString())).thenReturn(Optional.empty());
        when(outlets.findByOrgEnvAndMonth(anyLong(), anyString(), anyString())).thenReturn(List.of());
        when(products.findTopProducts(anyLong(), anyString(), anyString())).thenReturn(List.of());
        when(logistics.findByOrgEnvAndMonth(anyLong(), anyString(), anyString())).thenReturn(Optional.empty());
        when(inventory.findByOrgEnvAndOutlet(anyLong(), anyString(), isNull())).thenReturn(List.of());
        when(finance.findLatestByOrgEnv(anyLong(), anyString())).thenReturn(Optional.empty());
        when(activity.findRecent(anyLong(), anyString(), anyInt())).thenReturn(List.of());
    }
}
