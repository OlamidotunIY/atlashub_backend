package com.atlashub.analytics.presentation.rest;

import com.atlashub.analytics.application.query.GetDeliveryPerformance.DeliveryPerformanceResult;
import com.atlashub.analytics.application.query.GetDeliveryPerformance.GetDeliveryPerformanceHandler;
import com.atlashub.analytics.application.query.GetDeliveryPerformance.GetDeliveryPerformanceQuery;
import com.atlashub.analytics.presentation.dto.DeliveryPerformanceDto;
import com.atlashub.shared.application.dto.ApiResponse;
import com.atlashub.shared.application.security.AuthenticatedPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/analytics/logistics")
@Tag(name = "Analytics - Logistics", description = "Endpoints for shipping and delivery performance analytics")
@SecurityRequirement(name = "bearerAuth")
public class AnalyticsLogisticsController {

    private final GetDeliveryPerformanceHandler deliveryPerformanceHandler;

    public AnalyticsLogisticsController(GetDeliveryPerformanceHandler deliveryPerformanceHandler) {
        this.deliveryPerformanceHandler = deliveryPerformanceHandler;
    }

    @GetMapping("/delivery-performance")
    @Operation(summary = "Get delivery performance", description = "Returns shipment delivery rates and turnaround times for a month")
    public ResponseEntity<ApiResponse<DeliveryPerformanceDto>> getDeliveryPerformance(
            @AuthenticationPrincipal AuthenticatedPrincipal principal,
            @RequestParam(required = false) String month
    ) {
        String m = (month != null && !month.isBlank()) ? month : String.format("%04d-%02d", LocalDate.now().getYear(), LocalDate.now().getMonthValue());

        DeliveryPerformanceResult result = deliveryPerformanceHandler.execute(
                new GetDeliveryPerformanceQuery(principal.activeOrganizationId(), principal.environment(), m)
        );

        DeliveryPerformanceDto dto = new DeliveryPerformanceDto(
                result.month(),
                result.totalShipments(),
                result.delivered(),
                result.failed(),
                result.returned(),
                result.deliverySuccessRate(),
                result.avgDeliveryMinutes()
        );

        return ResponseEntity.ok(new ApiResponse<>(true, "Delivery performance retrieved successfully", dto, null));
    }
}
