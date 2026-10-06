package com.atlashub.authentication.infrastructure.messaging.listeners;

import com.atlashub.authentication.application.command.RevokeUserSessions.RevokeUserSessionsCommand;
import com.atlashub.authentication.application.command.RevokeUserSessions.RevokeUserSessionsHandler;
import com.atlashub.authentication.infrastructure.messaging.events.MemberDeactivated;
import com.atlashub.shared.application.messaging.BaseKafkaEventListener;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeoutException;

@Component
public class MemberDeactivatedListener extends BaseKafkaEventListener {
    private static final Logger log = LoggerFactory.getLogger(MemberDeactivatedListener.class);
    private static final String GROUP_ID = "authentication-member-group";
    private final RevokeUserSessionsHandler handler;

    public MemberDeactivatedListener(ObjectMapper objectMapper, RevokeUserSessionsHandler handler) {
        super(objectMapper);
        this.handler = handler;
    }

    @PostConstruct
    public void init() {
        registerSubscription("MemberDeactivatedEvent", GROUP_ID);
    }

    @KafkaListener(topics = "iam-events", groupId = GROUP_ID)
    public void listen(String payload) {
        processEventIfMatches(payload, "MemberDeactivatedEvent", MemberDeactivated.class, log, GROUP_ID,
                error -> error instanceof TimeoutException,
                event -> handler.execute(new RevokeUserSessionsCommand(event.payload().userId())));
    }
}
