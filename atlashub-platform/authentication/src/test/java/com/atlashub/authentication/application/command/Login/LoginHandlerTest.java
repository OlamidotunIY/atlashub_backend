package com.atlashub.authentication.application.command.Login;

import com.atlashub.authentication.application.command.SendVerificationEmail.SendVerificationEmailHandler;
import com.atlashub.authentication.application.port.OtpTransmissionPort;
import com.atlashub.authentication.application.port.TokenPort;
import com.atlashub.authentication.domain.entities.AuthAccount;
import com.atlashub.authentication.domain.exceptions.EmailVerificationRequired;
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

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class LoginHandlerTest {

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
