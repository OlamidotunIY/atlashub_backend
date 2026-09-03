package com.atlashub.accounts.adapter.in.messaging;

import com.atlashub.accounts.application.command.BootstrapOrganizationAccountsCommand;
import com.atlashub.accounts.application.usecase.BootstrapOrganizationAccountsUseCase;
import com.atlashub.identity.domain.event.OrganizationRegistered;
import com.atlashub.shared.adapter.in.messaging.BaseKafkaEventListener;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class OrganizationRegisteredListener extends BaseKafkaEventListener {

    private final BootstrapOrganizationAccountsUseCase bootstrapUseCase;

    public OrganizationRegisteredListener(BootstrapOrganizationAccountsUseCase bootstrapUseCase, ObjectMapper objectMapper) {
        super(objectMapper);
        this.bootstrapUseCase = bootstrapUseCase;
    }

    @KafkaListener(topics = "Identity-events", groupId = "accounts-module-bootstrap-group")
    public void onOrganizationRegistered(String messagePayload) {
        processEventIfMatches(messagePayload, "OrganizationRegistered", OrganizationRegistered.class, log,  "accounts-module-bootstrap-group", event -> {
            String aggregateId = event.aggregateId();
            if (aggregateId == null) {
                return;
            }

            Long organizationId = Long.valueOf(aggregateId);
            
            log.info("Received OrganizationRegistered event for org {}. Bootstrapping internal ledger accounts...", organizationId);

            bootstrapUseCase.execute(new BootstrapOrganizationAccountsCommand(organizationId, event.payload().currency()));
        });
    }
}