package com.atlashub.logistics.shipping.presentation.rest;

import com.atlashub.logistics.shipping.application.commands.AdvanceShipmentMilestone.AdvanceShipmentMilestoneCommand;
import com.atlashub.logistics.shipping.application.commands.AdvanceShipmentMilestone.AdvanceShipmentMilestoneHandler;
import com.atlashub.logistics.shipping.application.commands.AssignRider.AssignRiderCommand;
import com.atlashub.logistics.shipping.application.commands.AssignRider.AssignRiderHandler;
import com.atlashub.logistics.shipping.application.commands.CompleteDelivery.CompleteDeliveryCommand;
import com.atlashub.logistics.shipping.application.commands.CompleteDelivery.CompleteDeliveryHandler;
import com.atlashub.logistics.shipping.application.commands.CreateShipment.CreateShipmentCommand;
import com.atlashub.logistics.shipping.application.commands.CreateShipment.CreateShipmentHandler;
import com.atlashub.logistics.shipping.application.commands.CreateShipment.CreateShipmentResult;
import com.atlashub.logistics.shipping.application.commands.RecordFailedDeliveryAttempt.RecordFailedDeliveryAttemptCommand;
import com.atlashub.logistics.shipping.application.commands.RecordFailedDeliveryAttempt.RecordFailedDeliveryAttemptHandler;
import com.atlashub.logistics.shipping.application.commands.Route3PL.Route3PLCommand;
import com.atlashub.logistics.shipping.application.commands.Route3PL.Route3PLHandler;
import com.atlashub.logistics.shipping.application.commands.UpdateShipmentLocation.UpdateShipmentLocationCommand;
import com.atlashub.logistics.shipping.application.commands.UpdateShipmentLocation.UpdateShipmentLocationHandler;
import com.atlashub.logistics.shipping.application.queries.CalculateShippingQuotes.CalculateShippingQuotesHandler;
import com.atlashub.logistics.shipping.application.queries.CalculateShippingQuotes.CalculateShippingQuotesQuery;
import com.atlashub.logistics.shipping.application.queries.CalculateShippingQuotes.ShippingQuoteResult;
import com.atlashub.logistics.shipping.application.queries.GetShipmentDeliveryCode.DeliveryCodeResult;
import com.atlashub.logistics.shipping.application.queries.GetShipmentDeliveryCode.GetShipmentDeliveryCodeHandler;
import com.atlashub.logistics.shipping.application.queries.GetShipmentDeliveryCode.GetShipmentDeliveryCodeQuery;
import com.atlashub.logistics.shipping.application.queries.GetShipmentDetails.GetShipmentDetailsHandler;
import com.atlashub.logistics.shipping.application.queries.GetShipmentDetails.GetShipmentDetailsQuery;
import com.atlashub.logistics.shipping.application.queries.GetShipmentDetails.ShipmentDetailsResult;
import com.atlashub.logistics.shipping.application.queries.ListShipments.ListShipmentsHandler;
import com.atlashub.logistics.shipping.application.queries.ListShipments.ListShipmentsQuery;
import com.atlashub.logistics.shipping.application.queries.ListShipments.ShipmentSummaryResult;
import com.atlashub.logistics.shipping.application.queries.TrackShipment.ShipmentTrackingResult;
import com.atlashub.logistics.shipping.application.queries.TrackShipment.TrackShipmentHandler;
import com.atlashub.logistics.shipping.application.queries.TrackShipment.TrackShipmentQuery;
import com.atlashub.logistics.shipping.domain.valueobject.DeliveryTier;
import com.atlashub.logistics.shipping.domain.valueobject.Dimensions;
import com.atlashub.logistics.shipping.domain.valueobject.ShipmentType;
import com.atlashub.logistics.shipping.presentation.dto.AdvanceMilestoneRequest;
import com.atlashub.logistics.shipping.presentation.dto.AssignRiderRequest;
import com.atlashub.logistics.shipping.presentation.dto.CompleteDeliveryRequest;
import com.atlashub.logistics.shipping.presentation.dto.CreateShipmentRequest;
import com.atlashub.logistics.shipping.presentation.dto.CreateShipmentResponse;
import com.atlashub.logistics.shipping.presentation.dto.RecordFailedAttemptRequest;
import com.atlashub.logistics.shipping.presentation.dto.Route3PLRequest;
import com.atlashub.logistics.shipping.presentation.dto.ShippingQuotesRequest;
import com.atlashub.logistics.shipping.presentation.dto.UpdateLiveLocationRequest;
import com.atlashub.shared.application.annotation.PublicEndpoint;
import com.atlashub.shared.application.dto.ApiResponse;
import com.atlashub.shared.application.security.AuthenticatedPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
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
@RequestMapping("/api/v1/shipments")
@Tag(name = "Shipments")
public class ShipmentController {

