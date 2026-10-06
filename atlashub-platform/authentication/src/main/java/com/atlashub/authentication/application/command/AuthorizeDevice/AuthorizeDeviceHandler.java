package com.atlashub.authentication.application.command.AuthorizeDevice;

import com.atlashub.authentication.domain.entities.AuthAccount;
import com.atlashub.authentication.domain.entities.TrustedDevice;
import com.atlashub.authentication.domain.entities.Verification;
import com.atlashub.authentication.domain.exceptions.InvalidCredentials;
import com.atlashub.authentication.domain.exceptions.VerifyTokenError;
import com.atlashub.authentication.domain.repositories.AuthAccountRepository;
import com.atlashub.authentication.domain.repositories.TrustedDeviceRepository;
import com.atlashub.authentication.domain.repositories.VerificationRepository;
import com.atlashub.authentication.domain.valueobject.VerificationStatus;
import com.atlashub.authentication.domain.valueobject.VerificationType;
import com.atlashub.shared.application.port.PasswordEncoderPort;
import com.atlashub.shared.application.usecase.Command;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;

@Component
public class AuthorizeDeviceHandler extends Command<AuthorizeDeviceCommand, Void> {
    private final AuthAccountRepository accountRepository;
    private final VerificationRepository verificationRepository;
    private final TrustedDeviceRepository deviceRepository;
    private final PasswordEncoderPort passwordEncoderPort;

    public AuthorizeDeviceHandler(AuthAccountRepository accountRepository,
                                  VerificationRepository verificationRepository,
                                  TrustedDeviceRepository deviceRepository,
                                  PasswordEncoderPort passwordEncoderPort) {
        this.accountRepository = accountRepository;
        this.verificationRepository = verificationRepository;
        this.deviceRepository = deviceRepository;
        this.passwordEncoderPort = passwordEncoderPort;
    }

    @Override
    @Transactional
    public Void execute(AuthorizeDeviceCommand command) {
        AuthAccount account = accountRepository.findByAccountId(command.email())
                .orElseThrow(InvalidCredentials::new);
        Verification verification = verificationRepository.findByIdentifierAndTypeAndStatus(
                        account.getAccountId(), VerificationType.email_otp, VerificationStatus.pending)
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
        TrustedDevice device = deviceRepository.findByUserIdAndDeviceFingerprint(
                        account.getUserId(), command.deviceFingerprint())
                .map(existing -> {
                    existing.renew(command.deviceName(), command.ipAddress());
                    return existing;
                })
                .orElseGet(() -> TrustedDevice.create(
                        deviceRepository.nextIdentity(), account.getUserId(), command.deviceFingerprint(),
                        command.deviceName(), command.ipAddress()));
        deviceRepository.save(device);
        return null;
    }
}
