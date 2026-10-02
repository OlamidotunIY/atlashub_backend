package com.atlashub.authentication.infrastructure.security;

import com.atlashub.authentication.application.port.TokenPort;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.PublicKey;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.UUID;

@Component
public class JwtTokenAdapter implements TokenPort {

    private final RsaSigningKeyProvider keyProvider;
    private final long expirationMinutes;
    private final String issuer;
    private final String audience;

    public JwtTokenAdapter(
            RsaSigningKeyProvider keyProvider,
            @Value("${jwt.expiration-minutes:15}") long expirationMinutes,
            @Value("${atlashub.jwt.issuer:atlashub-authentication}") String issuer,
            @Value("${atlashub.jwt.audience:atlashub-api}") String audience
    ) {
        this.keyProvider = keyProvider;
        this.expirationMinutes = expirationMinutes;
        this.issuer = issuer;
        this.audience = audience;
    }

    @Override
    public AccessTokenResult generateAccessToken(AccessTokenPayload payload) {
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("UTC"));
        ZonedDateTime expiresAt = now.plusMinutes(expirationMinutes);
        String jti = UUID.randomUUID().toString();

        String token = Jwts.builder()
                .id(jti)
                .subject(payload.userId())
                .issuer(issuer)
                .audience().add(audience).and()
                .header().keyId(keyProvider.keyId()).type("at+jwt").and()
                .claim("sid", payload.sessionId())
                .claim("org", payload.orgId())
                .claim("env", payload.environment())
                .claim("permissions", payload.permissions())
                .issuedAt(Date.from(now.toInstant()))
                .notBefore(Date.from(now.toInstant()))
                .expiration(Date.from(expiresAt.toInstant()))
                .signWith(keyProvider.privateKey(), Jwts.SIG.RS256)
                .compact();

        return new AccessTokenResult(token, jti, expiresAt);
    }

    /**
     * Returns the secret key for use in JWT validation filters.
     */
    public PublicKey getPublicKey() {
        return keyProvider.publicKey();
    }

    public String issuer() { return issuer; }

    public String audience() { return audience; }
}
