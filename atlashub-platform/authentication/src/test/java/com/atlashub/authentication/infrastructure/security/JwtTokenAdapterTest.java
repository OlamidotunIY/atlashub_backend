package com.atlashub.authentication.infrastructure.security;

import com.atlashub.authentication.application.port.TokenPort;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class JwtTokenAdapterTest {

    @Test
    void signs_rs256_tokens_with_the_complete_session_context() {
        RsaSigningKeyProvider keys = new RsaSigningKeyProvider("", "", "test-key");
        JwtTokenAdapter adapter = new JwtTokenAdapter(keys, 15, "issuer", "audience");

        TokenPort.AccessTokenResult result = adapter.generateAccessToken(
                new TokenPort.AccessTokenPayload("1", "2", "3", "TEST", Set.of("orders:read")));
        var parsed = Jwts.parser().verifyWith(adapter.getPublicKey()).build()
                .parseSignedClaims(result.token());

        assertEquals("RS256", parsed.getHeader().getAlgorithm());
        assertEquals("at+jwt", parsed.getHeader().getType());
        assertEquals("1", parsed.getPayload().getSubject());
        assertEquals("2", parsed.getPayload().get("sid", String.class));
        assertEquals("3", parsed.getPayload().get("org", String.class));
        assertEquals("TEST", parsed.getPayload().get("env", String.class));
    }
}
