package com.atlashub.authentication.application.command.Login;

import com.atlashub.authentication.application.command.SendVerificationEmail.SendVerificationEmailHandler;
import com.atlashub.authentication.application.port.OtpTransmissionPort;
import com.atlashub.authentication.application.port.TokenPort;
import com.atlashub.authentication.domain.entities.AuthAccount;
import com.atlashub.authentication.domain.exceptions.EmailVerificationRequired;
import com.atlashub.authentication.domain.valueobject.LoginStatus;
import com.atlashub.authentication.domain.repositories.AuthAccountRepository;
import com.atlashub.authentication.domain.repositories.SessionRepository;
import com.atlashub.authentication.domain.repositories.TrustedDeviceRepository;
import com.atlashub.authentication.domain.repositories.VerificationRepository;
import com.atlashub.authentication.domain.services.OtpVerificationIssuer;
import com.atlashub.shared.application.port.MembershipQueryPort;
import com.atlashub.shared.application.port.PasswordEncoderPort;
import com.atlashub.shared.application.port.UserQueryPort;
import com.atlashub.shared.application.security.ApiEnvironment;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.Set;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class LoginHandlerTest {

    @Test
    void verified_user_can_log_in_before_joining_an_organization() {
        AuthAccountRepository accounts = mock(AuthAccountRepository.class);
        UserQueryPort users = mock(UserQueryPort.class);
        PasswordEncoderPort passwords = mock(PasswordEncoderPort.class);
        TrustedDeviceRepository devices = mock(TrustedDeviceRepository.class);
        SessionRepository sessions = mock(SessionRepository.class);
        TokenPort tokens = mock(TokenPort.class);
        MembershipQueryPort memberships = mock(MembershipQueryPort.class);
        AuthAccount account = AuthAccount.createCredentialsAccount(1L, 2L, "tolu@example.com", "hash");
        when(accounts.findByAccountId("tolu@example.com")).thenReturn(Optional.of(account));
        when(users.findById(2L)).thenReturn(Optional.of(new UserQueryPort.UserDto(
                2L, "Tolu", "Ade", "tolu@example.com", "NG", null, ApiEnvironment.TEST, true)));
        when(passwords.matches("Strong1!", "hash")).thenReturn(true);
        when(devices.findByUserIdAndDeviceFingerprint(2L, "device")).thenReturn(Optional.empty());
        when(devices.nextIdentity()).thenReturn(3L);
        when(sessions.nextIdentity()).thenReturn(4L);
        when(tokens.generateAccessToken(any())).thenReturn(
                new TokenPort.AccessTokenResult("access", "jti", ZonedDateTime.now().plusMinutes(15)));

        LoginHandler handler = new LoginHandler(passwords, accounts, devices,
                mock(VerificationRepository.class), mock(OtpVerificationIssuer.class),
                mock(OtpTransmissionPort.class), memberships, users, sessions, tokens,
                mock(SendVerificationEmailHandler.class));
        LoginResponse response = handler.execute(new LoginCommand(
                "tolu@example.com", "Strong1!", "device", "ip", "agent"));

        assertEquals(LoginStatus.SUCCESS, response.status());
        org.mockito.ArgumentCaptor<TokenPort.AccessTokenPayload> payload =
                org.mockito.ArgumentCaptor.forClass(TokenPort.AccessTokenPayload.class);
        verify(tokens).generateAccessToken(payload.capture());
        assertNull(payload.getValue().orgId());
        assertEquals(Set.of(), payload.getValue().permissions());
        org.mockito.Mockito.verifyNoInteractions(memberships);
    }

    @Test
    void resends_email_verification_when_an_unverified_user_attempts_login() {
        AuthAccountRepository accounts = mock(AuthAccountRepository.class);
        UserQueryPort users = mock(UserQueryPort.class);
        SendVerificationEmailHandler resend = mock(SendVerificationEmailHandler.class);
        AuthAccount account = AuthAccount.createCredentialsAccount(1L, 2L, "tolu@example.com", "hash");
        when(accounts.findByAccountId("tolu@example.com")).thenReturn(Optional.of(account));
        when(users.findById(2L)).thenReturn(Optional.of(new UserQueryPort.UserDto(
                2L, "Tolu", "Ade", "tolu@example.com", "NG", 3L, ApiEnvironment.TEST, false)));

        LoginHandler handler = new LoginHandler(
                mock(PasswordEncoderPort.class), accounts, mock(TrustedDeviceRepository.class),
                mock(VerificationRepository.class), mock(OtpVerificationIssuer.class), mock(OtpTransmissionPort.class),
                mock(MembershipQueryPort.class), users, mock(SessionRepository.class), mock(TokenPort.class), resend);

        assertThrows(EmailVerificationRequired.class,
                () -> handler.execute(new LoginCommand("tolu@example.com", "password", "device", "ip", "agent")));

        verify(resend).execute(any());
    }
}
