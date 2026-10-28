package com.atlashub.authentication.infrastructure.services;

import com.atlashub.authentication.domain.entities.OAuthClient;
import com.atlashub.authentication.domain.repositories.OAuthClientRepository;
import com.atlashub.authentication.domain.valueobject.OAuthClientType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Component
public class OAuthClientInitializer {

    private static final Logger log = LoggerFactory.getLogger(OAuthClientInitializer.class);

    private final OAuthClientRepository oauthClientRepository;

    public OAuthClientInitializer(OAuthClientRepository oauthClientRepository) {
        this.oauthClientRepository = oauthClientRepository;
    }

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void initDefaultClients() {
        seedClient(
                "atlashub-web",
                "AtlasHub Web",
                OAuthClientType.PUBLIC,
                null,
                Set.of("http://localhost:3000/callback", "https://app.atlashub.com/callback"),
                Set.of("openid", "profile", "email", "offline_access")
        );

        seedClient(
                "atlashub-auth-web",
                "AtlasHub Auth Web",
                OAuthClientType.PUBLIC,
                null,
                Set.of("http://localhost:3002/callback", "https://auth.atlashub.com/callback"),
                Set.of("openid", "profile", "email", "offline_access")
        );

        seedClient(
                "atlashub-storefront-portal",
                "AtlasHub Storefront Portal",
                OAuthClientType.PUBLIC,
                null,
                Set.of("http://localhost:3001/callback", "https://store.atlashub.com/callback"),
                Set.of("openid", "profile", "email", "offline_access")
        );
    }

    private void seedClient(String clientId, String clientName, OAuthClientType clientType,
                            String clientSecret, Set<String> redirectUris, Set<String> scopes) {
        if (oauthClientRepository.findByClientId(clientId).isEmpty()) {
            Long id = oauthClientRepository.nextIdentity();
            OAuthClient client = OAuthClient.create(
                    id, clientId, clientSecret, clientName, clientType, redirectUris, scopes
            );
            oauthClientRepository.save(client);
            log.info("Initialized default OAuth client: {}", clientId);
        }
    }
}
