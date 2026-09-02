package com.atlashub.auth.domain.model;

import com.atlashub.auth.domain.event.AuthNewDeviceLoginEvent;
import com.atlashub.auth.domain.event.AuthSessionCreatedEvent;
import com.atlashub.auth.domain.event.SessionPayload;
import com.atlashub.auth.domain.event.AuthSessionRevokedEvent;
import com.atlashub.auth.domain.valueobject.PrincipalType;
import com.atlashub.auth.domain.valueobject.SessionStatus;
import com.atlashub.shared.domain.AggregateRoot;
import lombok.Getter;

import java.time.ZonedDateTime;
import java.util.UUID;

@Getter
public class Session extends AggregateRoot<Long> {

    private final Long id;
    private final Long authAccountId;
    private final Long principalId;
    private final PrincipalType principalType;
    private final String token;
    private final String ipAddress;
    private final String userAgent;
    private SessionStatus status;
    private final ZonedDateTime createdAt;
    private final ZonedDateTime expiresAt;
    private ZonedDateTime revokedAt;

    public Session(Long id, Long authAccountId, Long principalId, PrincipalType principalType, String token, String ipAddress, String userAgent, ZonedDateTime expiresAt, SessionStatus status) {
        this.id = id;
        this.authAccountId = authAccountId;
        this.principalId = principalId;
        this.principalType = principalType;
        this.token = token;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
        this.expiresAt = expiresAt;
        this.status = status;
        this.createdAt = ZonedDateTime.now();
    }

    public static Session create(Long id, Long authAccountId, Long principalId, PrincipalType principalType, String jti, String ipAddress, String userAgent, ZonedDateTime expiresAt, boolean isNewDevice) {
        Session session = new Session(id, authAccountId, principalId, principalType, jti, ipAddress, userAgent, expiresAt, SessionStatus.ACTIVE);
        
        SessionPayload payload = new SessionPayload(jti, expiresAt, principalId, principalType, ipAddress, userAgent);
        
        session.registerEvent(
                new AuthSessionCreatedEvent(
                        UUID.randomUUID().toString(),
                        String.valueOf(session.getId()),
                        ZonedDateTime.now(),
                        payload
                ));
                
        if (isNewDevice) {
            session.registerEvent(
                    new AuthNewDeviceLoginEvent(
                            UUID.randomUUID().toString(),
                            String.valueOf(session.getId()),
                            ZonedDateTime.now(),
                            payload
                    ));
        }
        return session;
    }

    public void revoke() {
        if (this.status != SessionStatus.REVOKED) {
            this.status = SessionStatus.REVOKED;
            this.revokedAt = ZonedDateTime.now();
            
            this.registerEvent(
                new AuthSessionRevokedEvent(
                        UUID.randomUUID().toString(),
                        String.valueOf(this.getId()),
                        ZonedDateTime.now(),
                        new SessionPayload(this.token, this.expiresAt, this.principalId, this.principalType, this.ipAddress, this.userAgent)
                ));
        }
    }

    public void expire() {
        if (this.status == SessionStatus.ACTIVE) {
            this.status = SessionStatus.EXPIRED;
        }
    }

    @Override
    public Long getId() {
        return id;
    }
}
