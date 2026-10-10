package com.atlashub.commerce.storefront.presentation.rest;

import com.atlashub.commerce.storefront.application.commands.ClearTable.ClearTableCommand;
import com.atlashub.commerce.storefront.application.commands.ClearTable.ClearTableHandler;
import com.atlashub.commerce.storefront.application.commands.MarkKotReady.MarkKotReadyCommand;
import com.atlashub.commerce.storefront.application.commands.MarkKotReady.MarkKotReadyHandler;
import com.atlashub.commerce.storefront.application.commands.MarkKotServed.MarkKotServedCommand;
import com.atlashub.commerce.storefront.application.commands.MarkKotServed.MarkKotServedHandler;
import com.atlashub.commerce.storefront.application.commands.OccupyTable.OccupyTableCommand;
import com.atlashub.commerce.storefront.application.commands.OccupyTable.OccupyTableHandler;
import com.atlashub.commerce.storefront.application.commands.RequestTableBill.RequestTableBillCommand;
import com.atlashub.commerce.storefront.application.commands.RequestTableBill.RequestTableBillHandler;
import com.atlashub.commerce.storefront.application.commands.SendKitchenOrder.KotItemDto;
import com.atlashub.commerce.storefront.application.commands.SendKitchenOrder.SendKitchenOrderCommand;
import com.atlashub.commerce.storefront.application.commands.SendKitchenOrder.SendKitchenOrderHandler;
import com.atlashub.commerce.storefront.application.commands.SendKitchenOrder.SendKitchenOrderResult;
import com.atlashub.commerce.storefront.application.queries.ListActiveTables.ListActiveTablesHandler;
import com.atlashub.commerce.storefront.application.queries.ListActiveTables.ListActiveTablesQuery;
import com.atlashub.commerce.storefront.application.queries.ListActiveTables.TableResult;
import com.atlashub.commerce.storefront.presentation.dto.KotItemRequest;
import com.atlashub.commerce.storefront.presentation.dto.OccupyTableRequest;
import com.atlashub.commerce.storefront.presentation.dto.SendKitchenOrderRequest;
import com.atlashub.commerce.storefront.presentation.dto.SendKitchenOrderResponse;
import com.atlashub.shared.application.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Commerce Hospitality", description = "Tables, seating, and kitchen order ticket (KOT) operations")
@SecurityRequirement(name = "bearerAuth")
public class CommerceHospitalityController {

    private final OccupyTableHandler occupyTableHandler;
    private final RequestTableBillHandler requestTableBillHandler;
    private final ClearTableHandler clearTableHandler;
    private final ListActiveTablesHandler listActiveTablesHandler;
    private final SendKitchenOrderHandler sendKitchenOrderHandler;
    private final MarkKotReadyHandler markKotReadyHandler;
    private final MarkKotServedHandler markKotServedHandler;

    public CommerceHospitalityController(OccupyTableHandler occupyTableHandler,
                                         RequestTableBillHandler requestTableBillHandler,
                                         ClearTableHandler clearTableHandler,
                                         ListActiveTablesHandler listActiveTablesHandler,
                                         SendKitchenOrderHandler sendKitchenOrderHandler,
                                         MarkKotReadyHandler markKotReadyHandler,
                                         MarkKotServedHandler markKotServedHandler) {
        this.occupyTableHandler = Objects.requireNonNull(occupyTableHandler, "OccupyTableHandler must not be null");
        this.requestTableBillHandler =
                Objects.requireNonNull(requestTableBillHandler, "RequestTableBillHandler must not be null");
        this.clearTableHandler = Objects.requireNonNull(clearTableHandler, "ClearTableHandler must not be null");
        this.listActiveTablesHandler =
                Objects.requireNonNull(listActiveTablesHandler, "ListActiveTablesHandler must not be null");
        this.sendKitchenOrderHandler =
                Objects.requireNonNull(sendKitchenOrderHandler, "SendKitchenOrderHandler must not be null");
        this.markKotReadyHandler = Objects.requireNonNull(markKotReadyHandler, "MarkKotReadyHandler must not be null");
        this.markKotServedHandler =
                Objects.requireNonNull(markKotServedHandler, "MarkKotServedHandler must not be null");
    }

    @PostMapping("/tables/{id}/occupy")
    @Operation(summary = "Occupy hospitality table")
    public ResponseEntity<ApiResponse<Void>> occupyTable(@PathVariable Long id,
                                                         @Valid @RequestBody OccupyTableRequest request) {
        occupyTableHandler.execute(new OccupyTableCommand(id, request.salesOrderId(), request.covers()));
        return ResponseEntity.ok(new ApiResponse<>(true, "Table occupied successfully", null, null));
    }

    @PostMapping("/tables/{id}/bill")
    @Operation(summary = "Request bill for hospitality table")
    public ResponseEntity<ApiResponse<Void>> requestBill(@PathVariable Long id) {
        requestTableBillHandler.execute(new RequestTableBillCommand(id));
        return ResponseEntity.ok(new ApiResponse<>(true, "Table bill requested successfully", null, null));
    }

    @PostMapping("/tables/{id}/clear")
    @Operation(summary = "Clear hospitality table")
    public ResponseEntity<ApiResponse<Void>> clearTable(@PathVariable Long id) {
        clearTableHandler.execute(new ClearTableCommand(id));
        return ResponseEntity.ok(new ApiResponse<>(true, "Table cleared successfully", null, null));
    }

    @GetMapping("/tables")
    @Operation(summary = "List active hospitality tables for outlet")
    public ResponseEntity<ApiResponse<List<TableResult>>> listActiveTables(@RequestParam Long outletId) {
        List<TableResult> result = listActiveTablesHandler.execute(new ListActiveTablesQuery(outletId));
        return ResponseEntity.ok(new ApiResponse<>(true, "Active tables retrieved successfully", result, null));
    }

    @PostMapping("/kitchen-orders")
    @Operation(summary = "Send kitchen order ticket")
    public ResponseEntity<ApiResponse<SendKitchenOrderResponse>> sendKitchenOrder(
            @Valid @RequestBody SendKitchenOrderRequest request) {
        List<KotItemDto> items = request.items().stream().map(this::toKotItemDto).toList();

        SendKitchenOrderCommand command =
                new SendKitchenOrderCommand(request.salesOrderId(), request.tableId(), request.outletId(), items);

        SendKitchenOrderResult result = sendKitchenOrderHandler.execute(command);
        SendKitchenOrderResponse response = new SendKitchenOrderResponse(result.kotId());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "Kitchen order sent successfully", response, null));
    }

    @PostMapping("/kitchen-orders/{id}/ready")
    @Operation(summary = "Mark kitchen order ready")
    public ResponseEntity<ApiResponse<Void>> markKotReady(@PathVariable Long id) {
        markKotReadyHandler.execute(new MarkKotReadyCommand(id));
        return ResponseEntity.ok(new ApiResponse<>(true, "Kitchen order marked ready", null, null));
    }

    @PostMapping("/kitchen-orders/{id}/served")
    @Operation(summary = "Mark kitchen order served")
    public ResponseEntity<ApiResponse<Void>> markKotServed(@PathVariable Long id) {
        markKotServedHandler.execute(new MarkKotServedCommand(id));
        return ResponseEntity.ok(new ApiResponse<>(true, "Kitchen order marked served", null, null));
    }

    private KotItemDto toKotItemDto(KotItemRequest item) {
        return new KotItemDto(item.productId(), item.name(), item.quantity());
    }
}
