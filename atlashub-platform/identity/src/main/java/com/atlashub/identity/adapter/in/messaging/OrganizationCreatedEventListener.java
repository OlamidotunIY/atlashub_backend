package com.atlashub.identity.adapter.in.messaging;

import com.atlashub.identity.application.command.GenerateTestApiKeyPairCommand;
import com.atlashub.identity.application.usecase.GenerateTestApiKeyPairUseCase;
import com.atlashub.identity.domain.event.OrganizationRegistered;
import com.atlashub.shared.event.BaseKafkaEventListener;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class OrganizationCreatedEventListener extends BaseKafkaEventListener {

    private final GenerateTestApiKeyPairUseCase generateTestApiKeyPairUseCase;

    protected OrganizationCreatedEventListener(ObjectMapper objectMapper, GenerateTestApiKeyPairUseCase generateTestApiKeyPairUseCase) {
        super(objectMapper);
        this.generateTestApiKeyPairUseCase = generateTestApiKeyPairUseCase;
    }

    @KafkaListener(topics = "Organization-events", groupId = "identity-module-group")
    public void onOrganizationRegistered(String messagePayload) {
        processEventIfMatches(messagePayload, "OrganizationRegistered", OrganizationRegistered.class, log, "identity-module-group", event -> {
            String aggregateId = event.aggregateId();
            if (aggregateId == null) return;

            Long integration = Long.valueOf(aggregateId);

            log.warn("Received OrganizationRegistered event for Organization {}. Creating test api keys for Organization...", integration);

            generateTestApiKeyPairUseCase.execute(new GenerateTestApiKeyPairCommand(integration));

            log.info("Successfully created test api key pair for Organization {}", integration);
        });
    }
}
