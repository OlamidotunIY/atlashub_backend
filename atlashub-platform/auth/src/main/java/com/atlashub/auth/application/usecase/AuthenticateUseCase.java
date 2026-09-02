package com.atlashub.auth.application.usecase;

import com.atlashub.auth.application.command.AuthenticateCommand;
import com.atlashub.auth.application.result.AuthResponseDto;
import com.atlashub.auth.application.port.in.PasswordEncoderPort;
import com.atlashub.auth.application.port.in.PreAuthTokenStorePort;
import com.atlashub.auth.application.port.in.TokenGeneratorPort;
import com.atlashub.auth.application.service.TokenIssuanceService;
import com.atlashub.auth.domain.exception.AuthErrorCode;
import com.atlashub.auth.domain.model.AuthAccount;
import com.atlashub.auth.domain.valueobject.AuthStatus;
import com.atlashub.auth.domain.repository.AuthAccountRepository;
import com.atlashub.shared.application.dto.ApiResponse;
import com.atlashub.shared.domain.exception.BusinessRuleException;
import com.atlashub.shared.domain.exception.NotFoundException;
import com.atlashub.shared.application.usecase.BaseUseCase;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.atlashub.shared.application.api.OrganizationMemberQueryApi;

@Service
public class AuthenticateUseCase extends BaseUseCase<AuthenticateCommand, ApiResponse<AuthResponseDto>> {

    private final AuthAccountRepository authAccountRepository;
    private final PasswordEncoderPort passwordEncoderPort;
    private final TokenGeneratorPort tokenGeneratorPort;
    private final PreAuthTokenStorePort preAuthTokenStorePort;
    private final TokenIssuanceService tokenIssuanceService;
    private final OrganizationMemberQueryApi organizationMemberQueryApi;

    public AuthenticateUseCase(
            AuthAccountRepository authAccountRepository,
            PasswordEncoderPort passwordEncoderPort,
            TokenGeneratorPort tokenGeneratorPort,
            PreAuthTokenStorePort preAuthTokenStorePort,
            TokenIssuanceService tokenIssuanceService,
            OrganizationMemberQueryApi organizationMemberQueryApi) {
        this.authAccountRepository = authAccountRepository;
        this.passwordEncoderPort = passwordEncoderPort;
        this.tokenGeneratorPort = tokenGeneratorPort;
        this.preAuthTokenStorePort = preAuthTokenStorePort;
        this.tokenIssuanceService = tokenIssuanceService;
        this.organizationMemberQueryApi = organizationMemberQueryApi;
    }

    @Override
    @Transactional
    public ApiResponse<AuthResponseDto> execute(AuthenticateCommand input) {
        AuthAccount authAccount = authAccountRepository.findByIdentifier(input.identifier())
                .orElseThrow(() -> new NotFoundException(AuthErrorCode.AUTH_ACCOUNT_NOT_FOUND, "Auth account not found"));

        if (authAccount.getStatus() == AuthStatus.LOCKED) {
            throw new BusinessRuleException(AuthErrorCode.ACCOUNT_LOCKED, "Account is locked");
        }
        if (authAccount.getStatus() == AuthStatus.SUSPENDED) {
            throw new BusinessRuleException(AuthErrorCode.ACCOUNT_SUSPENDED, "Account is suspended");
        }

        if (!passwordEncoderPort.matches(input.rawCredential(), authAccount.getCredentialHash())) {
            throw new BusinessRuleException(AuthErrorCode.INVALID_CREDENTIAL, "Invalid credentials");
        }
        
        if (authAccount.getStatus() == AuthStatus.REQUIRES_PASSWORD_CHANGE) {
            return new ApiResponse<>(true, "Password change required", AuthResponseDto.forPasswordChangeRequired(authAccount.getIdentifier()), null);
        }

        if (Boolean.TRUE.equals(authAccount.getTotpEnabled())) {
            TokenGeneratorPort.TokenData preAuth = tokenGeneratorPort.generatePreAuthToken(authAccount.getPrincipalId(), authAccount.getPrincipalType().name());
            preAuthTokenStorePort.store(preAuth.token(), authAccount.getId());
            return new ApiResponse<>(true, "2FA Required", AuthResponseDto.forTwoFactor(preAuth.token()), null);
        }

        String onboardingStatus = organizationMemberQueryApi.getOnboardingStatus(authAccount.getPrincipalId(), authAccount.getIdentifier());
        AuthResponseDto responseDto = AuthResponseDto.forSuccess(
                tokenIssuanceService.issueTokensAndCreateSession(authAccount, input.ipAddress(), input.userAgent()),
                onboardingStatus
        );
        return new ApiResponse<>(true, "Authentication successful", responseDto, null);
    }
}