    private final CreateShipmentHandler createShipmentHandler;
    private final CalculateShippingQuotesHandler calculateShippingQuotesHandler;
    private final GetShipmentDeliveryCodeHandler getShipmentDeliveryCodeHandler;
    private final TrackShipmentHandler trackShipmentHandler;
    private final GetShipmentDetailsHandler getShipmentDetailsHandler;
    private final ListShipmentsHandler listShipmentsHandler;
    private final AssignRiderHandler assignRiderHandler;
    private final Route3PLHandler route3PLHandler;
    private final AdvanceShipmentMilestoneHandler advanceMilestoneHandler;
    private final UpdateShipmentLocationHandler updateLocationHandler;
    private final CompleteDeliveryHandler completeDeliveryHandler;
    private final RecordFailedDeliveryAttemptHandler recordFailedAttemptHandler;

    public ShipmentController(
            CreateShipmentHandler createShipmentHandler,
            CalculateShippingQuotesHandler calculateShippingQuotesHandler,
            GetShipmentDeliveryCodeHandler getShipmentDeliveryCodeHandler,
            TrackShipmentHandler trackShipmentHandler,
            GetShipmentDetailsHandler getShipmentDetailsHandler,
            ListShipmentsHandler listShipmentsHandler,
            AssignRiderHandler assignRiderHandler,
            Route3PLHandler route3PLHandler,
            AdvanceShipmentMilestoneHandler advanceMilestoneHandler,
            UpdateShipmentLocationHandler updateLocationHandler,
            CompleteDeliveryHandler completeDeliveryHandler,
            RecordFailedDeliveryAttemptHandler recordFailedAttemptHandler
    ) {
        this.createShipmentHandler = createShipmentHandler;
        this.calculateShippingQuotesHandler = calculateShippingQuotesHandler;
        this.getShipmentDeliveryCodeHandler = getShipmentDeliveryCodeHandler;
        this.trackShipmentHandler = trackShipmentHandler;
        this.getShipmentDetailsHandler = getShipmentDetailsHandler;
        this.listShipmentsHandler = listShipmentsHandler;
        this.assignRiderHandler = assignRiderHandler;
        this.route3PLHandler = route3PLHandler;
        this.advanceMilestoneHandler = advanceMilestoneHandler;
        this.updateLocationHandler = updateLocationHandler;
        this.completeDeliveryHandler = completeDeliveryHandler;
        this.recordFailedAttemptHandler = recordFailedAttemptHandler;
    }

