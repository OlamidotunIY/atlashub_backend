package com.atlashub.auth.application.usecase;

import com.atlashub.auth.application.command.ChangeTemporaryPasswordCommand;
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
public class ChangeTemporaryPasswordUseCase extends BaseUseCase<ChangeTemporaryPasswordCommand, ApiResponse<AuthResponseDto>> {

    private final AuthAccountRepository authAccountRepository;
    private final PasswordEncoderPort passwordEncoderPort;
    private final TokenGeneratorPort tokenGeneratorPort;
    private final PreAuthTokenStorePort preAuthTokenStorePort;
    private final TokenIssuanceService tokenIssuanceService;
    private final OrganizationMemberQueryApi organizationMemberQueryApi;

    public ChangeTemporaryPasswordUseCase(
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
    public ApiResponse<AuthResponseDto> execute(ChangeTemporaryPasswordCommand input) {
        AuthAccount authAccount = authAccountRepository.findByIdentifier(input.identifier())
                .orElseThrow(() -> new NotFoundException(AuthErrorCode.AUTH_ACCOUNT_NOT_FOUND, "Auth account not found"));

        if (authAccount.getStatus() != AuthStatus.REQUIRES_PASSWORD_CHANGE) {
            throw new BusinessRuleException(AuthErrorCode.INVALID_REQUEST, "Password change not required for this account");
        }

        if (!passwordEncoderPort.matches(input.oldPassword(), authAccount.getCredentialHash())) {
            throw new BusinessRuleException(AuthErrorCode.INVALID_CREDENTIAL, "Invalid credentials");
        }

        // Hash new password and update account
        String newHash = passwordEncoderPort.encode(input.newPassword());
        authAccount.updateCredential(newHash);
        authAccountRepository.save(authAccount);

        // Standard 2FA check after successful password change
        if (Boolean.TRUE.equals(authAccount.getTotpEnabled())) {
            TokenGeneratorPort.TokenData preAuth = tokenGeneratorPort.generatePreAuthToken(authAccount.getPrincipalId(), authAccount.getPrincipalType().name());
            preAuthTokenStorePort.store(preAuth.token(), authAccount.getId());
            return new ApiResponse<>(true, "Password changed. 2FA Required", AuthResponseDto.forTwoFactor(preAuth.token()), null);
        }

        // Issue tokens immediately if no 2FA required
        String onboardingStatus = organizationMemberQueryApi.getOnboardingStatus(authAccount.getPrincipalId(), authAccount.getIdentifier());
        AuthResponseDto responseDto = AuthResponseDto.forSuccess(tokenIssuanceService.issueTokensAndCreateSession(authAccount, input.ipAddress(), input.userAgent()), onboardingStatus);
        return new ApiResponse<>(true, "Password changed successfully", responseDto, null);
    }
}

