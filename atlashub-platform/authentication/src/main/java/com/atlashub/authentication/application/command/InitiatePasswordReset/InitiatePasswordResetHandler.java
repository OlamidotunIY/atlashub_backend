package com.atlashub.authentication.application.command.InitiatePasswordReset;

import com.atlashub.authentication.application.port.OtpTransmissionPort;
import com.atlashub.authentication.domain.entities.AuthAccount;
import com.atlashub.authentication.domain.repositories.AuthAccountRepository;
import com.atlashub.authentication.domain.repositories.OtpVerificationRepository;
import com.atlashub.authentication.domain.services.OtpVerificationIssuer;
import com.atlashub.authentication.domain.valueobject.OtpType;
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
    private final OtpVerificationRepository otpRepository;
    private final OtpVerificationIssuer issuer;
    private final OtpTransmissionPort transmissionPort;

    public InitiatePasswordResetHandler(AuthAccountRepository accountRepository,
                                        OtpVerificationRepository otpRepository,
                                        OtpVerificationIssuer issuer,
                                        OtpTransmissionPort transmissionPort) {
        this.accountRepository = accountRepository;
        this.otpRepository = otpRepository;
        this.issuer = issuer;
        this.transmissionPort = transmissionPort;
    }

    @Override
    public Void execute(InitiatePasswordResetCommand command) {
        // Silently succeed if email not found — prevents account enumeration
        Optional<AuthAccount> accountOpt = accountRepository.findByEmail(command.email());
        if (accountOpt.isEmpty()) {
            log.debug("Password reset requested for unknown email — silently succeeded.");
            return null;
        }

        AuthAccount account = accountOpt.get();
        OtpVerificationIssuer.IssuedToken issued = issuer.issue(
                otpRepository.nextIdentity(), account.getId(), OtpType.PASSWORD_RESET);

        transmissionPort.storeForTransmission(CorrelationId.getOrCreate(), issued.rawOtp());
        otpRepository.save(issued.token());

        return null;
    }
}
