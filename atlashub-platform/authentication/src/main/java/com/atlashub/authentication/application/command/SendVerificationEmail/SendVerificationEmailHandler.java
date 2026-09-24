package com.atlashub.authentication.application.command.SendVerificationEmail;

import com.atlashub.authentication.application.port.OtpTransmissionPort;
import com.atlashub.authentication.domain.entities.AuthAccount;
import com.atlashub.authentication.domain.repositories.AuthAccountRepository;
import com.atlashub.authentication.domain.repositories.VerificationRepository;
import com.atlashub.authentication.domain.services.OtpVerificationIssuer;
import com.atlashub.authentication.domain.valueobject.VerificationType;
import com.atlashub.shared.application.usecase.Command;
import com.atlashub.shared.domain.exception.NotFoundException;
import com.atlashub.shared.domain.valueobject.CorrelationId;
import org.springframework.stereotype.Component;

@Component
public class SendVerificationEmailHandler extends Command<SendVerificationEmailCommand, Void> {

    private final AuthAccountRepository accountRepository;
    private final VerificationRepository verificationRepository;
    private final OtpVerificationIssuer issuer;
    private final OtpTransmissionPort transmissionPort;

    public SendVerificationEmailHandler(AuthAccountRepository accountRepository,
                                        VerificationRepository verificationRepository,
                                        OtpVerificationIssuer issuer,
                                        OtpTransmissionPort transmissionPort) {
        this.accountRepository = accountRepository;
        this.verificationRepository = verificationRepository;
        this.issuer = issuer;
        this.transmissionPort = transmissionPort;
    }

    @Override
    public Void execute(SendVerificationEmailCommand command) {
        AuthAccount account = accountRepository.findByAccountId(command.email())
                .orElseThrow(() -> new NotFoundException("Account not found"));

        OtpVerificationIssuer.IssuedToken issued = issuer.issue(
                verificationRepository.nextIdentity(), account.getAccountId(), VerificationType.email_verification);

        transmissionPort.storeForTransmission(CorrelationId.getOrCreate(), issued.rawOtp());
        verificationRepository.save(issued.token());

        return null;
    }
}
