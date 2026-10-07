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
import com.atlashub.authentication.application.command.SendVerificationEmail.SendVerificationEmailCommand;
import com.atlashub.authentication.application.command.SendVerificationEmail.SendVerificationEmailHandler;
import com.atlashub.authentication.application.command.VerifyEmail.VerifyEmailCommand;
import com.atlashub.authentication.application.command.VerifyEmail.VerifyEmailHandler;
import com.atlashub.authentication.presentation.dto.ChangePasswordRequest;
import com.atlashub.authentication.presentation.dto.EmailRequest;
import com.atlashub.authentication.presentation.dto.LoginRequest;
import com.atlashub.authentication.presentation.dto.LoginWebResponse;
import com.atlashub.authentication.presentation.dto.RefreshTokenRequest;
import com.atlashub.authentication.presentation.dto.RefreshTokenWebResponse;
import com.atlashub.authentication.presentation.dto.ResetPasswordRequest;
import com.atlashub.authentication.presentation.dto.VerifyEmailRequest;
import com.atlashub.shared.application.annotation.PublicEndpoint;
import com.atlashub.shared.application.dto.ApiResponse;
import com.atlashub.shared.application.security.AuthenticatedPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Authentication", description = "Authentication and credential management")
public class AuthenticationController {
    private final LoginHandler loginHandler;
    private final LogoutHandler logoutHandler;
    private final LogoutAllDevicesHandler logoutAllDevicesHandler;
    private final RefreshTokenHandler refreshTokenHandler;
    private final VerifyEmailHandler verifyEmailHandler;
    private final SendVerificationEmailHandler sendVerificationEmailHandler;
    private final InitiatePasswordResetHandler initiatePasswordResetHandler;
    private final ResetPasswordHandler resetPasswordHandler;
    private final ChangePasswordHandler changePasswordHandler;

    public AuthenticationController(LoginHandler loginHandler,
                                    LogoutHandler logoutHandler,
                                    LogoutAllDevicesHandler logoutAllDevicesHandler,
                                    RefreshTokenHandler refreshTokenHandler,
                                    VerifyEmailHandler verifyEmailHandler,
                                    SendVerificationEmailHandler sendVerificationEmailHandler,
                                    InitiatePasswordResetHandler initiatePasswordResetHandler,
                                    ResetPasswordHandler resetPasswordHandler,
                                    ChangePasswordHandler changePasswordHandler) {
        this.loginHandler = loginHandler;
        this.logoutHandler = logoutHandler;
        this.logoutAllDevicesHandler = logoutAllDevicesHandler;
        this.refreshTokenHandler = refreshTokenHandler;
        this.verifyEmailHandler = verifyEmailHandler;
        this.sendVerificationEmailHandler = sendVerificationEmailHandler;
        this.initiatePasswordResetHandler = initiatePasswordResetHandler;
        this.resetPasswordHandler = resetPasswordHandler;
        this.changePasswordHandler = changePasswordHandler;
    }

    @PublicEndpoint
    @PostMapping("/login")
    @Operation(summary = "Login with email and password")
    public ResponseEntity<ApiResponse<LoginWebResponse>> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest) {
        LoginResponse response = loginHandler.execute(new LoginCommand(
                request.email(), request.password(), request.deviceFingerprint(),
                clientIp(httpRequest), httpRequest.getHeader("User-Agent")));
        return ok(toWebResponse(response));
    }

    @PublicEndpoint
    @PostMapping("/refresh")
    @Operation(summary = "Refresh access token")
    public ResponseEntity<ApiResponse<RefreshTokenWebResponse>> refresh(
            @Valid @RequestBody RefreshTokenRequest request) {
        return ok(toWebResponse(refreshTokenHandler.execute(
                new RefreshTokenCommand(request.refreshToken(), request.deviceFingerprint()))));
    }

    @PublicEndpoint
    @PostMapping("/email/verify")
    @Operation(summary = "Verify email address")
    public ResponseEntity<ApiResponse<Void>> verifyEmail(@Valid @RequestBody VerifyEmailRequest request) {
        verifyEmailHandler.execute(new VerifyEmailCommand(request.email(), request.otp()));
        return done("Email verified successfully");
    }

    @PublicEndpoint
    @PostMapping("/email/resend-verification")
    @Operation(summary = "Resend email verification code")
    public ResponseEntity<ApiResponse<Void>> resendVerification(@Valid @RequestBody EmailRequest request) {
        sendVerificationEmailHandler.execute(new SendVerificationEmailCommand(request.email()));
        return done("Verification email sent if account exists");
    }

    @PublicEndpoint
    @PostMapping("/password/forgot")
    @Operation(summary = "Request password reset code")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(@Valid @RequestBody EmailRequest request) {
        initiatePasswordResetHandler.execute(new InitiatePasswordResetCommand(request.email()));
        return done("Password reset email sent if account exists");
    }

    @PublicEndpoint
    @PostMapping("/password/reset")
    @Operation(summary = "Reset password with verification code")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        resetPasswordHandler.execute(new ResetPasswordCommand(
                request.email(), request.otp(), request.newPassword()));
        return done("Password reset successfully");
    }

    @PostMapping("/logout")
    @Operation(summary = "Log out current session")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("principal.userId() != null")
    public ResponseEntity<ApiResponse<Void>> logout(
            @AuthenticationPrincipal AuthenticatedPrincipal principal) {
        logoutHandler.execute(new LogoutCommand(
                principal.userId(), Long.valueOf(principal.sessionId()),
                principal.tokenId(), principal.tokenExpiresAt()));
        return done("Logged out successfully");
    }

    @PostMapping("/logout-all")
    @Operation(summary = "Log out from all sessions")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("principal.userId() != null")
    public ResponseEntity<ApiResponse<Void>> logoutAll(
            @AuthenticationPrincipal AuthenticatedPrincipal principal) {
        logoutAllDevicesHandler.execute(new LogoutAllDevicesCommand(principal.userId()));
        return done("Logged out from all devices");
    }

    @PostMapping("/password/change")
    @Operation(summary = "Change current password")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("principal.userId() != null")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @AuthenticationPrincipal AuthenticatedPrincipal principal,
            @Valid @RequestBody ChangePasswordRequest request) {
        changePasswordHandler.execute(new ChangePasswordCommand(
                principal.userId(), request.currentPassword(), request.newPassword()));
        return done("Password changed successfully");
    }

    private String clientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        return forwarded == null || forwarded.isBlank()
                ? request.getRemoteAddr() : forwarded.split(",")[0].trim();
    }

    private LoginWebResponse toWebResponse(LoginResponse response) {
        return new LoginWebResponse(
                response.status(), response.accessToken(), response.accessTokenExpiresAt(),
                response.refreshToken(), response.refreshTokenExpiresAt(), response.message());
    }

    private RefreshTokenWebResponse toWebResponse(RefreshTokenResponse response) {
        return new RefreshTokenWebResponse(
                response.accessToken(), response.accessTokenExpiresAt(),
                response.refreshToken(), response.refreshTokenExpiresAt());
    }

    private <T> ResponseEntity<ApiResponse<T>> ok(T value) {
        return ResponseEntity.ok(new ApiResponse<>(true, "Success", value, null));
    }

    private ResponseEntity<ApiResponse<Void>> done(String message) {
        return ResponseEntity.ok(new ApiResponse<>(true, message, null, null));
    }
}
