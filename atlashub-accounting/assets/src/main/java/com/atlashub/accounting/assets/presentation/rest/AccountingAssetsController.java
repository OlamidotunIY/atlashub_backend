package com.atlashub.accounting.assets.presentation.rest;

import com.atlashub.accounting.assets.application.command.DisposeAsset.DisposeAssetCommand;
import com.atlashub.accounting.assets.application.command.DisposeAsset.DisposeAssetHandler;
import com.atlashub.accounting.assets.application.command.RegisterAsset.RegisterAssetCommand;
import com.atlashub.accounting.assets.application.command.RegisterAsset.RegisterAssetHandler;
import com.atlashub.accounting.assets.application.command.RunDepreciation.RunDepreciationCommand;
import com.atlashub.accounting.assets.application.command.RunDepreciation.RunDepreciationHandler;
import com.atlashub.accounting.assets.application.query.GetAssetDetails.GetAssetDetailsHandler;
import com.atlashub.accounting.assets.application.query.GetAssetDetails.GetAssetDetailsQuery;
import com.atlashub.accounting.assets.application.query.ListAssets.AssetResult;
import com.atlashub.accounting.assets.application.query.ListAssets.ListAssetsHandler;
import com.atlashub.accounting.assets.application.query.ListAssets.ListAssetsQuery;
import com.atlashub.accounting.assets.domain.valueobject.AssetStatus;
import com.atlashub.accounting.assets.presentation.dto.AssetResponse;
import com.atlashub.accounting.assets.presentation.dto.DisposeAssetRequest;
import com.atlashub.accounting.assets.presentation.dto.RegisterAssetRequest;
import com.atlashub.shared.application.dto.ApiResponse;
import com.atlashub.shared.application.security.AuthenticatedPrincipal;
import com.atlashub.shared.domain.valueobject.CurrencyCode;
import com.atlashub.shared.domain.valueobject.Money;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Accounting Fixed Assets", description = "Endpoints for managing fixed assets and depreciation")
public class AccountingAssetsController {

    private final RegisterAssetHandler registerAssetHandler;
    private final RunDepreciationHandler runDepreciationHandler;
    private final DisposeAssetHandler disposeAssetHandler;
    private final ListAssetsHandler listAssetsHandler;
    private final GetAssetDetailsHandler getAssetDetailsHandler;

    public AccountingAssetsController(
            RegisterAssetHandler registerAssetHandler,
            RunDepreciationHandler runDepreciationHandler,
            DisposeAssetHandler disposeAssetHandler,
            ListAssetsHandler listAssetsHandler,
            GetAssetDetailsHandler getAssetDetailsHandler
    ) {
        this.registerAssetHandler = registerAssetHandler;
        this.runDepreciationHandler = runDepreciationHandler;
        this.disposeAssetHandler = disposeAssetHandler;
        this.listAssetsHandler = listAssetsHandler;
        this.getAssetDetailsHandler = getAssetDetailsHandler;
    }

    @PostMapping("/assets")
    @PreAuthorize("hasAuthority('accounting:assets:manage')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Register a new fixed asset")
    public ResponseEntity<ApiResponse<Long>> registerAsset(
            @Valid @RequestBody RegisterAssetRequest request,
            @AuthenticationPrincipal AuthenticatedPrincipal principal
    ) {
        CurrencyCode currencyCode = CurrencyCode.valueOf(request.currency().toUpperCase());
        RegisterAssetCommand command = new RegisterAssetCommand(
                principal.activeOrganizationId(),
                request.name(),
                request.tagNumber(),
                request.category(),
                request.purchaseDate(),
                Money.of(request.purchaseValue(), currencyCode),
                Money.of(request.salvageValue(), currencyCode),
                request.usefulLifeMonths(),
                request.depreciationMethod()
        );

        Long assetId = registerAssetHandler.execute(command);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "Asset registered successfully", assetId, null));
    }

    @GetMapping("/assets")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "List or get fixed assets")
    public ResponseEntity<ApiResponse<?>> getAssets(
            @RequestParam(required = false) Long id,
            @RequestParam(required = false) AssetStatus status,
            @AuthenticationPrincipal AuthenticatedPrincipal principal
    ) {
        if (id != null) {
            AssetResult result = getAssetDetailsHandler.execute(new GetAssetDetailsQuery(id, principal.activeOrganizationId()));
            return ResponseEntity.ok(new ApiResponse<>(true, "Asset retrieved successfully", toResponse(result), null));
        }

        List<AssetResult> results = listAssetsHandler.execute(new ListAssetsQuery(principal.activeOrganizationId(), status));
        List<AssetResponse> responses = results.stream().map(this::toResponse).toList();
        return ResponseEntity.ok(new ApiResponse<>(true, "Assets retrieved successfully", responses, null));
    }

    @PutMapping("/assets/{id}/dispose")
    @PreAuthorize("hasAuthority('accounting:assets:manage')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Dispose a fixed asset")
    public ResponseEntity<ApiResponse<Void>> disposeAsset(
            @PathVariable Long id,
            @Valid @RequestBody DisposeAssetRequest request,
            @AuthenticationPrincipal AuthenticatedPrincipal principal
    ) {
        AssetResult existing = getAssetDetailsHandler.execute(new GetAssetDetailsQuery(id, principal.activeOrganizationId()));
        CurrencyCode currency = CurrencyCode.valueOf(existing.currency());

        DisposeAssetCommand command = new DisposeAssetCommand(
                id,
                principal.activeOrganizationId(),
                Money.of(request.saleValue(), currency),
                request.disposalDate()
        );

        disposeAssetHandler.execute(command);
        return ResponseEntity.ok(new ApiResponse<>(true, "Asset disposed successfully", null, null));
    }

    @PostMapping("/assets/depreciation-runs")
    @PreAuthorize("hasAuthority('accounting:assets:manage')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Execute depreciation run for active assets")
    public ResponseEntity<ApiResponse<Integer>> runDepreciation(
            @AuthenticationPrincipal AuthenticatedPrincipal principal
    ) {
        RunDepreciationCommand command = new RunDepreciationCommand(
                principal.activeOrganizationId(),
                LocalDate.now()
        );

        int count = runDepreciationHandler.execute(command);
        return ResponseEntity.ok(new ApiResponse<>(true, "Depreciation run completed", count, null));
    }

    private AssetResponse toResponse(AssetResult result) {
        return new AssetResponse(
                result.id(),
                result.organizationId(),
                result.name(),
                result.tagNumber(),
                result.category(),
                result.purchaseDate(),
                result.purchaseValue(),
                result.salvageValue(),
                result.usefulLifeMonths(),
                result.depreciationMethod(),
                result.accumulatedDepreciation(),
                result.currentBookValue(),
                result.status(),
                result.currency(),
                result.disposalDate(),
                result.saleValue()
        );
    }
}
