package com.atlashub.authentication.presentation.rest;

import com.atlashub.authentication.application.command.ChangePassword.ChangePasswordCommand;
import com.atlashub.authentication.application.command.ChangePassword.ChangePasswordHandler;
import com.atlashub.authentication.application.command.InitiatePasswordReset.InitiatePasswordResetCommand;
import com.atlashub.authentication.application.command.InitiatePasswordReset.InitiatePasswordResetHandler;
import com.atlashub.authentication.application.command.Login.LoginCommand;
import com.atlashub.authentication.application.command.Login.LoginHandler;
import com.atlashub.authentication.application.command.Login.LoginResponse;
import com.atlashub.authentication.application.command.Logout.LogoutCommand;
import com.atlashub.authentication.application.command.Logout.LogoutHandler;
import com.atlashub.authentication.application.command.LogoutAllDevices.LogoutAllDevicesCommand;
import com.atlashub.authentication.application.command.LogoutAllDevices.LogoutAllDevicesHandler;
import com.atlashub.authentication.application.command.RefreshToken.RefreshTokenCommand;
import com.atlashub.authentication.application.command.RefreshToken.RefreshTokenHandler;
import com.atlashub.authentication.application.command.RefreshToken.RefreshTokenResponse;
import com.atlashub.authentication.application.command.ResetPassword.ResetPasswordCommand;
import com.atlashub.authentication.application.command.ResetPassword.ResetPasswordHandler;
import com.atlashub.authentication.application.command.RevokeTrustedDevice.RevokeTrustedDeviceCommand;
import com.atlashub.authentication.application.command.RevokeTrustedDevice.RevokeTrustedDeviceHandler;
import com.atlashub.authentication.application.command.SendVerificationEmail.SendVerificationEmailCommand;
import com.atlashub.authentication.application.command.SendVerificationEmail.SendVerificationEmailHandler;
import com.atlashub.authentication.application.command.VerifyEmail.VerifyEmailCommand;
import com.atlashub.authentication.application.command.VerifyEmail.VerifyEmailHandler;
import com.atlashub.authentication.application.query.GetActiveSessions.GetActiveSessionsHandler;
import com.atlashub.authentication.application.query.GetActiveSessions.GetActiveSessionsQuery;
import com.atlashub.authentication.application.query.GetTrustedDevices.GetTrustedDevicesHandler;
import com.atlashub.authentication.application.query.GetTrustedDevices.GetTrustedDevicesQuery;
import com.atlashub.authentication.presentation.dto.*;
import com.atlashub.shared.application.annotation.PublicEndpoint;
import com.atlashub.shared.application.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Authentication", description = "Authentication — login, logout, tokens, password management")
public class AuthController {

    private final LoginHandler loginHandler;
    private final LogoutHandler logoutHandler;
    private final LogoutAllDevicesHandler logoutAllDevicesHandler;
    private final RefreshTokenHandler refreshTokenHandler;
    private final VerifyEmailHandler verifyEmailHandler;
    private final SendVerificationEmailHandler sendVerificationEmailHandler;
    private final InitiatePasswordResetHandler initiatePasswordResetHandler;
    private final ResetPasswordHandler resetPasswordHandler;
    private final ChangePasswordHandler changePasswordHandler;
    private final RevokeTrustedDeviceHandler revokeTrustedDeviceHandler;
    private final GetActiveSessionsHandler getActiveSessionsHandler;
    private final GetTrustedDevicesHandler getTrustedDevicesHandler;

    public AuthController(LoginHandler loginHandler,
                          LogoutHandler logoutHandler,
                          LogoutAllDevicesHandler logoutAllDevicesHandler,
                          RefreshTokenHandler refreshTokenHandler,
                          VerifyEmailHandler verifyEmailHandler,
                          SendVerificationEmailHandler sendVerificationEmailHandler,
                          InitiatePasswordResetHandler initiatePasswordResetHandler,
                          ResetPasswordHandler resetPasswordHandler,
                          ChangePasswordHandler changePasswordHandler,
                          RevokeTrustedDeviceHandler revokeTrustedDeviceHandler,
                          GetActiveSessionsHandler getActiveSessionsHandler,
                          GetTrustedDevicesHandler getTrustedDevicesHandler) {
        this.loginHandler = loginHandler;
        this.logoutHandler = logoutHandler;
        this.logoutAllDevicesHandler = logoutAllDevicesHandler;
        this.refreshTokenHandler = refreshTokenHandler;
        this.verifyEmailHandler = verifyEmailHandler;
        this.sendVerificationEmailHandler = sendVerificationEmailHandler;
        this.initiatePasswordResetHandler = initiatePasswordResetHandler;
        this.resetPasswordHandler = resetPasswordHandler;
        this.changePasswordHandler = changePasswordHandler;
        this.revokeTrustedDeviceHandler = revokeTrustedDeviceHandler;
        this.getActiveSessionsHandler = getActiveSessionsHandler;
        this.getTrustedDevicesHandler = getTrustedDevicesHandler;
    }

    // ── Public ────────────────────────────────────────────────────────────────

