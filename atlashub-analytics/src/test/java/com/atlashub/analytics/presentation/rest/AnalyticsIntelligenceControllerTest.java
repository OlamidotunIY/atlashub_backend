package com.atlashub.analytics.presentation.rest;

import com.atlashub.analytics.application.query.GetBusinessFeatureSnapshot.BusinessFeatureSnapshotResult;
import com.atlashub.analytics.application.query.GetBusinessFeatureSnapshot.GetBusinessFeatureSnapshotHandler;
import com.atlashub.analytics.application.query.GetBusinessFeatureSnapshot.GetBusinessFeatureSnapshotQuery;
import com.atlashub.analytics.application.query.GetBusinessPerformance.BusinessPerformanceResult;
import com.atlashub.analytics.application.query.GetBusinessPerformance.GetBusinessPerformanceHandler;
import com.atlashub.analytics.application.query.GetBusinessPerformance.GetBusinessPerformanceQuery;
import com.atlashub.analytics.application.query.GetFinancialHealth.FinancialHealthResult;
import com.atlashub.analytics.application.query.GetFinancialHealth.GetFinancialHealthHandler;
import com.atlashub.analytics.application.query.GetFinancialHealth.GetFinancialHealthQuery;
import com.atlashub.analytics.application.query.GetFinancingBehaviour.FinancingBehaviourResult;
import com.atlashub.analytics.application.query.GetFinancingBehaviour.GetFinancingBehaviourHandler;
import com.atlashub.analytics.application.query.GetFinancingBehaviour.GetFinancingBehaviourQuery;
import com.atlashub.analytics.application.query.GetInventoryHealth.GetInventoryHealthHandler;
import com.atlashub.analytics.application.query.GetInventoryHealth.GetInventoryHealthQuery;
import com.atlashub.analytics.application.query.GetInventoryHealth.InventoryHealthResult;
import com.atlashub.analytics.application.query.GetOutletPerformance.GetOutletPerformanceHandler;
import com.atlashub.analytics.application.query.GetOutletPerformance.GetOutletPerformanceQuery;
import com.atlashub.analytics.application.query.GetOutletPerformance.OutletPerformanceResult;
import com.atlashub.analytics.domain.valueobject.DataQualityStatus;
import com.atlashub.analytics.presentation.dto.BusinessFeatureSnapshotDto;
import com.atlashub.analytics.presentation.dto.BusinessPerformanceDto;
import com.atlashub.analytics.presentation.dto.FinancialHealthDto;
import com.atlashub.analytics.presentation.dto.FinancingBehaviourDto;
import com.atlashub.analytics.presentation.dto.InventoryHealthDto;
import com.atlashub.analytics.presentation.dto.OutletPerformanceDto;
import com.atlashub.shared.application.dto.ApiResponse;
import com.atlashub.shared.application.security.AuthenticatedPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnalyticsIntelligenceControllerTest {

    @Mock
    private GetBusinessPerformanceHandler businessPerformanceHandler;
    @Mock
    private GetFinancialHealthHandler financialHealthHandler;
    @Mock
    private GetInventoryHealthHandler inventoryHealthHandler;
    @Mock
    private GetOutletPerformanceHandler outletPerformanceHandler;
    @Mock
    private GetFinancingBehaviourHandler financingBehaviourHandler;
    @Mock
    private GetBusinessFeatureSnapshotHandler businessFeatureSnapshotHandler;

    private AnalyticsIntelligenceController controller;
    private AuthenticatedPrincipal principal;

    @BeforeEach
    void setUp() {
        controller = new AnalyticsIntelligenceController(
                businessPerformanceHandler,
                financialHealthHandler,
                inventoryHealthHandler,
                outletPerformanceHandler,
                financingBehaviourHandler,
                businessFeatureSnapshotHandler
        );
        principal = new AuthenticatedPrincipal(1L, 100L, "live", "sess-1", "tok-1", ZonedDateTime.now().plusHours(1));
    }

    @Test
    @DisplayName("Should return 200 with overall business performance")
    void shouldReturnBusinessPerformance() {
        LocalDate asOf = LocalDate.now();
        BusinessPerformanceResult result = new BusinessPerformanceResult(
                100L, "live", asOf, BigDecimal.valueOf(10000), BigDecimal.valueOf(9500),
                20L, BigDecimal.valueOf(475), BigDecimal.valueOf(35), BigDecimal.valueOf(2)
        );

        when(businessPerformanceHandler.execute(any(GetBusinessPerformanceQuery.class)))
                .thenReturn(result);

        ResponseEntity<ApiResponse<BusinessPerformanceDto>> response =
                controller.getBusinessPerformance(principal, asOf);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(100L, response.getBody().data().organizationId());
        assertEquals(BigDecimal.valueOf(10000), response.getBody().data().totalGrossRevenue());
    }

    @Test
    @DisplayName("Should return 200 with financial health")
    void shouldReturnFinancialHealth() {
        LocalDate period = LocalDate.now();
        FinancialHealthResult result = new FinancialHealthResult(
                100L, "live", period, BigDecimal.valueOf(50000), BigDecimal.valueOf(20000),
                BigDecimal.valueOf(30000), BigDecimal.valueOf(60), BigDecimal.valueOf(10000),
                BigDecimal.valueOf(20000), BigDecimal.valueOf(100000), BigDecimal.valueOf(5000),
                BigDecimal.valueOf(3000), "RECONCILED"
        );

        when(financialHealthHandler.execute(any(GetFinancialHealthQuery.class))).thenReturn(result);

        ResponseEntity<ApiResponse<FinancialHealthDto>> response =
                controller.getFinancialHealth(principal, period);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(BigDecimal.valueOf(30000), response.getBody().data().grossProfit());
        assertEquals("RECONCILED", response.getBody().data().reconciliationStatus());
    }

    @Test
    @DisplayName("Should return 200 with feature snapshot")
    void shouldReturnFeatureSnapshot() {
        ZonedDateTime now = ZonedDateTime.now();
        BusinessFeatureSnapshotResult result = new BusinessFeatureSnapshotResult(
                10L, 100L, "live", "v1.0", now.minusDays(30), now, now,
                DataQualityStatus.COMPLETE, "{}", "{}", now, now.plusDays(90)
        );

        when(businessFeatureSnapshotHandler.execute(any(GetBusinessFeatureSnapshotQuery.class)))
                .thenReturn(result);

        ResponseEntity<ApiResponse<BusinessFeatureSnapshotDto>> response =
                controller.getFeatureSnapshot(principal, 10L, "v1.0");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(10L, response.getBody().data().snapshotId());
        assertEquals(DataQualityStatus.COMPLETE, response.getBody().data().completeness());
    }
}
