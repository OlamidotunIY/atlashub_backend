package com.atlashub.notifications.adapter.in.messaging;

import com.atlashub.identity.application.port.OrganizationQueryService;
import com.atlashub.identity.application.port.UserQueryService;
import com.atlashub.identity.application.result.UserDto;
import com.atlashub.identity.domain.event.OrganizationMemberAdded;
import com.atlashub.notifications.application.port.EmailSenderPort;
import com.atlashub.shared.adapter.out.external.dlq.DeadLetterRepository;
import com.atlashub.shared.adapter.in.messaging.BaseKafkaEventListener;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.retrytopic.DltStrategy;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class IdentityEventListener extends BaseKafkaEventListener {

    private static final Logger log = LoggerFactory.getLogger(IdentityEventListener.class);

    private final EmailSenderPort emailSenderPort;
    private final UserQueryService userQuery;
    private final OrganizationQueryService organizationQuery;

    public IdentityEventListener(
            EmailSenderPort emailSenderPort,
            UserQueryService UserQueryPort,
            OrganizationQueryService OrganizationQueryPort,
            ObjectMapper objectMapper,
            DeadLetterRepository deadLetterRepository
    ) {
        super(objectMapper);
        this.emailSenderPort = emailSenderPort;
        this.userQuery = UserQueryPort;
        this.organizationQuery = OrganizationQueryPort;
        this.deadLetterRepository = deadLetterRepository;
    }

    @RetryableTopic(
            attempts = "3",
            backoff = @Backoff(delay = 5000, multiplier = 2.0),
            dltStrategy = DltStrategy.FAIL_ON_ERROR
    )
    @KafkaListener(topics = "organization-events", groupId = "notifications-identity-group")
    public void handleIdentityEvent(String message) {
        processEventIfMatches(message, "OrganizationMemberAdded", OrganizationMemberAdded.class, log, "notifications-identity-group", event -> {
            Optional<UserDto> userOpt = userQuery.getUserById(event.payload().userId());
            Optional<OrganizationQueryService.OrganizationSharedDto> orgOpt = organizationQuery.getOrganizationById(event.payload().organizationId());

            if (userOpt.isPresent() && orgOpt.isPresent()) {
                UserDto user = userOpt.get();
                OrganizationQueryService.OrganizationSharedDto org = orgOpt.get();

                emailSenderPort.sendOrganizationJoinedEmail(
                        user.email(),
                        user.firstName(),
                        org.businessName()
                );
                log.info("Sent organization joined email to {} for organization {}", user.email(), org.businessName());
            } else {
                log.warn("Could not send organization joined email: User or Organization not found for event {}", event.eventId());
            }
        });
    }
}