    @PostMapping("/quotes")
    @Operation(summary = "Calculate shipping quotes and delivery windows at checkout", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<ShippingQuoteResult>> getShippingQuotes(
            @AuthenticationPrincipal AuthenticatedPrincipal principal,
            @Valid @RequestBody ShippingQuotesRequest request
    ) {
        CalculateShippingQuotesQuery query = new CalculateShippingQuotesQuery(
                principal.activeOrganizationId(),
                request.originAddress(),
                request.destinationAddress(),
                request.weightKg(),
                request.declaredValue()
        );
        ShippingQuoteResult result = calculateShippingQuotesHandler.execute(query);
        return ResponseEntity.ok(new ApiResponse<>(true, "Shipping quote calculated successfully", result, null));
    }

    @PostMapping
    @Operation(summary = "Create shipment", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<CreateShipmentResponse>> createShipment(
            @AuthenticationPrincipal AuthenticatedPrincipal principal,
            @Valid @RequestBody CreateShipmentRequest request
    ) {
        CreateShipmentCommand command = new CreateShipmentCommand(
                principal.activeOrganizationId(),
                request.orderId(),
                request.type() != null ? request.type() : ShipmentType.CUSTOMER_ORDER,
                request.serviceTier() != null ? request.serviceTier() : DeliveryTier.STANDARD,
                request.originAddress(),
                request.destinationAddress(),
                request.senderId() != null ? request.senderId() : principal.activeOrganizationId(),
                request.receiverId(),
                request.weightKg(),
                request.dimensions() != null ? request.dimensions() : new Dimensions(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO),
                request.declaredValue(),
                UUID.randomUUID().toString()
        );

        CreateShipmentResult result = createShipmentHandler.execute(command);
        CreateShipmentResponse response = new CreateShipmentResponse(
                result.shipmentId(),
                result.trackingNumber(),
                result.deliveryCode(),
                result.routeType(),
                result.serviceTier(),
                result.customerShippingFee(),
                result.deliveryWindowStart(),
                result.deliveryWindowEnd()
        );

        return ResponseEntity.ok(new ApiResponse<>(true, "Shipment created successfully", response, null));
    }

    @GetMapping("/delivery-code")
    @Operation(summary = "Retrieve storefront delivery code for customer display", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<DeliveryCodeResult>> getDeliveryCode(
            @AuthenticationPrincipal AuthenticatedPrincipal principal,
            @RequestParam("shipmentId") Long shipmentId
    ) {
        GetShipmentDeliveryCodeQuery query = new GetShipmentDeliveryCodeQuery(shipmentId, principal.activeOrganizationId());
        DeliveryCodeResult result = getShipmentDeliveryCodeHandler.execute(query);
        return ResponseEntity.ok(new ApiResponse<>(true, "Delivery code retrieved successfully", result, null));
    }

    @GetMapping("/track")
    @PublicEndpoint
    @Operation(summary = "Track shipment status, milestones, and live location")
    public ResponseEntity<ApiResponse<ShipmentTrackingResult>> trackShipment(
            @RequestParam("trackingNumber") String trackingNumber
    ) {
        TrackShipmentQuery query = new TrackShipmentQuery(trackingNumber);
        ShipmentTrackingResult result = trackShipmentHandler.execute(query);
        return ResponseEntity.ok(new ApiResponse<>(true, "Tracking details retrieved successfully", result, null));
    }

    @GetMapping("/details")
    @Operation(summary = "Get shipment details", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<ShipmentDetailsResult>> getShipmentDetails(
            @AuthenticationPrincipal AuthenticatedPrincipal principal,
            @RequestParam("id") Long id
    ) {
        GetShipmentDetailsQuery query = new GetShipmentDetailsQuery(id, principal.activeOrganizationId());
        ShipmentDetailsResult result = getShipmentDetailsHandler.execute(query);
        return ResponseEntity.ok(new ApiResponse<>(true, "Shipment details retrieved successfully", result, null));
    }

    @GetMapping
    @Operation(summary = "List shipments", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<List<ShipmentSummaryResult>>> listShipments(
            @AuthenticationPrincipal AuthenticatedPrincipal principal,
            @RequestParam(value = "status", required = false) String status
    ) {
        ListShipmentsQuery query = new ListShipmentsQuery(principal.activeOrganizationId(), status);
        List<ShipmentSummaryResult> results = listShipmentsHandler.execute(query);
        return ResponseEntity.ok(new ApiResponse<>(true, "Shipments retrieved successfully", results, null));
    }

    @PostMapping("/{id}/assign-rider")
    @Operation(summary = "Assign internal rider to shipment", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<Void>> assignRider(
            @PathVariable("id") Long id,
            @Valid @RequestBody AssignRiderRequest request
    ) {
        AssignRiderCommand command = new AssignRiderCommand(id, request.riderId(), request.vehicleId(), UUID.randomUUID().toString());
        assignRiderHandler.execute(command);
        return ResponseEntity.ok(new ApiResponse<>(true, "Rider assigned successfully", null, null));
    }

    @PostMapping("/{id}/route-3pl")
    @Operation(summary = "Route shipment via 3PL partner carrier", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<Void>> route3PL(
            @PathVariable("id") Long id,
            @Valid @RequestBody Route3PLRequest request
    ) {
        Route3PLCommand command = new Route3PLCommand(id, request.carrierCode(), UUID.randomUUID().toString());
        route3PLHandler.execute(command);
        return ResponseEntity.ok(new ApiResponse<>(true, "Shipment routed to 3PL successfully", null, null));
    }

    @PostMapping("/{id}/milestones")
    @Operation(summary = "Advance route-aware milestone", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<Void>> advanceMilestone(
            @PathVariable("id") Long id,
            @Valid @RequestBody AdvanceMilestoneRequest request
    ) {
        AdvanceShipmentMilestoneCommand command = new AdvanceShipmentMilestoneCommand(
                id,
                request.milestone(),
                request.locationName(),
                request.state(),
                request.description(),
                request.latitude(),
                request.longitude(),
                UUID.randomUUID().toString()
        );
        advanceMilestoneHandler.execute(command);
        return ResponseEntity.ok(new ApiResponse<>(true, "Milestone advanced successfully", null, null));
    }

    @PostMapping("/{id}/location")
    @Operation(summary = "Update live GPS location", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<Void>> updateLocation(
            @PathVariable("id") Long id,
            @Valid @RequestBody UpdateLiveLocationRequest request
    ) {
        UpdateShipmentLocationCommand command = new UpdateShipmentLocationCommand(
                id,
                request.locationName(),
                request.latitude(),
                request.longitude(),
                UUID.randomUUID().toString()
        );
        updateLocationHandler.execute(command);
        return ResponseEntity.ok(new ApiResponse<>(true, "Location updated successfully", null, null));
    }

    @PostMapping("/{id}/complete-delivery")
    @Operation(summary = "Complete delivery verifying delivery code and proof of delivery", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<Void>> completeDelivery(
            @PathVariable("id") Long id,
            @Valid @RequestBody CompleteDeliveryRequest request
    ) {
        CompleteDeliveryCommand command = new CompleteDeliveryCommand(
                id,
                request.deliveryCode(),
                request.proofOfDeliveryUrl(),
                UUID.randomUUID().toString()
        );
        completeDeliveryHandler.execute(command);
        return ResponseEntity.ok(new ApiResponse<>(true, "Delivery confirmed successfully", null, null));
    }

    @PostMapping("/{id}/failed-attempt")
    @Operation(summary = "Record failed delivery attempt or final failure under 3-part policy", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<Void>> recordFailedAttempt(
            @PathVariable("id") Long id,
            @Valid @RequestBody RecordFailedAttemptRequest request
    ) {
        RecordFailedDeliveryAttemptCommand command = new RecordFailedDeliveryAttemptCommand(
                id,
                request.markFailedFinal(),
                request.faultType(),
                request.reason(),
                UUID.randomUUID().toString()
        );
        recordFailedAttemptHandler.execute(command);
        return ResponseEntity.ok(new ApiResponse<>(true, "Delivery attempt recorded successfully", null, null));
    }
}
