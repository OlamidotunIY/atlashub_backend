package com.atlashub.commerce.inventory.presentation.rest;

import com.atlashub.commerce.inventory.application.commands.InitiateStockCount.InitiateStockCountCommand;
import com.atlashub.commerce.inventory.application.commands.InitiateStockCount.InitiateStockCountHandler;
import com.atlashub.commerce.inventory.application.commands.InitiateStockCount.InitiateStockCountResult;
import com.atlashub.commerce.inventory.application.commands.ReconcileStockCount.CountedItemDto;
import com.atlashub.commerce.inventory.application.commands.ReconcileStockCount.ReconcileStockCountCommand;
import com.atlashub.commerce.inventory.application.commands.ReconcileStockCount.ReconcileStockCountHandler;
import com.atlashub.commerce.inventory.presentation.dto.InitiateStockCountRequest;
import com.atlashub.commerce.inventory.presentation.dto.InitiateStockCountResponse;
import com.atlashub.commerce.inventory.presentation.dto.ReconcileStockCountRequest;
import com.atlashub.shared.application.dto.ApiResponse;
import com.atlashub.shared.application.security.AuthenticatedPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/api/v1/stock-counts")
@Tag(name = "Stock Counts", description = "Physical stock count initiation and reconciliation")
@SecurityRequirement(name = "bearerAuth")
public class StockCountController {

    private final InitiateStockCountHandler initiateStockCountHandler;
    private final ReconcileStockCountHandler reconcileStockCountHandler;

    public StockCountController(InitiateStockCountHandler initiateStockCountHandler,
                                ReconcileStockCountHandler reconcileStockCountHandler) {
        this.initiateStockCountHandler = Objects.requireNonNull(initiateStockCountHandler, "InitiateStockCountHandler must not be null");
        this.reconcileStockCountHandler = Objects.requireNonNull(reconcileStockCountHandler, "ReconcileStockCountHandler must not be null");
    }

    @PostMapping
    @Operation(summary = "Initiate a new stock count for an outlet")
    public ResponseEntity<ApiResponse<InitiateStockCountResponse>> initiateStockCount(
            @AuthenticationPrincipal AuthenticatedPrincipal principal,
            @Valid @RequestBody InitiateStockCountRequest request) {
        InitiateStockCountResult result = initiateStockCountHandler.execute(
                new InitiateStockCountCommand(principal.activeOrganizationId(), request.outletId(), principal.userId())
        );
        InitiateStockCountResponse response = new InitiateStockCountResponse(result.stockCountId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "Stock count initiated successfully", response, null));
    }

    @PostMapping("/{id}/reconcile")
    @Operation(summary = "Reconcile counted stock against system quantities")
    public ResponseEntity<ApiResponse<Void>> reconcileStockCount(
            @AuthenticationPrincipal AuthenticatedPrincipal principal,
            @PathVariable Long id,
            @Valid @RequestBody ReconcileStockCountRequest request) {
        List<CountedItemDto> countedItems = request.items().stream()
                .map(i -> new CountedItemDto(i.inventoryId(), i.countedQuantity()))
                .toList();

        reconcileStockCountHandler.execute(new ReconcileStockCountCommand(id, countedItems, principal.userId()));
        return ResponseEntity.ok(new ApiResponse<>(true, "Stock count reconciled successfully", null, null));
    }
}
