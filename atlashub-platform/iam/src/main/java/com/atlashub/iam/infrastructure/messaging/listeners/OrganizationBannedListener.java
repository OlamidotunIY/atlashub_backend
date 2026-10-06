package com.atlashub.iam.infrastructure.messaging.listeners;

import com.atlashub.iam.application.commands.DeactivateOrganizationMembers.DeactivateOrganizationMembersCommand;
import com.atlashub.iam.application.commands.DeactivateOrganizationMembers.DeactivateOrganizationMembersHandler;
import com.atlashub.iam.infrastructure.messaging.events.OrganizationBanned;
import com.atlashub.shared.application.messaging.BaseKafkaEventListener;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import java.util.concurrent.TimeoutException;

@Component("iamOrganizationBannedListener")
public class OrganizationBannedListener extends BaseKafkaEventListener {
    private static final Logger log = LoggerFactory.getLogger(OrganizationBannedListener.class);
    private static final String GROUP_ID = "iam-organization-ban-group";
    private final DeactivateOrganizationMembersHandler handler;

    public OrganizationBannedListener(ObjectMapper mapper, DeactivateOrganizationMembersHandler handler) {
        super(mapper);
        this.handler = handler;
    }

    @PostConstruct
    public void init() { registerSubscription("OrganizationBannedEvent", GROUP_ID); }

    @KafkaListener(topics = "admin-events", groupId = GROUP_ID)
    public void listen(String payload) {
        processEventIfMatches(payload, "OrganizationBannedEvent", OrganizationBanned.class, log, GROUP_ID,
                error -> error instanceof TimeoutException,
                event -> handler.execute(new DeactivateOrganizationMembersCommand(
                        event.payload().organizationId())));
    }
}
