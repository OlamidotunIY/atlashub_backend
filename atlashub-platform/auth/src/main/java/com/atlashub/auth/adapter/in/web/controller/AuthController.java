package com.atlashub.auth.adapter.in.web.controller;

import com.atlashub.auth.application.command.AuthenticateCommand;
import com.atlashub.auth.application.command.ChangeTemporaryPasswordCommand;
import com.atlashub.auth.application.command.CompleteTwoFactorCommand;
import com.atlashub.auth.application.command.CompleteVerificationCommand;
import com.atlashub.auth.application.command.RefreshTokenCommand;
import com.atlashub.auth.application.command.RevokeSessionCommand;
import com.atlashub.auth.application.command.SetupPasswordCommand;
import com.atlashub.auth.application.result.AuthResponseDto;
import com.atlashub.auth.application.result.AuthTokenDto;
import com.atlashub.auth.application.result.VerificationResponseDto;
import com.atlashub.auth.application.usecase.AuthenticateUseCase;
import com.atlashub.auth.application.usecase.ChangeTemporaryPasswordUseCase;
import com.atlashub.auth.application.usecase.CompleteTwoFactorUseCase;
import com.atlashub.auth.application.usecase.CompleteVerificationUseCase;
import com.atlashub.auth.application.usecase.RefreshTokenUseCase;
import com.atlashub.auth.application.usecase.RevokeSessionUseCase;
import com.atlashub.auth.application.usecase.SetupPasswordUseCase;
import com.atlashub.auth.application.usecase.ResendSetupTokenUseCase;
import com.atlashub.auth.application.command.ResendSetupTokenCommand;
import com.atlashub.auth.adapter.in.web.request.ResendSetupTokenRequestDto;
import com.atlashub.auth.adapter.in.web.request.ChangeTemporaryPasswordRequestDto;
import com.atlashub.auth.adapter.in.web.request.CompleteVerificationRequestDto;
import com.atlashub.auth.adapter.in.web.request.LoginRequestDto;
import com.atlashub.auth.adapter.in.web.request.SetupPasswordRequestDto;
import com.atlashub.shared.application.dto.ApiResponse;
import com.atlashub.shared.adapter.security.PublicEndpoint;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.atlashub.auth.adapter.in.web.request.VerifyMfaRequestDto;
import com.atlashub.auth.adapter.in.web.request.RefreshTokenRequestDto;
import com.atlashub.auth.adapter.in.web.request.LogoutRequestDto;
import com.atlashub.auth.application.query.GetAuthenticatedUserQuery;
import com.atlashub.auth.application.result.AuthenticatedUserDto;
import com.atlashub.auth.application.usecase.GetAuthenticatedUserUseCase;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.http.HttpStatus;
import java.security.Principal;

