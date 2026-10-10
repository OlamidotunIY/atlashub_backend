package com.atlashub.commerce.inventory.presentation.rest;

import com.atlashub.commerce.inventory.application.commands.AdjustStock.AdjustStockCommand;
import com.atlashub.commerce.inventory.application.commands.AdjustStock.AdjustStockHandler;
import com.atlashub.commerce.inventory.application.queries.GetInventoryLevel.GetInventoryLevelHandler;
import com.atlashub.commerce.inventory.application.queries.GetInventoryLevel.GetInventoryLevelQuery;
import com.atlashub.commerce.inventory.application.queries.GetInventoryLevel.InventoryResult;
import com.atlashub.commerce.inventory.application.queries.GetStockSummary.GetStockSummaryHandler;
import com.atlashub.commerce.inventory.application.queries.GetStockSummary.GetStockSummaryQuery;
import com.atlashub.commerce.inventory.application.queries.GetStockSummary.StockSummaryResult;
import com.atlashub.commerce.inventory.application.queries.ListLowStockProducts.ListLowStockProductsHandler;
import com.atlashub.commerce.inventory.application.queries.ListLowStockProducts.ListLowStockProductsQuery;
import com.atlashub.commerce.inventory.application.queries.ListLowStockProducts.LowStockResult;
import com.atlashub.commerce.inventory.presentation.dto.AdjustStockRequest;
import com.atlashub.commerce.inventory.presentation.dto.InventoryLevelResponse;
import com.atlashub.commerce.inventory.presentation.dto.LowStockResponse;
import com.atlashub.commerce.inventory.presentation.dto.StockSummaryResponse;
import com.atlashub.shared.application.dto.ApiResponse;
import com.atlashub.shared.application.security.AuthenticatedPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/api/v1/inventory")
@Tag(name = "Inventory", description = "Stock levels, summaries, and inventory adjustments")
@SecurityRequirement(name = "bearerAuth")
public class InventoryController {

    private final GetInventoryLevelHandler getInventoryLevelHandler;
    private final GetStockSummaryHandler getStockSummaryHandler;
    private final ListLowStockProductsHandler listLowStockProductsHandler;
    private final AdjustStockHandler adjustStockHandler;

    public InventoryController(GetInventoryLevelHandler getInventoryLevelHandler,
                               GetStockSummaryHandler getStockSummaryHandler,
                               ListLowStockProductsHandler listLowStockProductsHandler,
                               AdjustStockHandler adjustStockHandler) {
        this.getInventoryLevelHandler = Objects.requireNonNull(getInventoryLevelHandler, "GetInventoryLevelHandler must not be null");
        this.getStockSummaryHandler = Objects.requireNonNull(getStockSummaryHandler, "GetStockSummaryHandler must not be null");
        this.listLowStockProductsHandler = Objects.requireNonNull(listLowStockProductsHandler, "ListLowStockProductsHandler must not be null");
        this.adjustStockHandler = Objects.requireNonNull(adjustStockHandler, "AdjustStockHandler must not be null");
    }

    @GetMapping
    @Operation(summary = "Get inventory level for a product at an outlet")
    public ResponseEntity<ApiResponse<InventoryLevelResponse>> getInventoryLevel(
            @AuthenticationPrincipal AuthenticatedPrincipal principal,
            @RequestParam Long outletId,
            @RequestParam Long productId,
            @RequestParam(required = false) Long variantId) {
        InventoryResult result = getInventoryLevelHandler.execute(
                new GetInventoryLevelQuery(principal.activeOrganizationId(), outletId, productId, variantId)
        );
        InventoryLevelResponse response = new InventoryLevelResponse(
                result.id(),
                result.productId(),
                result.outletId(),
                result.quantity(),
                result.reservedQuantity(),
                result.availableQuantity(),
                result.reorderLevel(),
                result.safeStock(),
                result.isLowStock()
        );
        return ResponseEntity.ok(new ApiResponse<>(true, "Inventory level retrieved successfully", response, null));
    }

    @GetMapping("/summary")
    @Operation(summary = "Get stock summary metrics for an outlet")
    public ResponseEntity<ApiResponse<StockSummaryResponse>> getStockSummary(
            @AuthenticationPrincipal AuthenticatedPrincipal principal,
            @RequestParam Long outletId) {
        StockSummaryResult result = getStockSummaryHandler.execute(
                new GetStockSummaryQuery(principal.activeOrganizationId(), outletId)
        );
        StockSummaryResponse response = new StockSummaryResponse(
                result.outletId(),
                result.totalSkus(),
                result.totalQuantity(),
                result.lowStockCount(),
                result.outOfStockCount()
        );
        return ResponseEntity.ok(new ApiResponse<>(true, "Stock summary retrieved successfully", response, null));
    }

    @GetMapping("/low-stock")
    @Operation(summary = "List low stock products for an outlet")
    public ResponseEntity<ApiResponse<List<LowStockResponse>>> listLowStockProducts(
            @AuthenticationPrincipal AuthenticatedPrincipal principal,
            @RequestParam Long outletId) {
        List<LowStockResult> results = listLowStockProductsHandler.execute(
                new ListLowStockProductsQuery(principal.activeOrganizationId(), outletId)
        );
        List<LowStockResponse> response = results.stream()
                .map(r -> new LowStockResponse(r.id(), r.productId(), r.variantId(), r.outletId(), r.quantity(), r.reorderLevel(), r.safeStock()))
                .toList();
        return ResponseEntity.ok(new ApiResponse<>(true, "Low stock products retrieved successfully", response, null));
    }

    @PostMapping("/adjust")
    @Operation(summary = "Manually adjust stock quantity")
    public ResponseEntity<ApiResponse<Void>> adjustStock(
            @AuthenticationPrincipal AuthenticatedPrincipal principal,
            @Valid @RequestBody AdjustStockRequest request) {
        adjustStockHandler.execute(new AdjustStockCommand(
                request.inventoryId(),
                request.newQuantity(),
                request.reason(),
                principal.userId()
        ));
        return ResponseEntity.ok(new ApiResponse<>(true, "Stock adjusted successfully", null, null));
    }
}
