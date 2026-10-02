package com.atlashub.authentication.application.command.AuthAccount;

import com.atlashub.authentication.application.port.OtpTransmissionPort;
import com.atlashub.authentication.domain.entities.AuthAccount;
import com.atlashub.authentication.domain.repositories.AuthAccountRepository;
import com.atlashub.authentication.domain.repositories.VerificationRepository;
import com.atlashub.authentication.domain.services.OtpVerificationIssuer;
import com.atlashub.authentication.domain.valueobject.VerificationType;
import com.atlashub.shared.application.usecase.Command;
import com.atlashub.shared.application.port.OneTimeSecretStore;
import com.atlashub.shared.application.port.PasswordEncoderPort;
import com.atlashub.shared.domain.valueobject.CorrelationId;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class AuthAccountHandler extends Command<AuthAccountCommand, Void> {

    final AuthAccountRepository accountRepository;
    final OtpVerificationIssuer issuer;
    final VerificationRepository verificationRepository;
    final OtpTransmissionPort transmissionPort;
    final OneTimeSecretStore oneTimeSecretStore;
    final PasswordEncoderPort passwordEncoderPort;

    public AuthAccountHandler(AuthAccountRepository accountRepository, OtpVerificationIssuer issuer, VerificationRepository verificationRepository, OtpTransmissionPort transmissionPort, OneTimeSecretStore oneTimeSecretStore, PasswordEncoderPort passwordEncoderPort) {
        this.accountRepository = accountRepository;
        this.issuer = issuer;
        this.verificationRepository = verificationRepository;
        this.transmissionPort = transmissionPort;
        this.oneTimeSecretStore = oneTimeSecretStore;
        this.passwordEncoderPort = passwordEncoderPort;
    }

    @Override
    public Void execute(AuthAccountCommand input) {
        Optional<AuthAccount> existingAccount = accountRepository.findByAccountId(input.email());

        if (existingAccount.isPresent()) {
            return null;
        }

        String rawPassword = oneTimeSecretStore.claim(input.credentialReference()).orElseThrow(() -> new IllegalStateException("Registration credential is unavailable or already claimed"));
        String passwordHash = passwordEncoderPort.encode(rawPassword);

        AuthAccount account = AuthAccount.createCredentialsAccount(accountRepository.nextIdentity(), input.userId(), input.email(), passwordHash);

        OtpVerificationIssuer.IssuedToken token = issuer.issue(verificationRepository.nextIdentity(), account.getAccountId(), VerificationType.email_verification);

        transmissionPort.storeForTransmission(CorrelationId.getOrCreate(), token.rawOtp());
        accountRepository.save(account);
        verificationRepository.save(token.token());

        return null;
    }
}
