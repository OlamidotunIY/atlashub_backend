package com.atlashub.authentication.application.command.IssueAuthorizationCode;

import com.atlashub.authentication.domain.entities.AuthorizationCode;
import com.atlashub.authentication.domain.entities.OAuthClient;
import com.atlashub.authentication.domain.exceptions.EmailVerificationRequired;
import com.atlashub.authentication.domain.exceptions.InvalidClientException;
import com.atlashub.authentication.domain.exceptions.InvalidPkceChallengeException;
import com.atlashub.authentication.domain.exceptions.InvalidRedirectUriException;
import com.atlashub.authentication.domain.exceptions.OAuthException;
import com.atlashub.authentication.domain.repositories.AuthorizationCodeRepository;
import com.atlashub.authentication.domain.repositories.OAuthClientRepository;
import com.atlashub.authentication.domain.valueobject.OAuthClientType;
import com.atlashub.shared.application.port.UserQueryPort;
import com.atlashub.shared.application.security.ApiEnvironment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class IssueAuthorizationCodeHandlerTest {

    private OAuthClientRepository oauthClientRepository;
    private AuthorizationCodeRepository authorizationCodeRepository;
    private UserQueryPort userQueryPort;
    private IssueAuthorizationCodeHandler handler;

    @BeforeEach
    void setUp() {
        oauthClientRepository = mock(OAuthClientRepository.class);
        authorizationCodeRepository = mock(AuthorizationCodeRepository.class);
        userQueryPort = mock(UserQueryPort.class);
        handler = new IssueAuthorizationCodeHandler(oauthClientRepository, authorizationCodeRepository, userQueryPort);
    }

    @Test
    void issues_authorization_code_successfully() {
        OAuthClient client = OAuthClient.create(
                1L,
                "atlashub-web",
                null,
                "AtlasHub Web",
                OAuthClientType.PUBLIC,
                Set.of("http://localhost:3000/callback"),
                Set.of("openid", "profile", "email")
        );
        when(oauthClientRepository.findByClientId("atlashub-web")).thenReturn(Optional.of(client));

        UserQueryPort.UserDto user = new UserQueryPort.UserDto(
                42L, "Jane", "Doe", "jane@example.com", "NG", 100L, ApiEnvironment.LIVE, true
        );
        when(userQueryPort.findById(42L)).thenReturn(Optional.of(user));
        when(authorizationCodeRepository.nextIdentity()).thenReturn(501L);

        IssueAuthorizationCodeCommand command = new IssueAuthorizationCodeCommand(
                "atlashub-web",
                "http://localhost:3000/callback",
                "code",
                "openid profile",
                "state_xyz",
                "E9Melhoa2OwvFrGMTJguCH5rtG6uEiBQGrA042-EXTguIDA",
                "S256",
                "nonce_123",
                42L
        );

        IssueAuthorizationCodeResult result = handler.execute(command);

        assertNotNull(result.code());
        assertEquals("http://localhost:3000/callback", result.redirectUri());
        assertEquals("state_xyz", result.state());

        ArgumentCaptor<AuthorizationCode> captor = ArgumentCaptor.forClass(AuthorizationCode.class);
        verify(authorizationCodeRepository).save(captor.capture());
        AuthorizationCode savedCode = captor.getValue();
        assertEquals("atlashub-web", savedCode.getClientId());
        assertEquals(42L, savedCode.getUserId());
        assertEquals("http://localhost:3000/callback", savedCode.getRedirectUri());
    }

    @Test
    void rejects_unsupported_response_type() {
        IssueAuthorizationCodeCommand command = new IssueAuthorizationCodeCommand(
                "atlashub-web",
                "http://localhost:3000/callback",
                "token",
                "openid",
                "state",
                "challenge",
                "S256",
                null,
                42L
        );

        assertThrows(OAuthException.class, () -> handler.execute(command));
    }

    @Test
    void rejects_unknown_client() {
        when(oauthClientRepository.findByClientId("unknown-client")).thenReturn(Optional.empty());

        IssueAuthorizationCodeCommand command = new IssueAuthorizationCodeCommand(
                "unknown-client",
                "http://localhost:3000/callback",
                "code",
                "openid",
                "state",
                "challenge",
                "S256",
                null,
                42L
        );

        assertThrows(InvalidClientException.class, () -> handler.execute(command));
    }

    @Test
    void rejects_unapproved_redirect_uri() {
        OAuthClient client = OAuthClient.create(
                1L,
                "atlashub-web",
                null,
                "AtlasHub Web",
                OAuthClientType.PUBLIC,
                Set.of("http://localhost:3000/callback"),
                Set.of("openid")
        );
        when(oauthClientRepository.findByClientId("atlashub-web")).thenReturn(Optional.of(client));

        IssueAuthorizationCodeCommand command = new IssueAuthorizationCodeCommand(
                "atlashub-web",
                "http://evil.com/callback",
                "code",
                "openid",
                "state",
                "challenge",
                "S256",
                null,
                42L
        );

        assertThrows(InvalidRedirectUriException.class, () -> handler.execute(command));
    }

    @Test
    void rejects_missing_pkce_challenge() {
        OAuthClient client = OAuthClient.create(
                1L,
                "atlashub-web",
                null,
                "AtlasHub Web",
                OAuthClientType.PUBLIC,
                Set.of("http://localhost:3000/callback"),
                Set.of("openid")
        );
        when(oauthClientRepository.findByClientId("atlashub-web")).thenReturn(Optional.of(client));

        IssueAuthorizationCodeCommand command = new IssueAuthorizationCodeCommand(
                "atlashub-web",
                "http://localhost:3000/callback",
                "code",
                "openid",
                "state",
                null,
                "S256",
                null,
                42L
        );

        assertThrows(InvalidPkceChallengeException.class, () -> handler.execute(command));
    }

    @Test
    void rejects_unverified_user() {
        OAuthClient client = OAuthClient.create(
                1L,
                "atlashub-web",
                null,
                "AtlasHub Web",
                OAuthClientType.PUBLIC,
                Set.of("http://localhost:3000/callback"),
                Set.of("openid")
        );
        when(oauthClientRepository.findByClientId("atlashub-web")).thenReturn(Optional.of(client));

        UserQueryPort.UserDto unverifiedUser = new UserQueryPort.UserDto(
                42L, "Jane", "Doe", "jane@example.com", "NG", 100L, ApiEnvironment.LIVE, false
        );
        when(userQueryPort.findById(42L)).thenReturn(Optional.of(unverifiedUser));

        IssueAuthorizationCodeCommand command = new IssueAuthorizationCodeCommand(
                "atlashub-web",
                "http://localhost:3000/callback",
                "code",
                "openid",
                "state",
                "challenge",
                "S256",
                null,
                42L
        );

        assertThrows(EmailVerificationRequired.class, () -> handler.execute(command));
    }
}
