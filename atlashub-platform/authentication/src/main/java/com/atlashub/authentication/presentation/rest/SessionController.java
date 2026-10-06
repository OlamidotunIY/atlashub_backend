package com.atlashub.authentication.presentation.rest;

import com.atlashub.authentication.application.command.RefreshToken.RefreshTokenResponse;
import com.atlashub.authentication.application.command.SwitchEnvironment.SwitchEnvironmentCommand;
import com.atlashub.authentication.application.command.SwitchEnvironment.SwitchEnvironmentHandler;
import com.atlashub.authentication.application.command.SwitchOrganization.SwitchOrganizationCommand;
import com.atlashub.authentication.application.command.SwitchOrganization.SwitchOrganizationHandler;
import com.atlashub.authentication.application.query.GetActiveSessions.GetActiveSessionsHandler;
import com.atlashub.authentication.application.query.GetActiveSessions.GetActiveSessionsQuery;
import com.atlashub.authentication.presentation.dto.RefreshTokenWebResponse;
import com.atlashub.authentication.presentation.dto.SessionWebResponse;
import com.atlashub.authentication.presentation.dto.SwitchEnvironmentRequest;
import com.atlashub.authentication.presentation.dto.SwitchOrganizationRequest;
import com.atlashub.shared.application.dto.ApiResponse;
import com.atlashub.shared.application.security.AuthenticatedPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Sessions", description = "Active session and context switching")
@PreAuthorize("principal.userId() != null")
public class SessionController {
    private final GetActiveSessionsHandler getActiveSessionsHandler;
    private final SwitchOrganizationHandler switchOrganizationHandler;
    private final SwitchEnvironmentHandler switchEnvironmentHandler;

    public SessionController(
            GetActiveSessionsHandler getActiveSessionsHandler,
            SwitchOrganizationHandler switchOrganizationHandler,
            SwitchEnvironmentHandler switchEnvironmentHandler
    ) {
        this.getActiveSessionsHandler = getActiveSessionsHandler;
        this.switchOrganizationHandler = switchOrganizationHandler;
        this.switchEnvironmentHandler = switchEnvironmentHandler;
    }

    @GetMapping("/sessions")
    @Operation(summary = "List active sessions")
    public ResponseEntity<ApiResponse<List<SessionWebResponse>>> list(
            @AuthenticationPrincipal AuthenticatedPrincipal principal) {
        List<SessionWebResponse> sessions = getActiveSessionsHandler
                .execute(new GetActiveSessionsQuery(principal.userId())).stream()
                .map(session -> new SessionWebResponse(
                        session.id(), session.organizationId(), session.environment(),
                        session.expiresAt(), session.ipAddress(), session.userAgent()))
                .toList();
        return ok(sessions);
    }

    @PostMapping("/organizations/switch")
    @Operation(summary = "Switch active organization")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<RefreshTokenWebResponse>> switchOrganization(
            @AuthenticationPrincipal AuthenticatedPrincipal principal,
            @Valid @RequestBody SwitchOrganizationRequest request) {
        RefreshTokenResponse response = switchOrganizationHandler.execute(new SwitchOrganizationCommand(
                principal.userId(), Long.valueOf(principal.sessionId()), request.organizationId()));
        return ok(toWebResponse(response));
    }

    @PostMapping("/environments/switch")
    @Operation(summary = "Switch API environment")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ApiResponse<RefreshTokenWebResponse>> switchEnvironment(
            @AuthenticationPrincipal AuthenticatedPrincipal principal,
            @Valid @RequestBody SwitchEnvironmentRequest request) {
        RefreshTokenResponse response = switchEnvironmentHandler.execute(new SwitchEnvironmentCommand(
                principal.userId(), Long.valueOf(principal.sessionId()), request.environment()));
        return ok(toWebResponse(response));
    }

    private RefreshTokenWebResponse toWebResponse(RefreshTokenResponse response) {
        return new RefreshTokenWebResponse(
                response.accessToken(), response.accessTokenExpiresAt(),
                response.refreshToken(), response.refreshTokenExpiresAt());
    }

    private <T> ResponseEntity<ApiResponse<T>> ok(T value) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Success", value, null));
    }
}
