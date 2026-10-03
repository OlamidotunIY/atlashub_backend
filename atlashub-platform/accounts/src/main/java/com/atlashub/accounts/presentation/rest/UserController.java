package com.atlashub.accounts.presentation.rest;

import com.atlashub.accounts.application.command.RegisterOrg.RegisterOrganizationCommand;
import com.atlashub.accounts.application.command.RegisterOrg.RegisterOrganizationHandler;
import com.atlashub.accounts.application.command.UpdateUserProfile.UpdateUserProfileCommand;
import com.atlashub.accounts.application.command.UpdateUserProfile.UpdateUserProfileHandler;
import com.atlashub.accounts.application.query.GetUserProfile.GetUserProfileHandler;
import com.atlashub.accounts.application.query.GetUserProfile.GetUserProfileQuery;
import com.atlashub.accounts.application.query.GetUserProfile.UserProfileResult;
import com.atlashub.accounts.domain.valueobject.AtlasHubRegistrationType;
import com.atlashub.accounts.domain.valueobject.SupportedIndustry;
import com.atlashub.accounts.presentation.dto.OrganizationSummaryResponse;
import com.atlashub.accounts.presentation.dto.RegisterRequest;
import com.atlashub.accounts.presentation.dto.UpdateProfileRequest;
import com.atlashub.accounts.presentation.dto.UserProfileResponse;
import com.atlashub.shared.application.annotation.PublicEndpoint;
import com.atlashub.shared.application.dto.ApiResponse;
import com.atlashub.shared.application.security.AuthenticatedPrincipal;
import com.atlashub.shared.domain.valueobject.Country;
import com.atlashub.shared.domain.valueobject.PhoneNumber;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Users", description = "User registration and profile management")
@Validated
public class UserController {

    private final RegisterOrganizationHandler registerHandler;
    private final UpdateUserProfileHandler updateProfileHandler;
    private final GetUserProfileHandler getUserProfileHandler;

    public UserController(RegisterOrganizationHandler registerHandler,
                          UpdateUserProfileHandler updateProfileHandler,
                          GetUserProfileHandler getUserProfileHandler) {
        this.registerHandler = registerHandler;
        this.updateProfileHandler = updateProfileHandler;
        this.getUserProfileHandler = getUserProfileHandler;
    }

    @PublicEndpoint
    @PostMapping("/register")
    @Operation(summary = "Register a new user and organization")
    public ResponseEntity<ApiResponse<Void>> register(@Valid @RequestBody RegisterRequest request) {
        registerHandler.execute(new RegisterOrganizationCommand(
                request.businessName(),
                AtlasHubRegistrationType.parse(request.registrationType()),
                SupportedIndustry.parse(request.industry()),
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
        return done("Registration successful. Please verify your email.");
    }

    @GetMapping("/me")
    @Operation(summary = "Get current user profile and organizations", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<UserProfileResponse>> getMe(
            @AuthenticationPrincipal AuthenticatedPrincipal principal) {
        UserProfileResult result = getUserProfileHandler.execute(new GetUserProfileQuery(principal.userId()));
        return ok(toWebResponse(result));
    }

    @PutMapping("/me")
    @Operation(summary = "Update user profile", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<Void>> updateProfile(
            @AuthenticationPrincipal AuthenticatedPrincipal principal,
            @Valid @RequestBody UpdateProfileRequest request) {
        updateProfileHandler.execute(new UpdateUserProfileCommand(
                principal.userId(), request.firstName(), request.lastName(),
                request.phone() != null ? new PhoneNumber(request.phone()) : null,
                request.locale(),
                request.timezone()
        ));
        return done("Profile updated");
    }

    private <T> ResponseEntity<ApiResponse<T>> ok(T value) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Success", value, null));
    }

    private ResponseEntity<ApiResponse<Void>> done(String message) {
        return ResponseEntity.ok(new ApiResponse<>(true, message, null, null));
    }

    private UserProfileResponse toWebResponse(UserProfileResult result) {
        return new UserProfileResponse(
                result.id(),
                result.firstName(),
                result.lastName(),
                result.email(),
                result.phone(),
                result.imageUrl(),
                result.country(),
                result.activeOrganizationId(),
                result.createdAt(),
                result.organizations().stream()
                        .map(org -> new OrganizationSummaryResponse(
                                org.id(), org.businessName(), org.country(), org.baseCurrency(), org.logoUrl()))
                        .toList());
    }
}
