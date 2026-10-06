package com.atlashub.accounts.presentation.rest;

import com.atlashub.accounts.application.command.CloseOutlet.CloseOutletCommand;
import com.atlashub.accounts.application.command.CloseOutlet.CloseOutletHandler;
import com.atlashub.accounts.application.command.CreateOutlet.CreateOutletCommand;
import com.atlashub.accounts.application.command.CreateOutlet.CreateOutletHandler;
import com.atlashub.accounts.application.command.SuspendOutlet.SuspendOutletCommand;
import com.atlashub.accounts.application.command.SuspendOutlet.SuspendOutletHandler;
import com.atlashub.accounts.application.command.UpdateOutlet.UpdateOutletCommand;
import com.atlashub.accounts.application.command.UpdateOutlet.UpdateOutletHandler;
import com.atlashub.accounts.application.query.GetOutlet.GetOutletHandler;
import com.atlashub.accounts.application.query.GetOutlet.GetOutletQuery;
import com.atlashub.accounts.application.query.GetOutlet.OutletResult;
import com.atlashub.accounts.application.query.ListOutlets.ListOutletsHandler;
import com.atlashub.accounts.application.query.ListOutlets.ListOutletsQuery;
import com.atlashub.accounts.presentation.dto.CreateOutletRequest;
import com.atlashub.accounts.presentation.dto.OutletResponse;
import com.atlashub.accounts.presentation.dto.UpdateOutletRequest;
import com.atlashub.shared.application.dto.ApiResponse;
import com.atlashub.shared.application.security.AuthenticatedPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/outlets")
@Tag(name = "Outlets", description = "Outlet / branch management")
@Validated
public class OutletController {

    private final CreateOutletHandler createOutletHandler;
    private final UpdateOutletHandler updateOutletHandler;
    private final SuspendOutletHandler suspendOutletHandler;
    private final CloseOutletHandler closeOutletHandler;
    private final GetOutletHandler getOutletHandler;
    private final ListOutletsHandler listOutletsHandler;

    public OutletController(CreateOutletHandler createOutletHandler,
                            UpdateOutletHandler updateOutletHandler,
                            SuspendOutletHandler suspendOutletHandler,
                            CloseOutletHandler closeOutletHandler,
                            GetOutletHandler getOutletHandler,
                            ListOutletsHandler listOutletsHandler) {
        this.createOutletHandler = createOutletHandler;
        this.updateOutletHandler = updateOutletHandler;
        this.suspendOutletHandler = suspendOutletHandler;
        this.closeOutletHandler = closeOutletHandler;
        this.getOutletHandler = getOutletHandler;
        this.listOutletsHandler = listOutletsHandler;
    }

    @PostMapping
    @Operation(summary = "Create a new outlet / branch", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<Long>> createOutlet(
            @AuthenticationPrincipal AuthenticatedPrincipal principal,
            @Valid @RequestBody CreateOutletRequest request) {
        Long outletId = createOutletHandler.execute(new CreateOutletCommand(
                principal.activeOrganizationId(),
                request.name(),
                request.address(),
                request.city(),
                request.state(),
                request.managerId()
        ));
        return ok(outletId);
    }

    @GetMapping("/{outletId}")
    @Operation(summary = "Get a single outlet by ID", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<OutletResponse>> getOutlet(
            @AuthenticationPrincipal AuthenticatedPrincipal principal,
            @PathVariable @Positive Long outletId) {
        OutletResult result = getOutletHandler.execute(new GetOutletQuery(principal.activeOrganizationId(), outletId));
        return ok(toOutletResponse(result));
    }

    @GetMapping
    @Operation(summary = "List all outlets for an organization", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<List<OutletResponse>>> listOutlets(
            @AuthenticationPrincipal AuthenticatedPrincipal principal) {
        List<OutletResponse> results = listOutletsHandler.execute(
                        new ListOutletsQuery(principal.activeOrganizationId()))
                .stream().map(this::toOutletResponse).toList();
        return ok(results);
    }

    @PutMapping("/{outletId}")
    @Operation(summary = "Update outlet details", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<Void>> updateOutlet(
            @AuthenticationPrincipal AuthenticatedPrincipal principal,
            @PathVariable @Positive Long outletId,
            @Valid @RequestBody UpdateOutletRequest request) {
        updateOutletHandler.execute(new UpdateOutletCommand(
                principal.activeOrganizationId(), outletId, request.name(), request.address(),
                request.city(), request.state(), request.managerId()
        ));
        return done("Outlet updated");
    }

    @PostMapping("/{outletId}/suspend")
    @Operation(summary = "Suspend an outlet", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<Void>> suspendOutlet(
            @AuthenticationPrincipal AuthenticatedPrincipal principal,
            @PathVariable @Positive Long outletId) {
        suspendOutletHandler.execute(new SuspendOutletCommand(principal.activeOrganizationId(), outletId));
        return done("Outlet suspended");
    }

    @PostMapping("/{outletId}/close")
    @Operation(summary = "Permanently close an outlet", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<Void>> closeOutlet(
            @AuthenticationPrincipal AuthenticatedPrincipal principal,
            @PathVariable @Positive Long outletId) {
        closeOutletHandler.execute(new CloseOutletCommand(principal.activeOrganizationId(), outletId));
        return done("Outlet closed");
    }

    private <T> ResponseEntity<ApiResponse<T>> ok(T value) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Success", value, null));
    }

    private ResponseEntity<ApiResponse<Void>> done(String message) {
        return ResponseEntity.ok(new ApiResponse<>(true, message, null, null));
    }

    private OutletResponse toOutletResponse(OutletResult result) {
        return new OutletResponse(
                result.id(),
                result.organizationId(),
                result.name(),
                result.address(),
                result.city(),
                result.state(),
                result.country(),
                result.currency(),
                result.managerId(),
                result.status().name(),
                result.createdAt(),
                result.updatedAt()
        );
    }
}
