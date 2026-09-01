package com.atlashub.auth.adapter.in.messaging;

import com.atlashub.auth.application.port.out.TokenCachePort;
import com.atlashub.auth.domain.event.SessionCreatedEvent;
import com.atlashub.auth.domain.event.SessionRevokedEvent;
import com.atlashub.auth.domain.event.SessionPayload;
import com.atlashub.shared.event.EnvelopedDomainEvent;
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
        if (event.event() instanceof SessionCreatedEvent sessionCreatedEvent) {
            tokenCachePort.cacheSession(
                    sessionCreatedEvent.payload().token(),
                    sessionCreatedEvent.payload().expiresAt()
            );
        }
    }

    @EventListener
    @Async
    public void onSessionRevoked(EnvelopedDomainEvent<SessionPayload> event) {
        if (event.event() instanceof SessionRevokedEvent sessionRevokedEvent) {
            tokenCachePort.blacklistToken(
                    sessionRevokedEvent.payload().token(),
                    sessionRevokedEvent.payload().expiresAt()
            );
        }
    }
}
