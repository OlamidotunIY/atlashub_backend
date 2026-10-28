package com.atlashub.logistics.returns.presentation.rest;

import com.atlashub.logistics.returns.application.command.CreateReturnShipment.CreateReturnShipmentCommand;
import com.atlashub.logistics.returns.application.command.CreateReturnShipment.CreateReturnShipmentHandler;
import com.atlashub.logistics.returns.application.command.CreateReturnShipment.CreateReturnShipmentResult;
import com.atlashub.logistics.returns.application.command.MarkReturnInTransit.MarkReturnInTransitCommand;
import com.atlashub.logistics.returns.application.command.MarkReturnInTransit.MarkReturnInTransitHandler;
import com.atlashub.logistics.returns.application.command.MarkReturnInTransit.MarkReturnInTransitResult;
import com.atlashub.logistics.returns.application.command.ReceiveReturnShipment.ReceiveReturnShipmentCommand;
import com.atlashub.logistics.returns.application.command.ReceiveReturnShipment.ReceiveReturnShipmentHandler;
import com.atlashub.logistics.returns.application.command.ReceiveReturnShipment.ReceiveReturnShipmentResult;
import com.atlashub.logistics.returns.application.command.RejectReturnShipment.RejectReturnShipmentCommand;
import com.atlashub.logistics.returns.application.command.RejectReturnShipment.RejectReturnShipmentHandler;
import com.atlashub.logistics.returns.application.command.RejectReturnShipment.RejectReturnShipmentResult;
import com.atlashub.logistics.returns.application.query.GetReturnShipment.GetReturnShipmentHandler;
import com.atlashub.logistics.returns.application.query.GetReturnShipment.GetReturnShipmentQuery;
import com.atlashub.logistics.returns.application.query.GetReturnShipment.ReturnShipmentResult;
import com.atlashub.logistics.returns.application.query.ListReturnShipments.ListReturnShipmentsHandler;
import com.atlashub.logistics.returns.application.query.ListReturnShipments.ListReturnShipmentsQuery;
import com.atlashub.logistics.returns.presentation.dto.CreateReturnShipmentRequest;
import com.atlashub.logistics.returns.presentation.dto.ReceiveReturnShipmentRequest;
import com.atlashub.logistics.returns.presentation.dto.RejectReturnShipmentRequest;
import com.atlashub.logistics.returns.presentation.dto.ReturnShipmentResponse;
import com.atlashub.shared.application.dto.ApiResponse;
import com.atlashub.shared.application.security.AuthenticatedPrincipal;
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
@RequestMapping("/api/v1/logistics")
@Tag(name = "Logistics Returns", description = "Endpoints for reverse logistics and the 3-part failed delivery and return policy")
public class ReturnsController {

    private final CreateReturnShipmentHandler createReturnShipmentHandler;
    private final MarkReturnInTransitHandler markReturnInTransitHandler;
    private final ReceiveReturnShipmentHandler receiveReturnShipmentHandler;
    private final RejectReturnShipmentHandler rejectReturnShipmentHandler;
    private final GetReturnShipmentHandler getReturnShipmentHandler;
    private final ListReturnShipmentsHandler listReturnShipmentsHandler;

    public ReturnsController(
            CreateReturnShipmentHandler createReturnShipmentHandler,
            MarkReturnInTransitHandler markReturnInTransitHandler,
            ReceiveReturnShipmentHandler receiveReturnShipmentHandler,
            RejectReturnShipmentHandler rejectReturnShipmentHandler,
            GetReturnShipmentHandler getReturnShipmentHandler,
            ListReturnShipmentsHandler listReturnShipmentsHandler
    ) {
        this.createReturnShipmentHandler = createReturnShipmentHandler;
        this.markReturnInTransitHandler = markReturnInTransitHandler;
        this.receiveReturnShipmentHandler = receiveReturnShipmentHandler;
        this.rejectReturnShipmentHandler = rejectReturnShipmentHandler;
        this.getReturnShipmentHandler = getReturnShipmentHandler;
        this.listReturnShipmentsHandler = listReturnShipmentsHandler;
    }

