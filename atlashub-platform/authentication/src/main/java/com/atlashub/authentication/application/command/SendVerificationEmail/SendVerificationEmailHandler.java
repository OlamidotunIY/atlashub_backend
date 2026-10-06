package com.atlashub.authentication.application.command.SendVerificationEmail;

import com.atlashub.authentication.application.port.OtpTransmissionPort;
import com.atlashub.authentication.domain.entities.AuthAccount;
import com.atlashub.authentication.domain.repositories.AuthAccountRepository;
import com.atlashub.authentication.domain.repositories.VerificationRepository;
import com.atlashub.authentication.domain.services.OtpVerificationIssuer;
import com.atlashub.authentication.domain.valueobject.VerificationType;
import com.atlashub.shared.application.port.UserQueryPort;
import com.atlashub.shared.application.usecase.Command;
import com.atlashub.shared.domain.valueobject.CorrelationId;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Component
public class SendVerificationEmailHandler extends Command<SendVerificationEmailCommand, Void> {

    private final AuthAccountRepository accountRepository;
    private final VerificationRepository verificationRepository;
    private final OtpVerificationIssuer issuer;
    private final OtpTransmissionPort transmissionPort;
    private final UserQueryPort userQueryPort;

    public SendVerificationEmailHandler(AuthAccountRepository accountRepository,
                                        VerificationRepository verificationRepository,
                                        OtpVerificationIssuer issuer,
                                        OtpTransmissionPort transmissionPort,
                                        UserQueryPort userQueryPort) {
        this.accountRepository = accountRepository;
        this.verificationRepository = verificationRepository;
        this.issuer = issuer;
        this.transmissionPort = transmissionPort;
        this.userQueryPort = userQueryPort;
    }

    @Override
    @Transactional
    public Void execute(SendVerificationEmailCommand command) {
        Optional<AuthAccount> accountResult = accountRepository.findByAccountId(command.email());
        if (accountResult.isEmpty()) return null;
        AuthAccount account = accountResult.get();
        if (userQueryPort.findById(account.getUserId())
                .map(UserQueryPort.UserDto::emailVerified).orElse(false)) return null;

        OtpVerificationIssuer.IssuedToken issued = issuer.issue(
                verificationRepository.nextIdentity(), account.getAccountId(), VerificationType.email_verification);

        transmissionPort.storeForTransmission(CorrelationId.getOrCreate(), issued.rawOtp());
        verificationRepository.save(issued.token());

        return null;
    }
}
