package com.atlashub.logistics.warehousing.presentation.rest;

import com.atlashub.logistics.warehousing.application.command.AdjustWarehouseStock.AdjustWarehouseStockCommand;
import com.atlashub.logistics.warehousing.application.command.AdjustWarehouseStock.AdjustWarehouseStockHandler;
import com.atlashub.logistics.warehousing.application.command.AdjustWarehouseStock.AdjustWarehouseStockResult;
import com.atlashub.logistics.warehousing.application.command.ApproveStockTransfer.ApproveStockTransferCommand;
import com.atlashub.logistics.warehousing.application.command.ApproveStockTransfer.ApproveStockTransferHandler;
import com.atlashub.logistics.warehousing.application.command.ApproveStockTransfer.ApproveStockTransferResult;
import com.atlashub.logistics.warehousing.application.command.DispatchStockTransfer.DispatchStockTransferCommand;
import com.atlashub.logistics.warehousing.application.command.DispatchStockTransfer.DispatchStockTransferHandler;
import com.atlashub.logistics.warehousing.application.command.DispatchStockTransfer.DispatchStockTransferResult;
import com.atlashub.logistics.warehousing.application.command.ReceiveStockTransfer.ReceiveStockTransferCommand;
import com.atlashub.logistics.warehousing.application.command.ReceiveStockTransfer.ReceiveStockTransferHandler;
import com.atlashub.logistics.warehousing.application.command.ReceiveStockTransfer.ReceiveStockTransferResult;
import com.atlashub.logistics.warehousing.application.command.RegisterWarehouse.RegisterWarehouseCommand;
import com.atlashub.logistics.warehousing.application.command.RegisterWarehouse.RegisterWarehouseHandler;
import com.atlashub.logistics.warehousing.application.command.RegisterWarehouse.RegisterWarehouseResult;
import com.atlashub.logistics.warehousing.application.command.RequestStockTransfer.RequestStockTransferCommand;
import com.atlashub.logistics.warehousing.application.command.RequestStockTransfer.RequestStockTransferHandler;
import com.atlashub.logistics.warehousing.application.command.RequestStockTransfer.RequestStockTransferResult;
import com.atlashub.logistics.warehousing.application.query.GetStockTransfer.GetStockTransferHandler;
import com.atlashub.logistics.warehousing.application.query.GetStockTransfer.GetStockTransferQuery;
import com.atlashub.logistics.warehousing.application.query.GetStockTransfer.StockTransferResult;
import com.atlashub.logistics.warehousing.application.query.GetWarehouse.GetWarehouseHandler;
import com.atlashub.logistics.warehousing.application.query.GetWarehouse.GetWarehouseQuery;
import com.atlashub.logistics.warehousing.application.query.GetWarehouse.WarehouseResult;
import com.atlashub.logistics.warehousing.application.query.GetWarehouseInventory.GetWarehouseInventoryHandler;
import com.atlashub.logistics.warehousing.application.query.GetWarehouseInventory.GetWarehouseInventoryQuery;
import com.atlashub.logistics.warehousing.application.query.GetWarehouseInventory.WarehouseInventoryResult;
import com.atlashub.logistics.warehousing.application.query.ListStockTransfers.ListStockTransfersHandler;
import com.atlashub.logistics.warehousing.application.query.ListStockTransfers.ListStockTransfersQuery;
import com.atlashub.logistics.warehousing.application.query.ListWarehouses.ListWarehousesHandler;
import com.atlashub.logistics.warehousing.application.query.ListWarehouses.ListWarehousesQuery;
import com.atlashub.logistics.warehousing.presentation.dto.AdjustStockRequest;
import com.atlashub.logistics.warehousing.presentation.dto.ApproveStockTransferRequest;
import com.atlashub.logistics.warehousing.presentation.dto.DispatchStockTransferRequest;
import com.atlashub.logistics.warehousing.presentation.dto.ReceiveStockTransferRequest;
import com.atlashub.logistics.warehousing.presentation.dto.RegisterWarehouseRequest;
import com.atlashub.logistics.warehousing.presentation.dto.RequestStockTransferRequest;
import com.atlashub.logistics.warehousing.presentation.dto.StockTransferResponse;
import com.atlashub.logistics.warehousing.presentation.dto.WarehouseInventoryResponse;
import com.atlashub.logistics.warehousing.presentation.dto.WarehouseResponse;
import com.atlashub.shared.application.security.AuthenticatedPrincipal;
import com.atlashub.shared.application.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Logistics Warehousing", description = "Endpoints for managing warehouses, stock levels, and inter-outlet stock transfers")
public class WarehouseController {

