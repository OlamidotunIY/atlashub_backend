package com.atlashub.authentication.application.command.AuthorizeDevice;

import com.atlashub.authentication.domain.entities.AuthAccount;
import com.atlashub.authentication.domain.entities.Verification;
import com.atlashub.authentication.domain.repositories.AuthAccountRepository;
import com.atlashub.authentication.domain.repositories.TrustedDeviceRepository;
import com.atlashub.authentication.domain.repositories.VerificationRepository;
import com.atlashub.authentication.domain.valueobject.VerificationStatus;
import com.atlashub.authentication.domain.valueobject.VerificationType;
import com.atlashub.shared.application.port.PasswordEncoderPort;
import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuthorizeDeviceHandlerTest {

    @Test
    void verifies_the_challenge_and_trusts_the_device() {
        AuthAccountRepository accounts = mock(AuthAccountRepository.class);
        VerificationRepository verifications = mock(VerificationRepository.class);
        TrustedDeviceRepository devices = mock(TrustedDeviceRepository.class);
        PasswordEncoderPort passwords = mock(PasswordEncoderPort.class);
        AuthAccount account = AuthAccount.createCredentialsAccount(
                1L, 2L, "tolu@example.com", "bcrypt-hash");
        Verification verification = new Verification(
                3L, "tolu@example.com", "otp-hash", VerificationType.email_otp,
                VerificationStatus.pending, ZonedDateTime.now().plusMinutes(10),
                0, 5, ZonedDateTime.now(), ZonedDateTime.now());
        when(accounts.findByAccountId("tolu@example.com")).thenReturn(Optional.of(account));
        when(verifications.findByIdentifierAndTypeAndStatus(
                "tolu@example.com", VerificationType.email_otp, VerificationStatus.pending))
                .thenReturn(Optional.of(verification));
        when(passwords.matches("123456", "otp-hash")).thenReturn(true);
        when(devices.findByUserIdAndDeviceFingerprint(2L, "fingerprint"))
                .thenReturn(Optional.empty());
        when(devices.nextIdentity()).thenReturn(4L);

        new AuthorizeDeviceHandler(accounts, verifications, devices, passwords).execute(
                new AuthorizeDeviceCommand(
                        "tolu@example.com", "123456", "fingerprint", "Chrome", "127.0.0.1"));

        verify(verifications).save(verification);
        verify(devices).save(any());
    }
}
