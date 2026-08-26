package com.atlashub.auth.application.usecase;

import com.atlashub.auth.application.command.UpdateCredentialCommand;
import com.atlashub.auth.application.port.PasswordEncoderPort;
import com.atlashub.auth.domain.exception.AuthErrorCode;
import com.atlashub.auth.domain.model.AuthAccount;
import com.atlashub.auth.domain.repository.AuthAccountRepository;
import com.atlashub.shared.dto.ApiResponse;
import com.atlashub.shared.event.DomainEventPublisher;
import com.atlashub.shared.exception.NotFoundException;
import com.atlashub.shared.usecase.BaseUseCase;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UpdateCredentialUseCase extends BaseUseCase<UpdateCredentialCommand, ApiResponse<Void>> {

    private final AuthAccountRepository authAccountRepository;
    private final PasswordEncoderPort passwordEncoderPort;
    private final DomainEventPublisher eventPublisher;

    public UpdateCredentialUseCase(
            AuthAccountRepository authAccountRepository,
            PasswordEncoderPort passwordEncoderPort,
            DomainEventPublisher eventPublisher) {
        this.authAccountRepository = authAccountRepository;
        this.passwordEncoderPort = passwordEncoderPort;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional
    public ApiResponse<Void> execute(UpdateCredentialCommand input) {
        AuthAccount authAccount = authAccountRepository.findById(input.authAccountId())
                .orElseThrow(() -> new NotFoundException(AuthErrorCode.AUTH_ACCOUNT_NOT_FOUND, "Auth account not found"));

        String hash = passwordEncoderPort.encode(input.rawNewCredential());
        authAccount.updateCredential(hash);

        authAccountRepository.save(authAccount);
        publishEvents(authAccount, eventPublisher);

        return new ApiResponse<>(true, "Credential updated successfully", null, null);
    }
}