    private final RegisterWarehouseHandler registerWarehouseHandler;
    private final AdjustWarehouseStockHandler adjustWarehouseStockHandler;
    private final RequestStockTransferHandler requestStockTransferHandler;
    private final ApproveStockTransferHandler approveStockTransferHandler;
    private final DispatchStockTransferHandler dispatchStockTransferHandler;
    private final ReceiveStockTransferHandler receiveStockTransferHandler;

    private final GetWarehouseHandler getWarehouseHandler;
    private final ListWarehousesHandler listWarehousesHandler;
    private final GetWarehouseInventoryHandler getWarehouseInventoryHandler;
    private final GetStockTransferHandler getStockTransferHandler;
    private final ListStockTransfersHandler listStockTransfersHandler;

    public WarehouseController(
            RegisterWarehouseHandler registerWarehouseHandler,
            AdjustWarehouseStockHandler adjustWarehouseStockHandler,
            RequestStockTransferHandler requestStockTransferHandler,
            ApproveStockTransferHandler approveStockTransferHandler,
            DispatchStockTransferHandler dispatchStockTransferHandler,
            ReceiveStockTransferHandler receiveStockTransferHandler,
            GetWarehouseHandler getWarehouseHandler,
            ListWarehousesHandler listWarehousesHandler,
            GetWarehouseInventoryHandler getWarehouseInventoryHandler,
            GetStockTransferHandler getStockTransferHandler,
            ListStockTransfersHandler listStockTransfersHandler
    ) {
        this.registerWarehouseHandler = registerWarehouseHandler;
        this.adjustWarehouseStockHandler = adjustWarehouseStockHandler;
        this.requestStockTransferHandler = requestStockTransferHandler;
        this.approveStockTransferHandler = approveStockTransferHandler;
        this.dispatchStockTransferHandler = dispatchStockTransferHandler;
        this.receiveStockTransferHandler = receiveStockTransferHandler;
        this.getWarehouseHandler = getWarehouseHandler;
        this.listWarehousesHandler = listWarehousesHandler;
        this.getWarehouseInventoryHandler = getWarehouseInventoryHandler;
        this.getStockTransferHandler = getStockTransferHandler;
        this.listStockTransfersHandler = listStockTransfersHandler;
    }