    @PostMapping("/returns")
    @Operation(summary = "Create return or failed-delivery RTO shipment with 3-part policy financial resolution", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<CreateReturnShipmentResult>> createReturn(
            @AuthenticationPrincipal AuthenticatedPrincipal principal,
            @Valid @RequestBody CreateReturnShipmentRequest request
    ) {
        CreateReturnShipmentCommand command = new CreateReturnShipmentCommand(
                principal.activeOrganizationId(),
                request.originalSalesOrderId(),
                request.originalShipmentId(),
                request.customerId(),
                request.originAddress(),
                request.destinationOutletId(),
                request.reasonType(),
                request.faultType(),
                request.deliveryAttemptsConfirmed(),
                request.originalItemAmount(),
                request.originalShippingFee(),
                request.returnShippingFee(),
                request.declaredValue(),
                UUID.randomUUID().toString()
        );

        CreateReturnShipmentResult result = createReturnShipmentHandler.execute(command);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "Return shipment created successfully", result, null));
    }

    @PostMapping("/returns/{id}/in-transit")
    @Operation(summary = "Mark return shipment in transit", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<MarkReturnInTransitResult>> markInTransit(
            @AuthenticationPrincipal AuthenticatedPrincipal principal,
            @PathVariable("id") Long returnShipmentId
    ) {
        MarkReturnInTransitCommand command = new MarkReturnInTransitCommand(
                principal.activeOrganizationId(),
                returnShipmentId,
                UUID.randomUUID().toString()
        );

        MarkReturnInTransitResult result = markReturnInTransitHandler.execute(command);
        return ResponseEntity.ok(new ApiResponse<>(true, "Return shipment marked in transit", result, null));
    }

    @PostMapping("/returns/{id}/receive")
    @Operation(summary = "Confirm return received at merchant outlet and release eligible refund", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<ReceiveReturnShipmentResult>> receiveReturn(
            @AuthenticationPrincipal AuthenticatedPrincipal principal,
            @PathVariable("id") Long returnShipmentId,
            @Valid @RequestBody ReceiveReturnShipmentRequest request
    ) {
        ReceiveReturnShipmentCommand command = new ReceiveReturnShipmentCommand(
                principal.activeOrganizationId(),
                returnShipmentId,
                request.inspectionNotes(),
                request.receivedAt(),
                UUID.randomUUID().toString()
        );

        ReceiveReturnShipmentResult result = receiveReturnShipmentHandler.execute(command);
        return ResponseEntity.ok(new ApiResponse<>(true, "Return shipment received successfully", result, null));
    }

    @PostMapping("/returns/{id}/reject")
    @Operation(summary = "Reject return after inspection", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<RejectReturnShipmentResult>> rejectReturn(
            @AuthenticationPrincipal AuthenticatedPrincipal principal,
            @PathVariable("id") Long returnShipmentId,
            @Valid @RequestBody RejectReturnShipmentRequest request
    ) {
        RejectReturnShipmentCommand command = new RejectReturnShipmentCommand(
                principal.activeOrganizationId(),
                returnShipmentId,
                request.reason(),
                UUID.randomUUID().toString()
        );

        RejectReturnShipmentResult result = rejectReturnShipmentHandler.execute(command);
        return ResponseEntity.ok(new ApiResponse<>(true, "Return shipment rejected", result, null));
    }

    @GetMapping("/returns/details")
    @Operation(summary = "Get return shipment details and financial resolution", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<ReturnShipmentResponse>> getReturnDetails(
            @AuthenticationPrincipal AuthenticatedPrincipal principal,
            @RequestParam("id") Long returnShipmentId
    ) {
        ReturnShipmentResult result = getReturnShipmentHandler.execute(
                new GetReturnShipmentQuery(principal.activeOrganizationId(), returnShipmentId));

        ReturnShipmentResponse response = new ReturnShipmentResponse(
                result.id(),
                result.organizationId(),
                result.originalSalesOrderId(),
                result.originalShipmentId(),
                result.customerId(),
                result.originAddress(),
                result.destinationOutletId(),
                result.returnTrackingNumber(),
                result.reasonType(),
                result.faultType(),
                result.deliveryAttemptsConfirmed(),
                result.originalItemAmount(),
                result.originalShippingFee(),
                result.returnShippingFee(),
                result.declaredValue(),
                result.customerRefundAmount(),
                result.merchantPenaltyAmount(),
                result.merchantCompensationAmount(),
                result.refundCustomerShipping(),
                result.status(),
                result.inspectionNotes(),
                result.createdAt(),
                result.receivedAt()
        );

        return ResponseEntity.ok(new ApiResponse<>(true, "Return shipment retrieved successfully", response, null));
    }

    @GetMapping("/returns")
    @Operation(summary = "List return shipments for organization", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<List<ReturnShipmentResponse>>> listReturns(
            @AuthenticationPrincipal AuthenticatedPrincipal principal
    ) {
        List<ReturnShipmentResult> results = listReturnShipmentsHandler.execute(
                new ListReturnShipmentsQuery(principal.activeOrganizationId()));

        List<ReturnShipmentResponse> responses = results.stream()
                .map(r -> new ReturnShipmentResponse(
                        r.id(),
                        r.organizationId(),
                        r.originalSalesOrderId(),
                        r.originalShipmentId(),
                        r.customerId(),
                        r.originAddress(),
                        r.destinationOutletId(),
                        r.returnTrackingNumber(),
                        r.reasonType(),
                        r.faultType(),
                        r.deliveryAttemptsConfirmed(),
                        r.originalItemAmount(),
                        r.originalShippingFee(),
                        r.returnShippingFee(),
                        r.declaredValue(),
                        r.customerRefundAmount(),
                        r.merchantPenaltyAmount(),
                        r.merchantCompensationAmount(),
                        r.refundCustomerShipping(),
                        r.status(),
                        r.inspectionNotes(),
                        r.createdAt(),
                        r.receivedAt()
                ))
                .toList();

        return ResponseEntity.ok(new ApiResponse<>(true, "Return shipments retrieved successfully", responses, null));
    }
}