    @PublicEndpoint
    @PostMapping("/login")
    @Operation(summary = "Login with email and password")
    public ResponseEntity<ApiResponse<LoginWebResponse>> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest) {
        LoginResponse response = loginHandler.execute(new LoginCommand(
                request.email(), request.password(), request.deviceFingerprint(),
                getClientIp(httpRequest), httpRequest.getHeader("User-Agent")
        ));
        return ResponseEntity.ok(new ApiResponse<>(true, "Login processed", toWebResponse(response), null));
    }

    @PublicEndpoint
    @PostMapping("/refresh")
    @Operation(summary = "Refresh access token")
    public ResponseEntity<ApiResponse<RefreshTokenWebResponse>> refresh(
            @Valid @RequestBody RefreshTokenRequest request) {
        RefreshTokenResponse response = refreshTokenHandler.execute(new RefreshTokenCommand(request.refreshToken()));
        return ResponseEntity.ok(new ApiResponse<>(true, "Token refreshed", toWebResponse(response), null));
    }

    @PublicEndpoint
    @PostMapping("/email/verify")
    @Operation(summary = "Verify email address with OTP")
    public ResponseEntity<ApiResponse<Void>> verifyEmail(
            @Valid @RequestBody VerifyEmailRequest request) {
        verifyEmailHandler.execute(new VerifyEmailCommand(request.email(), request.otp()));
        return ResponseEntity.ok(new ApiResponse<>(true, "Email verified successfully", null, null));
    }

    @PublicEndpoint
    @PostMapping("/email/resend-verification")
    @Operation(summary = "Resend email verification OTP")
    public ResponseEntity<ApiResponse<Void>> resendVerification(
            @Valid @RequestBody EmailRequest request) {
        sendVerificationEmailHandler.execute(new SendVerificationEmailCommand(request.email()));
        return ResponseEntity.ok(new ApiResponse<>(true, "Verification email sent if account exists", null, null));
    }

    @PublicEndpoint
    @PostMapping("/password/forgot")
    @Operation(summary = "Initiate password reset")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(
            @Valid @RequestBody EmailRequest request) {
        initiatePasswordResetHandler.execute(new InitiatePasswordResetCommand(request.email()));
        return ResponseEntity.ok(new ApiResponse<>(true, "Password reset email sent if account exists", null, null));
    }

    @PublicEndpoint
    @PostMapping("/password/reset")
    @Operation(summary = "Reset password using OTP")
    public ResponseEntity<ApiResponse<Void>> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request) {
        resetPasswordHandler.execute(new ResetPasswordCommand(request.email(), request.otp(), request.newPassword()));
        return ResponseEntity.ok(new ApiResponse<>(true, "Password reset successfully", null, null));
    }

    // ── Authenticated ─────────────────────────────────────────────────────────

    @PostMapping("/logout")
    @Operation(summary = "Logout current session", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<Void>> logout(
            @Valid @RequestBody LogoutRequest request) {
        logoutHandler.execute(new LogoutCommand(
                request.refreshToken(), request.accessTokenJti(), request.accessTokenExpiresAt()));
        return ResponseEntity.ok(new ApiResponse<>(true, "Logged out successfully", null, null));
    }

    @PostMapping("/logout-all")
    @Operation(summary = "Logout from all devices", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<Void>> logoutAll(@AuthenticationPrincipal Long userId) {
        logoutAllDevicesHandler.execute(new LogoutAllDevicesCommand(userId));
        return ResponseEntity.ok(new ApiResponse<>(true, "Logged out from all devices", null, null));
    }

    @PostMapping("/password/change")
    @Operation(summary = "Change password", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody ChangePasswordRequest request) {
        changePasswordHandler.execute(new ChangePasswordCommand(userId, request.currentPassword(), request.newPassword()));
        return ResponseEntity.ok(new ApiResponse<>(true, "Password changed successfully", null, null));
    }

    @DeleteMapping("/devices")
    @Operation(summary = "Revoke a trusted device", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<Void>> revokeDevice(
            @AuthenticationPrincipal Long userId, @RequestParam Long id) {
        revokeTrustedDeviceHandler.execute(new RevokeTrustedDeviceCommand(userId, id));
        return ResponseEntity.ok(new ApiResponse<>(true, "Device revoked", null, null));
    }

    @GetMapping("/sessions")
    @Operation(summary = "Get all active sessions", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<List<SessionWebResponse>>> getSessions(
            @AuthenticationPrincipal Long userId) {
        List<SessionWebResponse> sessions = getActiveSessionsHandler.execute(new GetActiveSessionsQuery(userId)).stream()
                .map(session -> new SessionWebResponse(
                        session.token(), session.expiresAt(), session.ipAddress(), session.userAgent()))
                .toList();
        return ResponseEntity.ok(new ApiResponse<>(true, "Success", sessions, null));
    }

    @GetMapping("/devices")
    @Operation(summary = "Get trusted devices", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<List<TrustedDeviceWebResponse>>> getDevices(
            @AuthenticationPrincipal Long userId) {
        List<TrustedDeviceWebResponse> devices = getTrustedDevicesHandler.execute(new GetTrustedDevicesQuery(userId)).stream()
                .map(device -> new TrustedDeviceWebResponse(
                        device.id(),
                        device.deviceFingerprint(),
                        device.deviceName(),
                        device.lastSeenIp(),
                        device.trustedAt(),
                        device.expiresAt()))
                .toList();
        return ResponseEntity.ok(new ApiResponse<>(true, "Success", devices, null));
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private String getClientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        return (xff == null || xff.isBlank()) ? request.getRemoteAddr() : xff.split(",")[0].trim();
    }

    private LoginWebResponse toWebResponse(LoginResponse response) {
        return new LoginWebResponse(
                response.status().name(),
                response.accessToken(),
                response.accessTokenExpiresAt(),
                response.refreshToken(),
                response.refreshTokenExpiresAt(),
                response.message());
    }

    private RefreshTokenWebResponse toWebResponse(RefreshTokenResponse response) {
        return new RefreshTokenWebResponse(
                response.accessToken(),
                response.accessTokenExpiresAt(),
                response.refreshToken(),
                response.refreshTokenExpiresAt());
    }
}
