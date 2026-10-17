package com.atlashub.logistics.shipping.presentation.rest;

import com.atlashub.logistics.shipping.application.commands.CalculateMonthlyMerchantIncentive.CalculateMonthlyMerchantIncentiveCommand;
import com.atlashub.logistics.shipping.application.commands.CalculateMonthlyMerchantIncentive.CalculateMonthlyMerchantIncentiveHandler;
import com.atlashub.logistics.shipping.application.commands.CalculateMonthlyMerchantIncentive.CalculateMonthlyMerchantIncentiveResult;
import com.atlashub.logistics.shipping.application.commands.CreateConsolidationBatch.CreateConsolidationBatchCommand;
import com.atlashub.logistics.shipping.application.commands.CreateConsolidationBatch.CreateConsolidationBatchHandler;
import com.atlashub.logistics.shipping.application.commands.CreateConsolidationBatch.CreateConsolidationBatchResult;
import com.atlashub.logistics.shipping.application.commands.DispatchConsolidationBatch.DispatchConsolidationBatchCommand;
import com.atlashub.logistics.shipping.application.commands.DispatchConsolidationBatch.DispatchConsolidationBatchHandler;
import com.atlashub.logistics.shipping.application.commands.DispatchConsolidationBatch.DispatchConsolidationBatchResult;
import com.atlashub.logistics.shipping.application.queries.ListMerchantShippingIncentives.ListMerchantShippingIncentivesHandler;
import com.atlashub.logistics.shipping.application.queries.ListMerchantShippingIncentives.ListMerchantShippingIncentivesQuery;
import com.atlashub.logistics.shipping.application.queries.ListMerchantShippingIncentives.MerchantShippingIncentiveSummary;
import com.atlashub.logistics.shipping.presentation.dto.CalculateIncentivesRequest;
import com.atlashub.logistics.shipping.presentation.dto.CreateBatchRequest;
import com.atlashub.logistics.shipping.presentation.dto.DispatchBatchRequest;
import com.atlashub.shared.application.dto.ApiResponse;
import com.atlashub.shared.application.security.AuthenticatedPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/shipments/consolidation")
@Tag(name = "Shipment Consolidation & Incentives")
public class ConsolidationController {

    private final CreateConsolidationBatchHandler createBatchHandler;
    private final DispatchConsolidationBatchHandler dispatchBatchHandler;
    private final ListMerchantShippingIncentivesHandler listIncentivesHandler;
    private final CalculateMonthlyMerchantIncentiveHandler calculateIncentiveHandler;

    public ConsolidationController(
            CreateConsolidationBatchHandler createBatchHandler,
            DispatchConsolidationBatchHandler dispatchBatchHandler,
            ListMerchantShippingIncentivesHandler listIncentivesHandler,
            CalculateMonthlyMerchantIncentiveHandler calculateIncentiveHandler
    ) {
        this.createBatchHandler = createBatchHandler;
        this.dispatchBatchHandler = dispatchBatchHandler;
        this.listIncentivesHandler = listIncentivesHandler;
        this.calculateIncentiveHandler = calculateIncentiveHandler;
    }

    @PostMapping("/batches")
    @Operation(summary = "Create consolidation batch", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<CreateConsolidationBatchResult>> createBatch(
            @Valid @RequestBody CreateBatchRequest request
    ) {
        CreateConsolidationBatchCommand command = new CreateConsolidationBatchCommand(
                request.originState(),
                request.originCity(),
                request.destinationState(),
                request.destinationCity(),
                request.shipmentIds(),
                request.currency() != null ? request.currency() : "NGN"
        );
        CreateConsolidationBatchResult result = createBatchHandler.execute(command);
        return ResponseEntity.ok(new ApiResponse<>(true, "Consolidation batch created successfully", result, null));
    }

    @PostMapping("/batches/{id}/dispatch")
    @Operation(summary = "Dispatch consolidation batch", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<DispatchConsolidationBatchResult>> dispatchBatch(
            @PathVariable("id") Long id,
            @Valid @RequestBody DispatchBatchRequest request
    ) {
        DispatchConsolidationBatchCommand command = new DispatchConsolidationBatchCommand(
                id,
                request.channel(),
                request.carrierCode(),
                request.actualBatchCost(),
                UUID.randomUUID().toString()
        );
        DispatchConsolidationBatchResult result = dispatchBatchHandler.execute(command);
        return ResponseEntity.ok(new ApiResponse<>(true, "Consolidation batch dispatched successfully", result, null));
    }

    @GetMapping("/incentives")
    @Operation(summary = "List monthly shipping incentive rebate credits for merchant", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<List<MerchantShippingIncentiveSummary>>> listIncentives(
            @AuthenticationPrincipal AuthenticatedPrincipal principal
    ) {
        ListMerchantShippingIncentivesQuery query = new ListMerchantShippingIncentivesQuery(principal.activeOrganizationId());
        List<MerchantShippingIncentiveSummary> summaries = listIncentivesHandler.execute(query);
        return ResponseEntity.ok(new ApiResponse<>(true, "Incentive summaries retrieved successfully", summaries, null));
    }

    @PostMapping("/incentives/calculate")
    @Operation(summary = "Trigger monthly shipping incentive calculation", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<CalculateMonthlyMerchantIncentiveResult>> calculateIncentive(
            @AuthenticationPrincipal AuthenticatedPrincipal principal,
            @Valid @RequestBody CalculateIncentivesRequest request
    ) {
        YearMonth ym = YearMonth.parse(request.billingYearMonth());
        ZonedDateTime start = ym.atDay(1).atStartOfDay(ZoneId.of("UTC"));
        ZonedDateTime end = ym.plusMonths(1).atDay(1).atStartOfDay(ZoneId.of("UTC"));

        CalculateMonthlyMerchantIncentiveCommand command = new CalculateMonthlyMerchantIncentiveCommand(
                principal.activeOrganizationId(),
                request.billingYearMonth(),
                start,
                end,
                UUID.randomUUID().toString()
        );
        CalculateMonthlyMerchantIncentiveResult result = calculateIncentiveHandler.execute(command);
        return ResponseEntity.ok(new ApiResponse<>(true, "Monthly shipping incentive calculated successfully", result, null));
    }
}
