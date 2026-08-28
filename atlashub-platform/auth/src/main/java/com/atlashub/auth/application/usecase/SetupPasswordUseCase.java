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
import com.atlashub.shared.dto.ApiResponse;
import com.atlashub.shared.exception.BusinessRuleException;
import com.atlashub.shared.exception.NotFoundException;
import com.atlashub.shared.usecase.BaseUseCase;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SetupPasswordUseCase extends BaseUseCase<SetupPasswordCommand, ApiResponse<AuthResponseDto>> {

    private final AuthAccountRepository authAccountRepository;
    private final PasswordEncoderPort passwordEncoderPort;
    private final SetupTokenStorePort setupTokenStorePort;
    private final TokenIssuanceService tokenIssuanceService;

    public SetupPasswordUseCase(
            AuthAccountRepository authAccountRepository,
            PasswordEncoderPort passwordEncoderPort,
            SetupTokenStorePort setupTokenStorePort,
            TokenIssuanceService tokenIssuanceService) {
        this.authAccountRepository = authAccountRepository;
        this.passwordEncoderPort = passwordEncoderPort;
        this.setupTokenStorePort = setupTokenStorePort;
        this.tokenIssuanceService = tokenIssuanceService;
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

        AuthResponseDto responseDto = AuthResponseDto.forSuccess(tokenIssuanceService.issueTokensAndCreateSession(authAccount, input.ipAddress(), input.userAgent()));
        return new ApiResponse<>(true, "Password setup successfully", responseDto, null);
    }
}

