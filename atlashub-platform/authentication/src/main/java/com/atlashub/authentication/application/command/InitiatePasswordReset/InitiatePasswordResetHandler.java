package com.atlashub.authentication.application.command.InitiatePasswordReset;

import com.atlashub.authentication.application.port.OtpTransmissionPort;
import com.atlashub.authentication.domain.entities.AuthAccount;
import com.atlashub.authentication.domain.repositories.AuthAccountRepository;
import com.atlashub.authentication.domain.repositories.VerificationRepository;
import com.atlashub.authentication.domain.services.OtpVerificationIssuer;
import com.atlashub.authentication.domain.valueobject.VerificationType;
import com.atlashub.shared.application.usecase.Command;
import com.atlashub.shared.domain.valueobject.CorrelationId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class InitiatePasswordResetHandler extends Command<InitiatePasswordResetCommand, Void> {

    private static final Logger log = LoggerFactory.getLogger(InitiatePasswordResetHandler.class);

    private final AuthAccountRepository accountRepository;
    private final VerificationRepository verificationRepository;
    private final OtpVerificationIssuer issuer;
    private final OtpTransmissionPort transmissionPort;

    public InitiatePasswordResetHandler(AuthAccountRepository accountRepository,
                                        VerificationRepository verificationRepository,
                                        OtpVerificationIssuer issuer,
                                        OtpTransmissionPort transmissionPort) {
        this.accountRepository = accountRepository;
        this.verificationRepository = verificationRepository;
        this.issuer = issuer;
        this.transmissionPort = transmissionPort;
    }

    @Override
    public Void execute(InitiatePasswordResetCommand command) {
        Optional<AuthAccount> accountOpt = accountRepository.findByAccountId(command.email());
        if (accountOpt.isEmpty()) {
            log.debug("Password reset requested for unknown email — silently succeeded.");
            return null;
        }

        AuthAccount account = accountOpt.get();
        OtpVerificationIssuer.IssuedToken issued = issuer.issue(
                verificationRepository.nextIdentity(), account.getAccountId(), VerificationType.password_reset);

        transmissionPort.storeForTransmission(CorrelationId.getOrCreate(), issued.rawOtp());
        verificationRepository.save(issued.token());

        return null;
    }
}
