package com.atlashub.commerce.catalog.presentation.rest;

import com.atlashub.commerce.catalog.application.commands.ApproveVendor.ApproveVendorCommand;
import com.atlashub.commerce.catalog.application.commands.ApproveVendor.ApproveVendorHandler;
import com.atlashub.commerce.catalog.application.commands.CreateVendor.CreateVendorCommand;
import com.atlashub.commerce.catalog.application.commands.CreateVendor.CreateVendorHandler;
import com.atlashub.commerce.catalog.application.commands.CreateVendor.CreateVendorResult;
import com.atlashub.commerce.catalog.application.commands.SuspendVendor.SuspendVendorCommand;
import com.atlashub.commerce.catalog.application.commands.SuspendVendor.SuspendVendorHandler;
import com.atlashub.commerce.catalog.application.queries.ListVendors.ListVendorsHandler;
import com.atlashub.commerce.catalog.application.queries.ListVendors.ListVendorsQuery;
import com.atlashub.commerce.catalog.application.queries.ListVendors.VendorResult;
import com.atlashub.commerce.catalog.domain.valueobject.VendorStatus;
import com.atlashub.commerce.catalog.presentation.dto.CreateVendorRequest;
import com.atlashub.commerce.catalog.presentation.dto.CreateVendorResponse;
import com.atlashub.commerce.catalog.presentation.dto.SuspendVendorRequest;
import com.atlashub.shared.application.dto.ApiResponse;
import com.atlashub.shared.application.security.AuthenticatedPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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

import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/api/v1/vendors")
@Tag(name = "Vendors", description = "Marketplace vendor management")
@SecurityRequirement(name = "bearerAuth")
public class VendorController {

    private final CreateVendorHandler createVendorHandler;
    private final ApproveVendorHandler approveVendorHandler;
    private final SuspendVendorHandler suspendVendorHandler;
    private final ListVendorsHandler listVendorsHandler;

    public VendorController(CreateVendorHandler createVendorHandler,
                            ApproveVendorHandler approveVendorHandler,
                            SuspendVendorHandler suspendVendorHandler,
                            ListVendorsHandler listVendorsHandler) {
        this.createVendorHandler = Objects.requireNonNull(createVendorHandler, "CreateVendorHandler must not be null");
        this.approveVendorHandler = Objects.requireNonNull(approveVendorHandler, "ApproveVendorHandler must not be null");
        this.suspendVendorHandler = Objects.requireNonNull(suspendVendorHandler, "SuspendVendorHandler must not be null");
        this.listVendorsHandler = Objects.requireNonNull(listVendorsHandler, "ListVendorsHandler must not be null");
    }

    @PostMapping
    @Operation(summary = "Create vendor")
    public ResponseEntity<ApiResponse<CreateVendorResponse>> createVendor(
            @AuthenticationPrincipal AuthenticatedPrincipal principal,
            @Valid @RequestBody CreateVendorRequest request) {
        CreateVendorCommand command = new CreateVendorCommand(
                principal.activeOrganizationId(),
                request.userId(),
                request.businessName(),
                request.email(),
                request.phone(),
                request.settlementBankCode(),
                request.settlementAccountNumber(),
                request.settlementAccountName(),
                request.commissionRate(),
                request.disbursementSchedule()
        );
        CreateVendorResult result = createVendorHandler.execute(command);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "Vendor created successfully", new CreateVendorResponse(result.vendorId()), null));
    }

    @PostMapping("/{id}/approve")
    @Operation(summary = "Approve vendor")
    public ResponseEntity<ApiResponse<Void>> approveVendor(@PathVariable Long id) {
        approveVendorHandler.execute(new ApproveVendorCommand(id));
        return ResponseEntity.ok(new ApiResponse<>(true, "Vendor approved successfully", null, null));
    }

    @PostMapping("/{id}/suspend")
    @Operation(summary = "Suspend vendor")
    public ResponseEntity<ApiResponse<Void>> suspendVendor(
            @PathVariable Long id,
            @Valid @RequestBody SuspendVendorRequest request) {
        suspendVendorHandler.execute(new SuspendVendorCommand(id, request.reason()));
        return ResponseEntity.ok(new ApiResponse<>(true, "Vendor suspended successfully", null, null));
    }

    @GetMapping
    @Operation(summary = "List vendors")
    public ResponseEntity<ApiResponse<List<VendorResult>>> listVendors(
            @AuthenticationPrincipal AuthenticatedPrincipal principal,
            @RequestParam(required = false) VendorStatus status) {
        ListVendorsQuery query = new ListVendorsQuery(
                principal.activeOrganizationId(),
                status
        );
        List<VendorResult> result = listVendorsHandler.execute(query);
        return ResponseEntity.ok(new ApiResponse<>(true, "Success", result, null));
    }
}
