package com.atlashub.logistics.shipping.presentation.rest;

import com.atlashub.logistics.shipping.application.commands.ConfigureCoverageZone.ConfigureCoverageZoneCommand;
import com.atlashub.logistics.shipping.application.commands.ConfigureCoverageZone.ConfigureCoverageZoneHandler;
import com.atlashub.logistics.shipping.application.commands.ConfigureCoverageZone.ConfigureCoverageZoneResult;
import com.atlashub.logistics.shipping.application.commands.RegisterRider.RegisterRiderCommand;
import com.atlashub.logistics.shipping.application.commands.RegisterRider.RegisterRiderHandler;
import com.atlashub.logistics.shipping.application.commands.RegisterVehicle.RegisterVehicleCommand;
import com.atlashub.logistics.shipping.application.commands.RegisterVehicle.RegisterVehicleHandler;
import com.atlashub.logistics.shipping.application.commands.SetRiderOffline.SetRiderOfflineCommand;
import com.atlashub.logistics.shipping.application.commands.SetRiderOffline.SetRiderOfflineHandler;
import com.atlashub.logistics.shipping.application.commands.SetRiderOnline.SetRiderOnlineCommand;
import com.atlashub.logistics.shipping.application.commands.SetRiderOnline.SetRiderOnlineHandler;
import com.atlashub.logistics.shipping.application.commands.UpdateMerchantShippingConfig.UpdateMerchantShippingConfigCommand;
import com.atlashub.logistics.shipping.application.commands.UpdateMerchantShippingConfig.UpdateMerchantShippingConfigHandler;
import com.atlashub.logistics.shipping.application.queries.ListCoverageZones.CoverageZoneResult;
import com.atlashub.logistics.shipping.application.queries.ListCoverageZones.ListCoverageZonesHandler;
import com.atlashub.logistics.shipping.application.queries.ListCoverageZones.ListCoverageZonesQuery;
import com.atlashub.logistics.shipping.application.queries.ListRiders.ListRidersHandler;
import com.atlashub.logistics.shipping.application.queries.ListRiders.ListRidersQuery;
import com.atlashub.logistics.shipping.application.queries.ListRiders.RiderResult;
import com.atlashub.logistics.shipping.application.queries.ListVehicles.ListVehiclesHandler;
import com.atlashub.logistics.shipping.application.queries.ListVehicles.ListVehiclesQuery;
import com.atlashub.logistics.shipping.application.queries.ListVehicles.VehicleResult;
import com.atlashub.logistics.shipping.presentation.dto.ConfigureCoverageZoneRequest;
import com.atlashub.logistics.shipping.presentation.dto.RegisterRiderRequest;
import com.atlashub.logistics.shipping.presentation.dto.RegisterVehicleRequest;
import com.atlashub.logistics.shipping.presentation.dto.UpdateMerchantShippingConfigRequest;
import com.atlashub.shared.application.dto.ApiResponse;
import com.atlashub.shared.application.security.AuthenticatedPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/fleet")
@Tag(name = "Fleet & Logistics Coverage")
public class FleetController {

    private final ConfigureCoverageZoneHandler configureCoverageZoneHandler;
    private final ListCoverageZonesHandler listCoverageZonesHandler;
    private final UpdateMerchantShippingConfigHandler updateMerchantShippingConfigHandler;
    private final RegisterVehicleHandler registerVehicleHandler;
    private final ListVehiclesHandler listVehiclesHandler;
    private final RegisterRiderHandler registerRiderHandler;
    private final ListRidersHandler listRidersHandler;
    private final SetRiderOnlineHandler setRiderOnlineHandler;
    private final SetRiderOfflineHandler setRiderOfflineHandler;

    public FleetController(
            ConfigureCoverageZoneHandler configureCoverageZoneHandler,
            ListCoverageZonesHandler listCoverageZonesHandler,
            UpdateMerchantShippingConfigHandler updateMerchantShippingConfigHandler,
            RegisterVehicleHandler registerVehicleHandler,
            ListVehiclesHandler listVehiclesHandler,
            RegisterRiderHandler registerRiderHandler,
            ListRidersHandler listRidersHandler,
            SetRiderOnlineHandler setRiderOnlineHandler,
            SetRiderOfflineHandler setRiderOfflineHandler
    ) {
        this.configureCoverageZoneHandler = configureCoverageZoneHandler;
        this.listCoverageZonesHandler = listCoverageZonesHandler;
        this.updateMerchantShippingConfigHandler = updateMerchantShippingConfigHandler;
        this.registerVehicleHandler = registerVehicleHandler;
        this.listVehiclesHandler = listVehiclesHandler;
        this.registerRiderHandler = registerRiderHandler;
        this.listRidersHandler = listRidersHandler;
        this.setRiderOnlineHandler = setRiderOnlineHandler;
        this.setRiderOfflineHandler = setRiderOfflineHandler;
    }

