package com.atlashub.iam.infrastructure.messaging.listeners;

import com.atlashub.iam.application.commands.SuspendOrganizationMembers.SuspendOrganizationMembersCommand;
import com.atlashub.iam.application.commands.SuspendOrganizationMembers.SuspendOrganizationMembersHandler;
import com.atlashub.iam.infrastructure.messaging.events.SubscriptionSuspended;
import com.atlashub.shared.application.messaging.BaseKafkaEventListener;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import java.util.concurrent.TimeoutException;

@Component
public class SubscriptionSuspendedListener extends BaseKafkaEventListener {
    private static final Logger log = LoggerFactory.getLogger(SubscriptionSuspendedListener.class);
    private static final String GROUP_ID = "iam-subscription-group";
    private final SuspendOrganizationMembersHandler handler;

    public SubscriptionSuspendedListener(ObjectMapper mapper, SuspendOrganizationMembersHandler handler) {
        super(mapper);
        this.handler = handler;
    }

    @PostConstruct
    public void init() { registerSubscription("SubscriptionSuspendedEvent", GROUP_ID); }

    @KafkaListener(topics = "billing-events", groupId = GROUP_ID)
    public void listen(String payload) {
        processEventIfMatches(payload, "SubscriptionSuspendedEvent", SubscriptionSuspended.class, log, GROUP_ID,
                error -> error instanceof TimeoutException,
                event -> handler.execute(new SuspendOrganizationMembersCommand(
                        event.payload().organizationId(), "Subscription suspended")));
    }
}
