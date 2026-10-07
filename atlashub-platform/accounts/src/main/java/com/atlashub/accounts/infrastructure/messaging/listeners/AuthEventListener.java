package com.atlashub.accounts.infrastructure.messaging.listeners;

import com.atlashub.accounts.application.command.ApplyAuthenticationEvent.ApplyAuthenticationEventCommand;
import com.atlashub.accounts.application.command.ApplyAuthenticationEvent.ApplyAuthenticationEventHandler;
import com.atlashub.accounts.infrastructure.messaging.events.AuthEmailVerified;
import com.atlashub.accounts.infrastructure.messaging.events.ActiveOrganizationSwitched;
import com.atlashub.accounts.infrastructure.messaging.events.ActiveEnvironmentSwitched;
import com.atlashub.shared.application.messaging.BaseKafkaEventListener;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeoutException;

@Component
public class AuthEventListener extends BaseKafkaEventListener {

    private static final Logger log = LoggerFactory.getLogger(AuthEventListener.class);
    private static final String GROUP_ID = "accounts-auth-group";

    private final ApplyAuthenticationEventHandler handler;

    public AuthEventListener(ObjectMapper objectMapper, ApplyAuthenticationEventHandler handler) {
        super(objectMapper);
        this.handler = handler;
    }

    @PostConstruct
    public void init() {
        registerSubscription("AuthEmailVerifiedEvent", GROUP_ID);
        registerSubscription("ActiveOrganizationSwitchedEvent", GROUP_ID);
        registerSubscription("ActiveEnvironmentSwitchedEvent", GROUP_ID);
    }

    @KafkaListener(topics = "auth-events", groupId = GROUP_ID)
    public void listen(String messagePayload) {
        processEventIfMatches(messagePayload, "AuthEmailVerifiedEvent", AuthEmailVerified.class, log, GROUP_ID, e -> e instanceof TimeoutException, event -> {
            Long userId = Long.parseLong(event.payload().userId());
            handler.execute(new ApplyAuthenticationEventCommand(userId, null, null, true));
        });
        processEventIfMatches(messagePayload, "ActiveOrganizationSwitchedEvent", ActiveOrganizationSwitched.class, log, GROUP_ID, e -> e instanceof TimeoutException, event -> {
            handler.execute(new ApplyAuthenticationEventCommand(event.payload().userId(), event.payload().organizationId(), null, false));
        });
        processEventIfMatches(messagePayload, "ActiveEnvironmentSwitchedEvent", ActiveEnvironmentSwitched.class, log, GROUP_ID, e -> e instanceof TimeoutException, event ->
                handler.execute(new ApplyAuthenticationEventCommand(event.payload().userId(), null, event.payload().environment(), false)));
    }
}
