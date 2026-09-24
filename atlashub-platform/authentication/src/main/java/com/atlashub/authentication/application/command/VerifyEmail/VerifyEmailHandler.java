package com.atlashub.authentication.application.command.VerifyEmail;

import com.atlashub.authentication.domain.entities.AuthAccount;
import com.atlashub.authentication.domain.entities.Verification;
import com.atlashub.authentication.domain.exceptions.InvalidCredentials;
import com.atlashub.authentication.domain.exceptions.VerifyTokenError;
import com.atlashub.authentication.domain.repositories.AuthAccountRepository;
import com.atlashub.authentication.domain.repositories.VerificationRepository;
import com.atlashub.authentication.domain.valueobject.VerificationStatus;
import com.atlashub.authentication.domain.valueobject.VerificationType;
import com.atlashub.shared.application.port.PasswordEncoderPort;
import com.atlashub.shared.application.usecase.Command;
import com.atlashub.shared.domain.exception.NotFoundException;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;

@Component
public class VerifyEmailHandler extends Command<VerifyEmailCommand, Void> {

    private final AuthAccountRepository accountRepository;
    private final VerificationRepository verificationRepository;
    private final PasswordEncoderPort passwordEncoderPort;

    public VerifyEmailHandler(AuthAccountRepository accountRepository,
                              VerificationRepository verificationRepository,
                              PasswordEncoderPort passwordEncoderPort) {
        this.accountRepository = accountRepository;
        this.verificationRepository = verificationRepository;
        this.passwordEncoderPort = passwordEncoderPort;
    }

    @Override
    public Void execute(VerifyEmailCommand command) {
        AuthAccount account = accountRepository.findByAccountId(command.email())
                .orElseThrow(() -> new NotFoundException("Account not found"));

        Verification verification = verificationRepository
                .findByIdentifierAndTypeAndStatus(
                        account.getAccountId(), VerificationType.email_verification, VerificationStatus.pending)
                .orElseThrow(VerifyTokenError::new);

        if (ZonedDateTime.now().isAfter(verification.getExpiresAt())) {
            throw new VerifyTokenError();
        }

        if (!passwordEncoderPort.matches(command.rawOtp(), verification.getValueHash())) {
            verification.recordFailedAttempt();
            verificationRepository.save(verification);
            throw new InvalidCredentials();
        }

        verification.verify();
        verificationRepository.save(verification);

        // Fire domain event — accounts module User will set emailVerified = true
        account.recordEmailVerified();
        accountRepository.save(account);

        return null;
    }
}
