package com.atlashub.authentication.infrastructure.security;

import com.atlashub.authentication.application.port.TokenPort;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.UUID;

@Component
public class JwtTokenAdapter implements TokenPort {

    private final SecretKey secretKey;
    private final long expirationMinutes;

    public JwtTokenAdapter(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration-minutes:15}") long expirationMinutes
    ) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMinutes = expirationMinutes;
    }

    @Override
    public AccessTokenResult generateAccessToken(AccessTokenPayload payload) {
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("UTC"));
        ZonedDateTime expiresAt = now.plusMinutes(expirationMinutes);
        String jti = UUID.randomUUID().toString();

        String token = Jwts.builder()
                .id(jti)
                .subject(payload.userId())
                .claim("orgId", payload.orgId())
                .claim("permissions", payload.permissions())
                .issuedAt(Date.from(now.toInstant()))
                .expiration(Date.from(expiresAt.toInstant()))
                .signWith(secretKey)
                .compact();

        return new AccessTokenResult(token, jti, expiresAt);
    }

    /**
     * Returns the secret key for use in JWT validation filters.
     */
    public SecretKey getSecretKey() {
        return secretKey;
    }
}
