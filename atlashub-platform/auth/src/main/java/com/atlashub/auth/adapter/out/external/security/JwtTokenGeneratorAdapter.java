package com.atlashub.auth.adapter.out.external.security;

import com.atlashub.auth.application.port.in.TokenGeneratorPort;
import com.atlashub.auth.adapter.out.external.config.JwtProperties;
import io.jsonwebtoken.Jwts;

import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
import java.util.Date;
import java.util.UUID;
import javax.crypto.SecretKey;

@Component
public class JwtTokenGeneratorAdapter implements TokenGeneratorPort {

    private final SecretKey key;
    private final JwtProperties jwtProperties;

    public JwtTokenGeneratorAdapter(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
        this.key = Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes());
    }

    @Override
    public TokenData generateAccessToken(Long principalId, String principalType, String scope) {
        String jti = UUID.randomUUID().toString();
        ZonedDateTime expiresAt = ZonedDateTime.now().plusNanos(jwtProperties.getAccessTokenExpirationMs() * 1000000);
        
        String token = Jwts.builder()
                .id(jti)
                .subject(String.valueOf(principalId))
                .claim("type", principalType)
                .claim("scope", scope)
                .claim("purpose", "access")
                .issuedAt(new Date())
                .expiration(Date.from(expiresAt.toInstant()))
                .signWith(key)
                .compact();

        return new TokenData(token, jti, expiresAt);
    }

    @Override
    public TokenData generateRefreshToken(Long principalId, String principalType, String jti) {
        ZonedDateTime expiresAt = ZonedDateTime.now().plusNanos(jwtProperties.getRefreshTokenExpirationMs() * 1000000);

        String token = Jwts.builder()
                .id(jti)
                .subject(String.valueOf(principalId))
                .claim("type", principalType)
                .claim("purpose", "refresh")
                .issuedAt(new Date())
                .expiration(Date.from(expiresAt.toInstant()))
                .signWith(key)
                .compact();

        return new TokenData(token, jti, expiresAt);
    }

    @Override
    public TokenData generatePreAuthToken(Long principalId, String principalType) {
        String jti = UUID.randomUUID().toString();
        ZonedDateTime expiresAt = ZonedDateTime.now().plusNanos(jwtProperties.getPreAuthTokenExpirationMs() * 1000000);

        String token = Jwts.builder()
                .id(jti)
                .subject(String.valueOf(principalId))
                .claim("type", principalType)
                .claim("purpose", "pre-auth")
                .issuedAt(new Date())
                .expiration(Date.from(expiresAt.toInstant()))
                .signWith(key)
                .compact();

        return new TokenData(token, jti, expiresAt);
    }

    @Override
    public TokenData generateSetupToken(Long principalId, String principalType) {
        String jti = UUID.randomUUID().toString();
        // Use the same expiration as pre-auth for simplicity, or we could add a new property
        ZonedDateTime expiresAt = ZonedDateTime.now().plusNanos(jwtProperties.getPreAuthTokenExpirationMs() * 1000000);

        String token = Jwts.builder()
                .id(jti)
                .subject(String.valueOf(principalId))
                .claim("type", principalType)
                .claim("purpose", "setup")
                .issuedAt(new Date())
                .expiration(Date.from(expiresAt.toInstant()))
                .signWith(key)
                .compact();

        return new TokenData(token, jti, expiresAt);
    }
}
