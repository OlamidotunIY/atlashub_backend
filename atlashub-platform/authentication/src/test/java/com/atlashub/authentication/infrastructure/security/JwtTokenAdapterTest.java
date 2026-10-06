package com.atlashub.authentication.infrastructure.security;

import com.atlashub.authentication.application.port.TokenPort;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.Base64;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JwtTokenAdapterTest {

    @Test
    void signs_rs256_tokens_with_the_complete_session_context() {
        RsaSigningKeyProvider keys = new RsaSigningKeyProvider("", "", "", "", "test-key", true);
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

    @Test
    void rejects_missing_keys_outside_local_development() {
        assertThrows(IllegalStateException.class,
                () -> new RsaSigningKeyProvider("", "", "", "", "test-key", false));
    }

    @Test
    void loads_rs256_keys_from_mounted_files() throws Exception {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        KeyPair keyPair = generator.generateKeyPair();
        Path privateKey = Files.createTempFile("atlashub-jwt-private", ".pem");
        Path publicKey = Files.createTempFile("atlashub-jwt-public", ".pem");

        try {
            Files.writeString(privateKey, pem("PRIVATE KEY", keyPair.getPrivate().getEncoded()));
            Files.writeString(publicKey, pem("PUBLIC KEY", keyPair.getPublic().getEncoded()));

            RsaSigningKeyProvider keys = new RsaSigningKeyProvider(
                    "", "", privateKey.toString(), publicKey.toString(), "test-key", false);

            assertEquals(keyPair.getPublic(), keys.publicKey());
        } finally {
            Files.deleteIfExists(privateKey);
            Files.deleteIfExists(publicKey);
        }
    }

    private String pem(String type, byte[] encoded) {
        return "-----BEGIN " + type + "-----\n"
                + Base64.getMimeEncoder(64, "\n".getBytes()).encodeToString(encoded)
                + "\n-----END " + type + "-----\n";
    }
}