    @PostMapping("/zones")
    @Operation(summary = "Configure or register coverage zone", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<ConfigureCoverageZoneResult>> configureZone(
            @Valid @RequestBody ConfigureCoverageZoneRequest request
    ) {
        ConfigureCoverageZoneCommand command = new ConfigureCoverageZoneCommand(
                request.state(),
                request.city(),
                request.zoneCode(),
                request.internalFleetCovered(),
                request.partner3PLCovered(),
                request.preferred3PLCarrierCode(),
                request.urgentDeliverySupported(),
                request.baseStandardFee(),
                request.perKgStandardFee(),
                request.urgentMultiplier() != null ? request.urgentMultiplier() : BigDecimal.valueOf(2.0),
                request.intraStateStandardMinHours(),
                request.intraStateStandardMaxHours(),
                request.interStateStandardMinHours(),
                request.interStateStandardMaxHours(),
                request.urgentMinHours(),
                request.urgentMaxHours()
        );
        ConfigureCoverageZoneResult result = configureCoverageZoneHandler.execute(command);
        return ResponseEntity.ok(new ApiResponse<>(true, "Coverage zone configured successfully", result, null));
    }

    @GetMapping("/zones")
    @Operation(summary = "List all coverage zones", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<List<CoverageZoneResult>>> listZones() {
        List<CoverageZoneResult> zones = listCoverageZonesHandler.execute(new ListCoverageZonesQuery());
        return ResponseEntity.ok(new ApiResponse<>(true, "Coverage zones retrieved successfully", zones, null));
    }

    @PostMapping("/merchant-config")
    @Operation(summary = "Update merchant shipping configuration", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<Void>> updateMerchantConfig(
            @AuthenticationPrincipal AuthenticatedPrincipal principal,
            @Valid @RequestBody UpdateMerchantShippingConfigRequest request
    ) {
        UpdateMerchantShippingConfigCommand command = new UpdateMerchantShippingConfigCommand(
                principal.activeOrganizationId(),
                request.urgentDeliveryEnabled(),
                request.defaultPickupAddress()
        );
        updateMerchantShippingConfigHandler.execute(command);
        return ResponseEntity.ok(new ApiResponse<>(true, "Merchant shipping configuration updated successfully", null, null));
    }

    @PostMapping("/vehicles")
    @Operation(summary = "Register new fleet vehicle", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<Long>> registerVehicle(
            @AuthenticationPrincipal AuthenticatedPrincipal principal,
            @Valid @RequestBody RegisterVehicleRequest request
    ) {
        RegisterVehicleCommand command = new RegisterVehicleCommand(
                principal.activeOrganizationId(),
                request.zoneCode(),
                request.licensePlate(),
                request.vehicleType(),
                request.maxWeightKg()
        );
        Long vehicleId = registerVehicleHandler.execute(command);
        return ResponseEntity.ok(new ApiResponse<>(true, "Vehicle registered successfully", vehicleId, null));
    }

    @GetMapping("/vehicles")
    @Operation(summary = "List all fleet vehicles", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<List<VehicleResult>>> listVehicles(
            @AuthenticationPrincipal AuthenticatedPrincipal principal
    ) {
        List<VehicleResult> vehicles = listVehiclesHandler.execute(new ListVehiclesQuery(principal.activeOrganizationId()));
        return ResponseEntity.ok(new ApiResponse<>(true, "Fleet vehicles retrieved successfully", vehicles, null));
    }

    @PostMapping("/riders")
    @Operation(summary = "Register new dispatch rider", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<Long>> registerRider(
            @AuthenticationPrincipal AuthenticatedPrincipal principal,
            @Valid @RequestBody RegisterRiderRequest request
    ) {
        RegisterRiderCommand command = new RegisterRiderCommand(
                principal.activeOrganizationId(),
                request.userId(),
                request.zoneCode(),
                request.vehicleId(),
                request.licenseNumber()
        );
        Long riderId = registerRiderHandler.execute(command);
        return ResponseEntity.ok(new ApiResponse<>(true, "Dispatch rider registered successfully", riderId, null));
    }

    @GetMapping("/riders")
    @Operation(summary = "List all dispatch riders", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<List<RiderResult>>> listRiders(
            @AuthenticationPrincipal AuthenticatedPrincipal principal
    ) {
        List<RiderResult> riders = listRidersHandler.execute(new ListRidersQuery(principal.activeOrganizationId()));
        return ResponseEntity.ok(new ApiResponse<>(true, "Dispatch riders retrieved successfully", riders, null));
    }

    @PostMapping("/riders/{id}/online")
    @Operation(summary = "Set rider online / available", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<Void>> setRiderOnline(@PathVariable("id") Long id) {
        setRiderOnlineHandler.execute(new SetRiderOnlineCommand(id));
        return ResponseEntity.ok(new ApiResponse<>(true, "Rider is now online", null, null));
    }

    @PostMapping("/riders/{id}/offline")
    @Operation(summary = "Set rider offline", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<Void>> setRiderOffline(@PathVariable("id") Long id) {
        setRiderOfflineHandler.execute(new SetRiderOfflineCommand(id));
        return ResponseEntity.ok(new ApiResponse<>(true, "Rider is now offline", null, null));
    }
}