@RestController
@io.swagger.v3.oas.annotations.tags.Tag(name = "Authentication", description = "Authentication and session management")
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticateUseCase authenticateUseCase;
    private final ChangeTemporaryPasswordUseCase changeTemporaryPasswordUseCase;
    private final CompleteTwoFactorUseCase completeTwoFactorUseCase;
    private final RefreshTokenUseCase refreshTokenUseCase;
    private final RevokeSessionUseCase revokeSessionUseCase;
    private final CompleteVerificationUseCase completeVerificationUseCase;
    private final SetupPasswordUseCase setupPasswordUseCase;
    private final ResendSetupTokenUseCase resendSetupTokenUseCase;
    private final GetAuthenticatedUserUseCase getAuthenticatedUserUseCase;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<AuthenticatedUserDto>> getMe(Principal principal) {
        if (principal == null || principal.getName() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse<>(false, "Unauthorized", null, null));
        }

        Long userId = Long.valueOf(principal.getName());
        GetAuthenticatedUserQuery query = new GetAuthenticatedUserQuery(userId);
        
        AuthenticatedUserDto result = getAuthenticatedUserUseCase.execute(query);

        return ResponseEntity.ok(new ApiResponse<>(true, "Authenticated user retrieved successfully", result, null));
    }

    @PublicEndpoint
    @PostMapping("/setup-password/resend")
    public ResponseEntity<ApiResponse<Void>> resendSetupToken(
            @Valid @RequestBody ResendSetupTokenRequestDto request) {
        
        ResendSetupTokenCommand command = 
            new ResendSetupTokenCommand(request.identifier());
        
        return ResponseEntity.ok(resendSetupTokenUseCase.execute(command));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponseDto>> login(
            @Valid @RequestBody LoginRequestDto request,
            HttpServletRequest httpRequest) {
        
        AuthenticateCommand command = new AuthenticateCommand(
                request.getIdentifier(),
                request.getPassword(),
                getClientIp(httpRequest),
                getUserAgent(httpRequest)
        );
        
        return ResponseEntity.ok(authenticateUseCase.execute(command));
    }

    @PostMapping("/password/change")
    public ResponseEntity<ApiResponse<AuthResponseDto>> changeTemporaryPassword(
            @Valid @RequestBody ChangeTemporaryPasswordRequestDto request,
            HttpServletRequest httpRequest) {
        
        ChangeTemporaryPasswordCommand command = new ChangeTemporaryPasswordCommand(
                request.getIdentifier(),
                request.getOldPassword(),
                request.getNewPassword(),
                getClientIp(httpRequest),
                getUserAgent(httpRequest)
        );
        
        return ResponseEntity.ok(changeTemporaryPasswordUseCase.execute(command));
    }
    
    @PublicEndpoint
    @PostMapping("/setup-password")
    public ResponseEntity<ApiResponse<AuthResponseDto>> setupPassword(
            @Valid @RequestBody SetupPasswordRequestDto request,
            HttpServletRequest httpRequest) {
        
        SetupPasswordCommand command = new SetupPasswordCommand(
                request.setupToken(),
                request.newPassword(),
                getClientIp(httpRequest),
                getUserAgent(httpRequest)
        );
        
        return ResponseEntity.ok(setupPasswordUseCase.execute(command));
    }

    @PublicEndpoint
    @PostMapping("/verify-email")
    public ResponseEntity<ApiResponse<VerificationResponseDto>> verifyEmail(
            @Valid @RequestBody CompleteVerificationRequestDto request) {
        
        CompleteVerificationCommand command = new CompleteVerificationCommand(
                request.type(),
                request.identifier(),
                request.code()
        );
        
        return ResponseEntity.ok(completeVerificationUseCase.execute(command));
    }

    @PostMapping("/mfa/verify")
    public ResponseEntity<ApiResponse<AuthTokenDto>> verifyMfa(
            @Valid @RequestBody VerifyMfaRequestDto request,
            HttpServletRequest httpRequest) {
        
        CompleteTwoFactorCommand command = new CompleteTwoFactorCommand(
                request.preAuthToken(),
                request.code(),
                getClientIp(httpRequest),
                getUserAgent(httpRequest)
        );
        
        return ResponseEntity.ok(completeTwoFactorUseCase.execute(command));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthTokenDto>> refreshToken(
            @Valid @RequestBody RefreshTokenRequestDto request,
            HttpServletRequest httpRequest) {
        
        RefreshTokenCommand command = new RefreshTokenCommand(
                request.refreshToken(),
                getClientIp(httpRequest),
                getUserAgent(httpRequest)
        );
        
        return ResponseEntity.ok(refreshTokenUseCase.execute(command));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @Valid @RequestBody LogoutRequestDto request) {
        
        RevokeSessionCommand command = new RevokeSessionCommand(
                request.jti()
        );
        
        return ResponseEntity.ok(revokeSessionUseCase.execute(command));
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private String getUserAgent(HttpServletRequest request) {
        return request.getHeader("User-Agent");
    }
}

