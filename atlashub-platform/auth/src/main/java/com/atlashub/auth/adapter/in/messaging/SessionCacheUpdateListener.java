package com.atlashub.auth.adapter.in.messaging;

import com.atlashub.auth.application.port.out.TokenCachePort;
import com.atlashub.auth.domain.event.AuthSessionCreatedEvent;
import com.atlashub.auth.domain.event.AuthSessionRevokedEvent;
import com.atlashub.auth.domain.event.SessionPayload;
import com.atlashub.shared.domain.event.EnvelopedDomainEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class SessionCacheUpdateListener {

    private final TokenCachePort tokenCachePort;

    public SessionCacheUpdateListener(TokenCachePort tokenCachePort) {
        this.tokenCachePort = tokenCachePort;
    }

    @EventListener
    @Async
    public void onSessionCreated(EnvelopedDomainEvent<SessionPayload> event) {
        if (event.event() instanceof AuthSessionCreatedEvent AuthSessionCreatedEvent) {
            tokenCachePort.cacheSession(
                    AuthSessionCreatedEvent.payload().token(),
                    AuthSessionCreatedEvent.payload().expiresAt()
            );
        }
    }

    @EventListener
    @Async
    public void onSessionRevoked(EnvelopedDomainEvent<SessionPayload> event) {
        if (event.event() instanceof AuthSessionRevokedEvent AuthSessionRevokedEvent) {
            tokenCachePort.blacklistToken(
                    AuthSessionRevokedEvent.payload().token(),
                    AuthSessionRevokedEvent.payload().expiresAt()
            );
        }
    }
}
