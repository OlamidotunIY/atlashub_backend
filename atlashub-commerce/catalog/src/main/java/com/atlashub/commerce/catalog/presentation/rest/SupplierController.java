package com.atlashub.commerce.catalog.presentation.rest;

import com.atlashub.commerce.catalog.application.commands.CreateSupplier.CreateSupplierCommand;
import com.atlashub.commerce.catalog.application.commands.CreateSupplier.CreateSupplierHandler;
import com.atlashub.commerce.catalog.application.commands.CreateSupplier.CreateSupplierResult;
import com.atlashub.commerce.catalog.application.queries.ListSuppliers.ListSuppliersHandler;
import com.atlashub.commerce.catalog.application.queries.ListSuppliers.ListSuppliersQuery;
import com.atlashub.commerce.catalog.application.queries.ListSuppliers.SupplierResult;
import com.atlashub.commerce.catalog.domain.valueobject.SupplierStatus;
import com.atlashub.commerce.catalog.presentation.dto.CreateSupplierRequest;
import com.atlashub.commerce.catalog.presentation.dto.CreateSupplierResponse;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/api/v1/suppliers")
@Tag(name = "Suppliers", description = "Supplier management")
@SecurityRequirement(name = "bearerAuth")
public class SupplierController {

    private final CreateSupplierHandler createSupplierHandler;
    private final ListSuppliersHandler listSuppliersHandler;

    public SupplierController(CreateSupplierHandler createSupplierHandler,
                              ListSuppliersHandler listSuppliersHandler) {
        this.createSupplierHandler = Objects.requireNonNull(createSupplierHandler, "CreateSupplierHandler must not be null");
        this.listSuppliersHandler = Objects.requireNonNull(listSuppliersHandler, "ListSuppliersHandler must not be null");
    }

    @PostMapping
    @Operation(summary = "Create supplier")
    public ResponseEntity<ApiResponse<CreateSupplierResponse>> createSupplier(
            @AuthenticationPrincipal AuthenticatedPrincipal principal,
            @Valid @RequestBody CreateSupplierRequest request) {
        CreateSupplierCommand command = new CreateSupplierCommand(
                principal.activeOrganizationId(),
                request.name(),
                request.email(),
                request.phone(),
                request.address()
        );
        CreateSupplierResult result = createSupplierHandler.execute(command);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "Supplier created successfully", new CreateSupplierResponse(result.supplierId()), null));
    }

    @GetMapping
    @Operation(summary = "List suppliers")
    public ResponseEntity<ApiResponse<List<SupplierResult>>> listSuppliers(
            @AuthenticationPrincipal AuthenticatedPrincipal principal,
            @RequestParam(required = false) SupplierStatus status) {
        ListSuppliersQuery query = new ListSuppliersQuery(
                principal.activeOrganizationId(),
                status
        );
        List<SupplierResult> result = listSuppliersHandler.execute(query);
        return ResponseEntity.ok(new ApiResponse<>(true, "Success", result, null));
    }
}
