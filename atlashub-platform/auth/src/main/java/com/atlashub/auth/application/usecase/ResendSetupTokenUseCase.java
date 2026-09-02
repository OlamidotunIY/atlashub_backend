package com.atlashub.auth.application.usecase;

import com.atlashub.auth.application.command.ResendSetupTokenCommand;
import com.atlashub.auth.application.port.in.SetupTokenStorePort;
import com.atlashub.auth.application.port.in.TokenGeneratorPort;
import com.atlashub.auth.domain.exception.AuthErrorCode;
import com.atlashub.auth.domain.model.AuthAccount;
import com.atlashub.auth.domain.valueobject.AuthStatus;
import com.atlashub.auth.domain.repository.AuthAccountRepository;
import com.atlashub.shared.application.dto.ApiResponse;
import com.atlashub.shared.application.port.out.DomainEventPublisher;
import com.atlashub.shared.domain.exception.BusinessRuleException;
import com.atlashub.shared.domain.exception.NotFoundException;
import com.atlashub.shared.application.usecase.BaseUseCase;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.util.UUID;

@Service
public class ResendSetupTokenUseCase extends BaseUseCase<ResendSetupTokenCommand, ApiResponse<Void>> {

    private final AuthAccountRepository authAccountRepository;
    private final SetupTokenStorePort setupTokenStorePort;
    private final TokenGeneratorPort tokenGeneratorPort;
    private final DomainEventPublisher eventPublisher;

    public ResendSetupTokenUseCase(
            AuthAccountRepository authAccountRepository,
            SetupTokenStorePort setupTokenStorePort,
            TokenGeneratorPort tokenGeneratorPort,
            DomainEventPublisher eventPublisher) {
        this.authAccountRepository = authAccountRepository;
        this.setupTokenStorePort = setupTokenStorePort;
        this.tokenGeneratorPort = tokenGeneratorPort;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public ApiResponse<Void> execute(ResendSetupTokenCommand input) {
        AuthAccount authAccount = authAccountRepository.findByIdentifier(input.identifier())
                .orElseThrow(() -> new NotFoundException(AuthErrorCode.AUTH_ACCOUNT_NOT_FOUND, "Auth account not found"));

        if (authAccount.getStatus() != AuthStatus.REQUIRES_PASSWORD_SETUP) {
            throw new BusinessRuleException(AuthErrorCode.INVALID_REQUEST, "Account is not in password setup phase");
        }

        TokenGeneratorPort.TokenData setupToken = tokenGeneratorPort.generateSetupToken(authAccount.getPrincipalId(), authAccount.getPrincipalType().name());
        setupTokenStorePort.store(setupToken.token(), authAccount.getId());

        authAccount.requirePasswordSetup(setupToken.token());
        authAccountRepository.save(authAccount);
        publishEvents(authAccount, eventPublisher);

        return new ApiResponse<>(true, "Password setup token resent successfully", null, null);
    }
}
