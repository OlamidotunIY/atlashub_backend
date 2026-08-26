package com.atlashub.auth.application.usecase;

import com.atlashub.auth.application.command.EnableTotpCommand;
import com.atlashub.auth.application.dto.TotpSetupDto;
import com.atlashub.auth.application.port.out.TotpServicePort;
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
public class EnableTotpUseCase extends BaseUseCase<EnableTotpCommand, ApiResponse<TotpSetupDto>> {

    private final AuthAccountRepository authAccountRepository;
    private final TotpServicePort totpServicePort;
    private final DomainEventPublisher eventPublisher;

    public EnableTotpUseCase(
            AuthAccountRepository authAccountRepository,
            TotpServicePort totpServicePort,
            DomainEventPublisher eventPublisher) {
        this.authAccountRepository = authAccountRepository;
        this.totpServicePort = totpServicePort;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional
    public ApiResponse<TotpSetupDto> execute(EnableTotpCommand input) {
        AuthAccount authAccount = authAccountRepository.findById(input.authAccountId())
                .orElseThrow(() -> new NotFoundException(AuthErrorCode.AUTH_ACCOUNT_NOT_FOUND, "Auth account not found"));

        String secret = totpServicePort.generateSecret();
        authAccount.enableTotp(secret);

        authAccountRepository.save(authAccount);
        publishEvents(authAccount, eventPublisher);

        String accountName = authAccount.getPrincipalType().name() + "-" + authAccount.getPrincipalId();
        String qrCodeUri = totpServicePort.generateUri(secret, accountName);

        TotpSetupDto dto = new TotpSetupDto(secret, qrCodeUri);
        return new ApiResponse<>(true, "TOTP enabled successfully", dto, null);
    }
}
