package com.atlashub.authentication.application.command.ResetPassword;

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
public class ResetPasswordHandler extends Command<ResetPasswordCommand, Void> {

    private final AuthAccountRepository accountRepository;
    private final VerificationRepository verificationRepository;
    private final PasswordEncoderPort passwordEncoderPort;

    public ResetPasswordHandler(AuthAccountRepository accountRepository,
                                VerificationRepository verificationRepository,
                                PasswordEncoderPort passwordEncoderPort) {
        this.accountRepository = accountRepository;
        this.verificationRepository = verificationRepository;
        this.passwordEncoderPort = passwordEncoderPort;
    }

    @Override
    public Void execute(ResetPasswordCommand command) {
        AuthAccount account = accountRepository.findByAccountId(command.email())
                .orElseThrow(() -> new NotFoundException("Account not found"));

        Verification verification = verificationRepository
                .findByIdentifierAndTypeAndStatus(
                        account.getAccountId(), VerificationType.password_reset, VerificationStatus.pending)
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

        String newHash = passwordEncoderPort.encode(command.newPassword());
        account.updatePassword(newHash);
        accountRepository.save(account);

        return null;
    }
}