    @PostMapping("/warehouses")
    @Operation(summary = "Register a new warehouse or distribution center", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<RegisterWarehouseResult>> registerWarehouse(
            @AuthenticationPrincipal AuthenticatedPrincipal principal,
            @Valid @RequestBody RegisterWarehouseRequest request
    ) {
        RegisterWarehouseCommand command = new RegisterWarehouseCommand(
                principal.activeOrganizationId(),
                request.name(),
                request.code(),
                request.facilityType(),
                request.address(),
                request.managerId(),
                UUID.randomUUID().toString()
        );
        RegisterWarehouseResult result = registerWarehouseHandler.execute(command);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "Warehouse registered successfully", result, null));
    }

    @GetMapping("/warehouses")
    @Operation(summary = "Get a warehouse by query ID or list all warehouses for organization", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<Object>> getWarehouses(
            @AuthenticationPrincipal AuthenticatedPrincipal principal,
            @RequestParam(name = "id", required = false) Long warehouseId
    ) {
        if (warehouseId != null) {
            WarehouseResult result = getWarehouseHandler.execute(new GetWarehouseQuery(principal.activeOrganizationId(), warehouseId));
            WarehouseResponse response = new WarehouseResponse(
                    result.id(),
                    result.organizationId(),
                    result.name(),
                    result.code(),
                    result.facilityType(),
                    result.address(),
                    result.managerId(),
                    result.status(),
                    result.createdAt()
            );
            return ResponseEntity.ok(new ApiResponse<>(true, "Warehouse retrieved successfully", response, null));
        }

        List<WarehouseResult> results = listWarehousesHandler.execute(new ListWarehousesQuery(principal.activeOrganizationId()));
        List<WarehouseResponse> responses = results.stream()
                .map(r -> new WarehouseResponse(
                        r.id(),
                        r.organizationId(),
                        r.name(),
                        r.code(),
                        r.facilityType(),
                        r.address(),
                        r.managerId(),
                        r.status(),
                        r.createdAt()
                ))
                .toList();
        return ResponseEntity.ok(new ApiResponse<>(true, "Warehouses retrieved successfully", responses, null));
    }

    @PostMapping("/warehouses/{id}/inventory")
    @Operation(summary = "Adjust inventory stock count in warehouse", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<AdjustWarehouseStockResult>> adjustInventory(
            @AuthenticationPrincipal AuthenticatedPrincipal principal,
            @PathVariable("id") Long warehouseId,
            @Valid @RequestBody AdjustStockRequest request
    ) {
        AdjustWarehouseStockCommand command = new AdjustWarehouseStockCommand(
                principal.activeOrganizationId(),
                warehouseId,
                request.productId(),
                request.newQuantity(),
                UUID.randomUUID().toString()
        );
        AdjustWarehouseStockResult result = adjustWarehouseStockHandler.execute(command);
        return ResponseEntity.ok(new ApiResponse<>(true, "Warehouse stock adjusted successfully", result, null));
    }

    @GetMapping("/warehouses/{id}/inventory")
    @Operation(summary = "List all inventory products and quantities in warehouse", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<List<WarehouseInventoryResponse>>> getWarehouseInventory(
            @AuthenticationPrincipal AuthenticatedPrincipal principal,
            @PathVariable("id") Long warehouseId
    ) {
        List<WarehouseInventoryResult> results = getWarehouseInventoryHandler.execute(
                new GetWarehouseInventoryQuery(principal.activeOrganizationId(), warehouseId));

        List<WarehouseInventoryResponse> responses = results.stream()
                .map(r -> new WarehouseInventoryResponse(
                        r.id(),
                        r.organizationId(),
                        r.warehouseId(),
                        r.productId(),
                        r.quantity(),
                        r.reservedQuantity(),
                        r.availableQuantity(),
                        r.updatedAt()
                ))
                .toList();

        return ResponseEntity.ok(new ApiResponse<>(true, "Warehouse inventory retrieved successfully", responses, null));
    }

    @PostMapping("/stock-transfers")
    @Operation(summary = "Request an inter-outlet or warehouse stock transfer", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<RequestStockTransferResult>> requestStockTransfer(
            @AuthenticationPrincipal AuthenticatedPrincipal principal,
            @Valid @RequestBody RequestStockTransferRequest request
    ) {
        RequestStockTransferCommand command = new RequestStockTransferCommand(
                principal.activeOrganizationId(),
                request.sourceId(),
                request.sourceType(),
                request.sourceAddress(),
                request.destinationId(),
                request.destinationType(),
                request.destinationAddress(),
                request.transferShippingFee(),
                request.items(),
                principal.userId(),
                UUID.randomUUID().toString()
        );
        RequestStockTransferResult result = requestStockTransferHandler.execute(command);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "Stock transfer requested successfully", result, null));
    }

    @PostMapping("/stock-transfers/{id}/approve")
    @Operation(summary = "Approve a requested stock transfer", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<ApproveStockTransferResult>> approveStockTransfer(
            @AuthenticationPrincipal AuthenticatedPrincipal principal,
            @PathVariable("id") Long transferId,
            @Valid @RequestBody ApproveStockTransferRequest request
    ) {
        ApproveStockTransferCommand command = new ApproveStockTransferCommand(
                principal.activeOrganizationId(),
                transferId,
                principal.userId(),
                request.declaredValue(),
                UUID.randomUUID().toString()
        );
        ApproveStockTransferResult result = approveStockTransferHandler.execute(command);
        return ResponseEntity.ok(new ApiResponse<>(true, "Stock transfer approved successfully", result, null));
    }

    @PostMapping("/stock-transfers/{id}/dispatch")
    @Operation(summary = "Mark stock transfer as dispatched and link shipment", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<DispatchStockTransferResult>> dispatchStockTransfer(
            @AuthenticationPrincipal AuthenticatedPrincipal principal,
            @PathVariable("id") Long transferId,
            @Valid @RequestBody DispatchStockTransferRequest request
    ) {
        DispatchStockTransferCommand command = new DispatchStockTransferCommand(
                principal.activeOrganizationId(),
                transferId,
                request.shipmentId(),
                UUID.randomUUID().toString()
        );
        DispatchStockTransferResult result = dispatchStockTransferHandler.execute(command);
        return ResponseEntity.ok(new ApiResponse<>(true, "Stock transfer marked as dispatched", result, null));
    }

    @PostMapping("/stock-transfers/{id}/receive")
    @Operation(summary = "Mark stock transfer as received at destination", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<ReceiveStockTransferResult>> receiveStockTransfer(
            @AuthenticationPrincipal AuthenticatedPrincipal principal,
            @PathVariable("id") Long transferId,
            @Valid @RequestBody ReceiveStockTransferRequest request
    ) {
        ReceiveStockTransferCommand command = new ReceiveStockTransferCommand(
                principal.activeOrganizationId(),
                transferId,
                request.receivedAt(),
                UUID.randomUUID().toString()
        );
        ReceiveStockTransferResult result = receiveStockTransferHandler.execute(command);
        return ResponseEntity.ok(new ApiResponse<>(true, "Stock transfer marked as received", result, null));
    }

    @GetMapping("/stock-transfers")
    @Operation(summary = "Get a stock transfer by query ID or list transfers for organization", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<Object>> getStockTransfers(
            @AuthenticationPrincipal AuthenticatedPrincipal principal,
            @RequestParam(name = "id", required = false) Long transferId
    ) {
        if (transferId != null) {
            StockTransferResult result = getStockTransferHandler.execute(
                    new GetStockTransferQuery(principal.activeOrganizationId(), transferId));
            StockTransferResponse response = new StockTransferResponse(
                    result.id(),
                    result.organizationId(),
                    result.sourceId(),
                    result.sourceType(),
                    result.sourceAddress(),
                    result.destinationId(),
                    result.destinationType(),
                    result.destinationAddress(),
                    result.totalWeightKg(),
                    result.transferShippingFee(),
                    result.status(),
                    result.shipmentId(),
                    result.items(),
                    result.requestedBy(),
                    result.approvedBy(),
                    result.requestedAt(),
                    result.receivedAt()
            );
            return ResponseEntity.ok(new ApiResponse<>(true, "Stock transfer retrieved successfully", response, null));
        }

        List<StockTransferResult> results = listStockTransfersHandler.execute(
                new ListStockTransfersQuery(principal.activeOrganizationId()));
        List<StockTransferResponse> responses = results.stream()
                .map(r -> new StockTransferResponse(
                        r.id(),
                        r.organizationId(),
                        r.sourceId(),
                        r.sourceType(),
                        r.sourceAddress(),
                        r.destinationId(),
                        r.destinationType(),
                        r.destinationAddress(),
                        r.totalWeightKg(),
                        r.transferShippingFee(),
                        r.status(),
                        r.shipmentId(),
                        r.items(),
                        r.requestedBy(),
                        r.approvedBy(),
                        r.requestedAt(),
                        r.receivedAt()
                ))
                .toList();

        return ResponseEntity.ok(new ApiResponse<>(true, "Stock transfers retrieved successfully", responses, null));
    }
}
