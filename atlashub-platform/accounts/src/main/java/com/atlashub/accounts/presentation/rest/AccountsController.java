package com.atlashub.accounts.presentation.rest;

import com.atlashub.accounts.application.command.RegisterOrg.RegisterOrganizationCommand;
import com.atlashub.accounts.application.command.RegisterOrg.RegisterOrganizationHandler;
import com.atlashub.accounts.application.command.SwitchActiveOrganization.SwitchActiveOrganizationCommand;
import com.atlashub.accounts.application.command.SwitchActiveOrganization.SwitchActiveOrganizationHandler;
import com.atlashub.accounts.application.command.UpdateOrganizationDetails.UpdateOrganizationDetailsCommand;
import com.atlashub.accounts.application.command.UpdateOrganizationDetails.UpdateOrganizationDetailsHandler;
import com.atlashub.accounts.application.command.UpdateUserProfile.UpdateUserProfileCommand;
import com.atlashub.accounts.application.command.UpdateUserProfile.UpdateUserProfileHandler;
import com.atlashub.accounts.application.query.GetOrganizationDetails.GetOrganizationDetailsHandler;
import com.atlashub.accounts.application.query.GetOrganizationDetails.GetOrganizationDetailsQuery;
import com.atlashub.accounts.application.query.GetOrganizationDetails.OrganizationDetailsResult;
import com.atlashub.accounts.application.query.GetUserProfile.GetUserProfileHandler;
import com.atlashub.accounts.application.query.GetUserProfile.GetUserProfileQuery;
import com.atlashub.accounts.application.query.GetUserProfile.UserProfileResult;
import com.atlashub.accounts.domain.valueobject.BusinessSize;
import com.atlashub.accounts.domain.valueobject.BusinessType;
import com.atlashub.accounts.presentation.dto.*;
import com.atlashub.shared.application.annotation.PublicEndpoint;
import com.atlashub.shared.application.dto.ApiResponse;
import com.atlashub.shared.domain.valueobject.Country;
import com.atlashub.shared.domain.valueobject.PhoneNumber;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Accounts", description = "User and organization account management")
public class AccountsController {

    private final RegisterOrganizationHandler registerHandler;
    private final UpdateUserProfileHandler updateProfileHandler;
    private final UpdateOrganizationDetailsHandler updateOrgHandler;
    private final SwitchActiveOrganizationHandler switchOrgHandler;
    private final GetUserProfileHandler getUserProfileHandler;
    private final GetOrganizationDetailsHandler getOrgDetailsHandler;

    public AccountsController(RegisterOrganizationHandler registerHandler,
                              UpdateUserProfileHandler updateProfileHandler,
                              UpdateOrganizationDetailsHandler updateOrgHandler,
                              SwitchActiveOrganizationHandler switchOrgHandler,
                              GetUserProfileHandler getUserProfileHandler,
                              GetOrganizationDetailsHandler getOrgDetailsHandler) {
        this.registerHandler = registerHandler;
        this.updateProfileHandler = updateProfileHandler;
        this.updateOrgHandler = updateOrgHandler;
        this.switchOrgHandler = switchOrgHandler;
        this.getUserProfileHandler = getUserProfileHandler;
        this.getOrgDetailsHandler = getOrgDetailsHandler;
    }

    // ── Public ────────────────────────────────────────────────────────────────

    @PublicEndpoint
    @PostMapping("/register")
    @Operation(summary = "Register a new user and organization")
    public ResponseEntity<ApiResponse<Void>> register(@Valid @RequestBody RegisterRequest request) {
        registerHandler.execute(new RegisterOrganizationCommand(
                request.businessName(),
                BusinessType.valueOf(request.businessType()),
                BusinessSize.valueOf(request.businessSize()),
                request.industry(),
                request.description(),
                request.logoUrl(),
                request.websiteUrl(),
                new Country(request.country()),
                request.firstName(),
                request.lastName(),
                request.email(),
                request.password(),
                false
        ));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, "Registration successful. Please verify your email.", null, null));
    }

    // ── Authenticated ─────────────────────────────────────────────────────────

    @GetMapping("/me")
    @Operation(summary = "Get current user profile and organizations", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<UserProfileResult>> getMe(@AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Success",
                getUserProfileHandler.execute(new GetUserProfileQuery(userId)), null));
    }

    @PutMapping("/me")
    @Operation(summary = "Update user profile", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<Void>> updateProfile(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody UpdateProfileRequest request) {
        updateProfileHandler.execute(new UpdateUserProfileCommand(
                userId, request.firstName(), request.lastName(),
                request.phone() != null ? new PhoneNumber(request.phone()) : null
        ));
        return ResponseEntity.ok(new ApiResponse<>(true, "Profile updated", null, null));
    }

    @GetMapping("/organizations")
    @Operation(summary = "Get organization details", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<OrganizationDetailsResult>> getOrganization(@RequestParam Long id) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Success",
                getOrgDetailsHandler.execute(new GetOrganizationDetailsQuery(id)), null));
    }

    @PutMapping("/organizations")
    @Operation(summary = "Update organization details", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<Void>> updateOrganization(
            @RequestParam Long id,
            @Valid @RequestBody UpdateOrganizationRequest request) {
        updateOrgHandler.execute(new UpdateOrganizationDetailsCommand(
                id, request.businessName(), request.description(),
                request.logoUrl(), request.industry(), request.websiteUrl()
        ));
        return ResponseEntity.ok(new ApiResponse<>(true, "Organization updated", null, null));
    }

    @PostMapping("/organizations/switch")
    @Operation(summary = "Switch active organization", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<Void>> switchOrganization(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody SwitchOrganizationRequest request) {
        switchOrgHandler.execute(new SwitchActiveOrganizationCommand(userId, request.organizationId()));
        return ResponseEntity.ok(new ApiResponse<>(true, "Active organization switched", null, null));
    }
}
