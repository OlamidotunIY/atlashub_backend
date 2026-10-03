package com.atlashub.accounts.presentation.rest;

import com.atlashub.accounts.application.command.UpdateOrganizationDetails.UpdateOrganizationDetailsCommand;
import com.atlashub.accounts.application.command.UpdateOrganizationDetails.UpdateOrganizationDetailsHandler;
import com.atlashub.accounts.application.query.GetOrganizationDetails.GetOrganizationDetailsHandler;
import com.atlashub.accounts.application.query.GetOrganizationDetails.GetOrganizationDetailsQuery;
import com.atlashub.accounts.application.query.GetOrganizationDetails.OrganizationDetailsResult;
import com.atlashub.accounts.presentation.dto.OrganizationDetailsResponse;
import com.atlashub.accounts.presentation.dto.UpdateOrganizationRequest;
import com.atlashub.accounts.domain.valueobject.SupportedIndustry;
import com.atlashub.shared.application.security.AuthenticatedPrincipal;
import com.atlashub.shared.application.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Organizations", description = "Organization profile management")
@Validated
public class OrganizationController {

    private final UpdateOrganizationDetailsHandler updateOrgHandler;
    private final GetOrganizationDetailsHandler getOrgDetailsHandler;

    public OrganizationController(UpdateOrganizationDetailsHandler updateOrgHandler,
                                  GetOrganizationDetailsHandler getOrgDetailsHandler) {
        this.updateOrgHandler = updateOrgHandler;
        this.getOrgDetailsHandler = getOrgDetailsHandler;
    }

    @GetMapping("/organizations")
    @Operation(summary = "Get organization details", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<OrganizationDetailsResponse>> getOrganization(
            @AuthenticationPrincipal AuthenticatedPrincipal principal) {
        OrganizationDetailsResult result = getOrgDetailsHandler.execute(
                new GetOrganizationDetailsQuery(principal.activeOrganizationId()));
        return ok(toWebResponse(result));
    }

    @PutMapping("/organizations")
    @Operation(summary = "Update organization details", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<Void>> updateOrganization(
            @AuthenticationPrincipal AuthenticatedPrincipal principal,
            @Valid @RequestBody UpdateOrganizationRequest request) {
        updateOrgHandler.execute(new UpdateOrganizationDetailsCommand(
                principal.activeOrganizationId(), request.businessName(), request.description(),
                request.logoUrl(), request.industry() == null ? null : SupportedIndustry.parse(request.industry()),
                request.websiteUrl()
        ));
        return done("Organization updated");
    }

    private <T> ResponseEntity<ApiResponse<T>> ok(T value) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Success", value, null));
    }

    private ResponseEntity<ApiResponse<Void>> done(String message) {
        return ResponseEntity.ok(new ApiResponse<>(true, message, null, null));
    }

    private OrganizationDetailsResponse toWebResponse(OrganizationDetailsResult result) {
        return new OrganizationDetailsResponse(
                result.id(),
                result.businessName(),
                result.registrationType(),
                result.industry(),
                result.registrationDate(),
                result.description(),
                result.logoUrl(),
                result.websiteUrl(),
                result.country(),
                result.baseCurrency(),
                result.createdAt());
    }
}
