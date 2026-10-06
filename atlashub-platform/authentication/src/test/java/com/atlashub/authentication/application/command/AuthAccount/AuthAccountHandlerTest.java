package com.atlashub.authentication.application.command.AuthAccount;

import com.atlashub.authentication.application.port.OtpTransmissionPort;
import com.atlashub.authentication.domain.entities.AuthAccount;
import com.atlashub.authentication.domain.events.AuthEmailVerifiedEvent;
import com.atlashub.authentication.domain.repositories.AuthAccountRepository;
import com.atlashub.authentication.domain.repositories.VerificationRepository;
import com.atlashub.authentication.domain.services.OtpVerificationIssuer;
import com.atlashub.shared.application.port.OneTimeSecretStore;
import com.atlashub.shared.application.port.PasswordEncoderPort;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuthAccountHandlerTest {

    @Test
    void invited_users_skip_otp_and_are_marked_verified() {
        AuthAccountRepository accounts = mock(AuthAccountRepository.class);
        OtpVerificationIssuer issuer = mock(OtpVerificationIssuer.class);
        VerificationRepository verifications = mock(VerificationRepository.class);
        OtpTransmissionPort transmission = mock(OtpTransmissionPort.class);
        OneTimeSecretStore secrets = mock(OneTimeSecretStore.class);
        PasswordEncoderPort passwords = mock(PasswordEncoderPort.class);
        when(accounts.findByAccountId("tolu@example.com")).thenReturn(Optional.empty());
        when(accounts.nextIdentity()).thenReturn(1L);
        when(secrets.claim("credential-reference")).thenReturn(Optional.of("Strong1!"));
        when(passwords.encode("Strong1!")).thenReturn("bcrypt-hash");

        AuthAccountHandler handler = new AuthAccountHandler(
                accounts, issuer, verifications, transmission, secrets, passwords);
        handler.execute(new AuthAccountCommand(
                2L, "tolu@example.com", "credential-reference", true));

        ArgumentCaptor<AuthAccount> saved = ArgumentCaptor.forClass(AuthAccount.class);
        verify(accounts).save(saved.capture());
        assertInstanceOf(AuthEmailVerifiedEvent.class, saved.getValue().pullDomainEvents().getFirst());
        verify(issuer, never()).issue(org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
    }
}
