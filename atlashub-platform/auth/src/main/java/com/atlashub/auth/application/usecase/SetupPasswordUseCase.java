package com.atlashub.auth.application.usecase;

import com.atlashub.auth.application.command.SetupPasswordCommand;
import com.atlashub.auth.application.port.in.PasswordEncoderPort;
import com.atlashub.auth.application.port.in.SetupTokenStorePort;
import com.atlashub.auth.application.result.AuthResponseDto;
import com.atlashub.auth.application.service.TokenIssuanceService;
import com.atlashub.auth.domain.exception.AuthErrorCode;
import com.atlashub.auth.domain.model.AuthAccount;
import com.atlashub.auth.domain.repository.AuthAccountRepository;
import com.atlashub.auth.domain.valueobject.AuthStatus;
import com.atlashub.shared.application.dto.ApiResponse;
import com.atlashub.shared.domain.exception.BusinessRuleException;
import com.atlashub.shared.domain.exception.NotFoundException;
import com.atlashub.shared.application.usecase.BaseUseCase;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.atlashub.shared.application.api.OrganizationMemberQueryApi;

@Service
public class SetupPasswordUseCase extends BaseUseCase<SetupPasswordCommand, ApiResponse<AuthResponseDto>> {

    private final AuthAccountRepository authAccountRepository;
    private final PasswordEncoderPort passwordEncoderPort;
    private final SetupTokenStorePort setupTokenStorePort;
    private final TokenIssuanceService tokenIssuanceService;
    private final OrganizationMemberQueryApi organizationMemberQueryApi;

    public SetupPasswordUseCase(
            AuthAccountRepository authAccountRepository,
            PasswordEncoderPort passwordEncoderPort,
            SetupTokenStorePort setupTokenStorePort,
            TokenIssuanceService tokenIssuanceService,
            OrganizationMemberQueryApi organizationMemberQueryApi) {
        this.authAccountRepository = authAccountRepository;
        this.passwordEncoderPort = passwordEncoderPort;
        this.setupTokenStorePort = setupTokenStorePort;
        this.tokenIssuanceService = tokenIssuanceService;
        this.organizationMemberQueryApi = organizationMemberQueryApi;
    }

    @Override
    @Transactional
    public ApiResponse<AuthResponseDto> execute(SetupPasswordCommand input) {
        Long authAccountId = setupTokenStorePort.consume(input.setupToken())
                .orElseThrow(() -> new BusinessRuleException(AuthErrorCode.INVALID_REQUEST, "Invalid or expired setup token"));

        AuthAccount authAccount = authAccountRepository.findById(authAccountId)
                .orElseThrow(() -> new NotFoundException(AuthErrorCode.AUTH_ACCOUNT_NOT_FOUND, "Auth account not found"));

        if (authAccount.getStatus() != AuthStatus.REQUIRES_PASSWORD_SETUP) {
            throw new BusinessRuleException(AuthErrorCode.INVALID_REQUEST, "Password setup not required for this account");
        }

        // Hash new password and update account
        String newHash = passwordEncoderPort.encode(input.newPassword());
        authAccount.updateCredential(newHash);
        authAccountRepository.save(authAccount);

        String onboardingStatus = organizationMemberQueryApi.getOnboardingStatus(authAccount.getPrincipalId(), authAccount.getIdentifier());
        AuthResponseDto responseDto = AuthResponseDto.forSuccess(
                tokenIssuanceService.issueTokensAndCreateSession(authAccount, input.ipAddress(), input.userAgent()),
                onboardingStatus
        );
        return new ApiResponse<>(true, "Password setup successfully", responseDto, null);
    }
}

