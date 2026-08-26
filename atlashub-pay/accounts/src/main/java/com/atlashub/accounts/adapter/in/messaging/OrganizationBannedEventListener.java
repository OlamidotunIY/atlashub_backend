package com.atlashub.accounts.adapter.in.messaging;

import com.atlashub.accounts.application.command.ForceCloseAccountsCommand;
import com.atlashub.accounts.application.usecase.ForceCloseAccountsUseCase;
import com.atlashub.shared.event.BaseKafkaEventListener;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class OrganizationBannedEventListener extends BaseKafkaEventListener {

    private final ForceCloseAccountsUseCase forceCloseAccountsUseCase;

    public OrganizationBannedEventListener(ForceCloseAccountsUseCase forceCloseAccountsUseCase, ObjectMapper objectMapper) {
        super(objectMapper);
        this.forceCloseAccountsUseCase = forceCloseAccountsUseCase;
    }

    @KafkaListener(topics = "Organization-events", groupId = "accounts-module-group")
    public void onOrganizationBanned(String messagePayload) {
        processEventIfMatches(messagePayload, "OrganizationBanned", log, root -> {
            String aggregateId = root.path("aggregateId").asText(null);
            if (aggregateId == null) {
                return;
            }

            Long integration = Long.valueOf(aggregateId);
            
            log.warn("Received OrganizationBanned event for integration {}. Forcing closure of all virtual accounts...", integration);

            forceCloseAccountsUseCase.execute(new ForceCloseAccountsCommand(integration));
            
            log.info("Successfully requested closure for all virtual accounts belonging to integration {}", integration);
        });
    }
}
