package com.atlashub.authentication.application.command.ExchangeToken;

import com.atlashub.authentication.application.command.RefreshToken.RefreshTokenCommand;
import com.atlashub.authentication.application.command.RefreshToken.RefreshTokenHandler;
import com.atlashub.authentication.application.command.RefreshToken.RefreshTokenResponse;
import com.atlashub.authentication.application.port.TokenPort;
import com.atlashub.authentication.domain.entities.AuthAccount;
import com.atlashub.authentication.domain.entities.AuthorizationCode;
import com.atlashub.authentication.domain.entities.OAuthClient;
import com.atlashub.authentication.domain.entities.Session;
import com.atlashub.authentication.domain.exceptions.InvalidGrantException;
import com.atlashub.authentication.domain.exceptions.InvalidPkceChallengeException;
import com.atlashub.authentication.domain.exceptions.OAuthException;
import com.atlashub.authentication.domain.repositories.AuthAccountRepository;
import com.atlashub.authentication.domain.repositories.AuthorizationCodeRepository;
import com.atlashub.authentication.domain.repositories.OAuthClientRepository;
import com.atlashub.authentication.domain.repositories.SessionRepository;
import com.atlashub.authentication.domain.services.PkceVerifier;
import com.atlashub.authentication.domain.valueobject.CodeChallengeMethod;
import com.atlashub.authentication.domain.valueobject.OAuthClientType;
import com.atlashub.shared.application.port.MembershipQueryPort;
import com.atlashub.shared.application.port.PasswordEncoderPort;
import com.atlashub.shared.application.port.UserQueryPort;
import com.atlashub.shared.application.security.ApiEnvironment;
import com.atlashub.shared.application.service.HashingUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ExchangeTokenHandlerTest {

    private OAuthClientRepository oauthClientRepository;
    private AuthorizationCodeRepository authorizationCodeRepository;
    private SessionRepository sessionRepository;
    private AuthAccountRepository authAccountRepository;
    private UserQueryPort userQueryPort;
    private MembershipQueryPort membershipQueryPort;
    private TokenPort tokenPort;
    private PkceVerifier pkceVerifier;
    private PasswordEncoderPort passwordEncoderPort;
    private RefreshTokenHandler refreshTokenHandler;
    private ExchangeTokenHandler handler;

    @BeforeEach
    void setUp() {
        oauthClientRepository = mock(OAuthClientRepository.class);
        authorizationCodeRepository = mock(AuthorizationCodeRepository.class);
        sessionRepository = mock(SessionRepository.class);
        authAccountRepository = mock(AuthAccountRepository.class);
        userQueryPort = mock(UserQueryPort.class);
        membershipQueryPort = mock(MembershipQueryPort.class);
        tokenPort = mock(TokenPort.class);
        pkceVerifier = new PkceVerifier();
        passwordEncoderPort = mock(PasswordEncoderPort.class);
        refreshTokenHandler = mock(RefreshTokenHandler.class);

        handler = new ExchangeTokenHandler(
                oauthClientRepository,
                authorizationCodeRepository,
                sessionRepository,
                authAccountRepository,
                userQueryPort,
                membershipQueryPort,
                tokenPort,
                pkceVerifier,
                passwordEncoderPort,
                refreshTokenHandler
        );
    }

    @Test
    void exchanges_authorization_code_with_valid_pkce_successfully() {
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

        String codeVerifier = "dBjftJeZ4CVP-mB92K27uhbUJU1p1r_wW1gFWFOEjXk";
        String codeChallenge = "E9Melhoa2OwvFrEMTJguCHaoeK1t8URWbuGJSstw-cM";

        String rawCode = "valid_auth_code_123";
        String codeHash = HashingUtils.sha256Hex(rawCode);

        AuthorizationCode authCode = AuthorizationCode.create(
                10L,
                codeHash,
                "atlashub-web",
                42L,
                "http://localhost:3000/callback",
                Set.of("openid", "profile"),
                codeChallenge,
                CodeChallengeMethod.S256,
                "nonce_123",
                ZonedDateTime.now().plusMinutes(5)
        );
        when(authorizationCodeRepository.findByCodeHash(codeHash)).thenReturn(Optional.of(authCode));

        UserQueryPort.UserDto user = new UserQueryPort.UserDto(
                42L, "Jane", "Doe", "jane@example.com", "NG", 100L, ApiEnvironment.LIVE, true
        );
        when(userQueryPort.findById(42L)).thenReturn(Optional.of(user));

        AuthAccount account = AuthAccount.createCredentialsAccount(1L, 42L, "jane@example.com", "hash");
        when(authAccountRepository.findByUserId(42L)).thenReturn(Optional.of(account));
        when(membershipQueryPort.getMemberStatus(42L, 100L)).thenReturn(MembershipQueryPort.MembershipStatus.ACTIVE);
        when(membershipQueryPort.getPermissions(42L, 100L)).thenReturn(Set.of("charges:create"));
        when(sessionRepository.nextIdentity()).thenReturn(999L);

        when(tokenPort.generateAccessToken(any())).thenReturn(
                new TokenPort.AccessTokenResult("jwt_access_token", "jti_123", ZonedDateTime.now().plusMinutes(15))
        );
        when(tokenPort.generateIdToken(any())).thenReturn(
                new TokenPort.IdTokenResult("jwt_id_token", ZonedDateTime.now().plusMinutes(15))
        );

        ExchangeTokenCommand command = new ExchangeTokenCommand(
                "authorization_code",
                rawCode,
                "http://localhost:3000/callback",
                "atlashub-web",
                null,
                codeVerifier,
                null,
                "fp_123",
                "127.0.0.1",
                "Mozilla/5.0"
        );

        ExchangeTokenResult result = handler.execute(command);

        assertEquals("jwt_access_token", result.accessToken());
        assertEquals("Bearer", result.tokenType());
        assertEquals(900L, result.expiresIn());
        assertNotNull(result.refreshToken());
        assertEquals("jwt_id_token", result.idToken());
        assertTrue(authCode.isUsed());
        verify(authorizationCodeRepository).save(authCode);
        verify(sessionRepository).save(any(Session.class));
    }

    @Test
    void rejects_already_used_authorization_code() {
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

        String rawCode = "used_code";
        String codeHash = HashingUtils.sha256Hex(rawCode);

        AuthorizationCode authCode = AuthorizationCode.create(
                10L,
                codeHash,
                "atlashub-web",
                42L,
                "http://localhost:3000/callback",
                Set.of("openid"),
                "challenge",
                CodeChallengeMethod.S256,
                null,
                ZonedDateTime.now().plusMinutes(5)
        );
        authCode.markAsUsed();
        when(authorizationCodeRepository.findByCodeHash(codeHash)).thenReturn(Optional.of(authCode));

        ExchangeTokenCommand command = new ExchangeTokenCommand(
                "authorization_code",
                rawCode,
                "http://localhost:3000/callback",
                "atlashub-web",
                null,
                "dBjftJeZ4CVP-mB92K27uhbUJU1p1r_wW1gFWFOEjXk",
                null,
                "fp_123",
                "127.0.0.1",
                "Mozilla/5.0"
        );

        assertThrows(InvalidGrantException.class, () -> handler.execute(command));
    }

    @Test
    void rejects_invalid_pkce_verifier() {
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

        String rawCode = "valid_code";
        String codeHash = HashingUtils.sha256Hex(rawCode);

        AuthorizationCode authCode = AuthorizationCode.create(
                10L,
                codeHash,
                "atlashub-web",
                42L,
                "http://localhost:3000/callback",
                Set.of("openid"),
                "E9Melhoa2OwvFrGMTJguCH5rtG6uEiBQGrA042-EXTguIDA",
                CodeChallengeMethod.S256,
                null,
                ZonedDateTime.now().plusMinutes(5)
        );
        when(authorizationCodeRepository.findByCodeHash(codeHash)).thenReturn(Optional.of(authCode));

        ExchangeTokenCommand command = new ExchangeTokenCommand(
                "authorization_code",
                rawCode,
                "http://localhost:3000/callback",
                "atlashub-web",
                null,
                "wrong_verifier_1234567890123456789012345678901234567890",
                null,
                "fp_123",
                "127.0.0.1",
                "Mozilla/5.0"
        );

        assertThrows(InvalidPkceChallengeException.class, () -> handler.execute(command));
    }

    @Test
    void exchanges_refresh_token_grant_successfully() {
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

        when(refreshTokenHandler.execute(any(RefreshTokenCommand.class))).thenReturn(
                new RefreshTokenResponse("new_access_token", ZonedDateTime.now().plusMinutes(15), "new_refresh_token", ZonedDateTime.now().plusDays(30))
        );

        ExchangeTokenCommand command = new ExchangeTokenCommand(
                "refresh_token",
                null,
                null,
                "atlashub-web",
                null,
                null,
                "existing_refresh_token",
                "fp_123",
                "127.0.0.1",
                "Mozilla/5.0"
        );

        ExchangeTokenResult result = handler.execute(command);

        assertEquals("new_access_token", result.accessToken());
        assertEquals("new_refresh_token", result.refreshToken());
        assertEquals("Bearer", result.tokenType());
    }

    @Test
    void rejects_unsupported_grant_type() {
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

        ExchangeTokenCommand command = new ExchangeTokenCommand(
                "implicit",
                null,
                null,
                "atlashub-web",
                null,
                null,
                null,
                "fp_123",
                "127.0.0.1",
                "Mozilla/5.0"
        );

        assertThrows(OAuthException.class, () -> handler.execute(command));
    }
}
