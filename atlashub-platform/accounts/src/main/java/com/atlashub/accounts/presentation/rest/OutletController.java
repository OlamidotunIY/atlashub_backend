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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
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

    @PostMapping("/outlets")
    @Operation(summary = "Create a new outlet / branch", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<Long>> createOutlet(@Valid @RequestBody CreateOutletRequest request) {
        Long outletId = createOutletHandler.execute(new CreateOutletCommand(
                request.organizationId(),
                request.name(),
                request.address(),
                request.city(),
                request.state(),
                request.country(),
                request.managerId()
        ));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "Outlet created", outletId, null));
    }

    @GetMapping("/outlets")
    @Operation(summary = "Get a single outlet by ID", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<OutletResponse>> getOutlet(@RequestParam @Positive Long id) {
        OutletResult result = getOutletHandler.execute(new GetOutletQuery(id));
        return ResponseEntity.ok(new ApiResponse<>(true, "Success", toOutletResponse(result), null));
    }

    @GetMapping("/organizations/{orgId}/outlets")
    @Operation(summary = "List all outlets for an organization", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<List<OutletResponse>>> listOutlets(@PathVariable Long orgId) {
        List<OutletResponse> results = listOutletsHandler.execute(new ListOutletsQuery(orgId))
                .stream().map(this::toOutletResponse).toList();
        return ResponseEntity.ok(new ApiResponse<>(true, "Success", results, null));
    }

    @PutMapping("/outlets")
    @Operation(summary = "Update outlet details", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<Void>> updateOutlet(
            @RequestParam @Positive Long id,
            @Valid @RequestBody UpdateOutletRequest request) {
        updateOutletHandler.execute(new UpdateOutletCommand(
                id, request.name(), request.address(),
                request.city(), request.state(), request.managerId()
        ));
        return ResponseEntity.ok(new ApiResponse<>(true, "Outlet updated", null, null));
    }

    @PostMapping("/outlets/suspend")
    @Operation(summary = "Suspend an outlet", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<Void>> suspendOutlet(@RequestParam @Positive Long id) {
        suspendOutletHandler.execute(new SuspendOutletCommand(id));
        return ResponseEntity.ok(new ApiResponse<>(true, "Outlet suspended", null, null));
    }

    @PostMapping("/outlets/close")
    @Operation(summary = "Permanently close an outlet", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<Void>> closeOutlet(@RequestParam @Positive Long id) {
        closeOutletHandler.execute(new CloseOutletCommand(id));
        return ResponseEntity.ok(new ApiResponse<>(true, "Outlet closed", null, null));
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
