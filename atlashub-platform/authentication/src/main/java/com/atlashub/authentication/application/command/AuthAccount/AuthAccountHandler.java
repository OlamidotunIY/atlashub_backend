package com.atlashub.authentication.application.command.AuthAccount;

import com.atlashub.authentication.application.port.OtpTransmissionPort;
import com.atlashub.authentication.domain.entities.AuthAccount;
import com.atlashub.authentication.domain.repositories.AuthAccountRepository;
import com.atlashub.authentication.domain.repositories.OtpVerificationRepository;
import com.atlashub.authentication.domain.services.OtpVerificationIssuer;
import com.atlashub.authentication.domain.valueobject.OtpType;
import com.atlashub.shared.application.usecase.Command;
import com.atlashub.shared.domain.valueobject.CorrelationId;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class AuthAccountHandler extends Command<AuthAccountCommand, Void> {

    final AuthAccountRepository accountRepository;
    final OtpVerificationIssuer issuer;
    final OtpVerificationRepository verificationRepository;
    final OtpTransmissionPort transmissionPort;

    public AuthAccountHandler(AuthAccountRepository accountRepository, OtpVerificationIssuer issuer, OtpVerificationRepository verificationRepository, OtpTransmissionPort transmissionPort) {
        this.accountRepository = accountRepository;
        this.issuer = issuer;
        this.verificationRepository = verificationRepository;
        this.transmissionPort = transmissionPort;
    }

    @Override
    public Void execute(AuthAccountCommand input) {
        Optional<AuthAccount> existingAccount = accountRepository.findByEmail(input.email());

        if (existingAccount.isPresent()) {
            return null;
        }

        AuthAccount account = AuthAccount.create(accountRepository.nextIdentity(), input.userId(), input.email(), input.passwordHash());
        OtpVerificationIssuer.IssuedToken token = issuer.issue(verificationRepository.nextIdentity(), account.getId(), OtpType.EMAIL_VERIFICATION);
        transmissionPort.storeForTransmission(CorrelationId.getOrCreate(), token.rawOtp());
        accountRepository.save(account);
        verificationRepository.save(token.token());

        return null;
    }
}
