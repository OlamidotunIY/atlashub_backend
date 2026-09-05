package com.atlashub.accounts.adapter.in.messaging;

import com.atlashub.accounts.application.command.IssueVirtualAccountCommand;
import com.atlashub.accounts.application.usecase.IssueVirtualAccountUseCase;
import com.atlashub.shared.domain.valueobject.Country;
import com.atlashub.shared.adapter.in.messaging.BaseKafkaEventListener;
import com.atlashub.shared.domain.money.CurrencyCode;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
public class OrganizationComplianceApprovedListener extends BaseKafkaEventListener {

    private final IssueVirtualAccountUseCase issueVirtualAccountUseCase;

    public OrganizationComplianceApprovedListener(IssueVirtualAccountUseCase issueVirtualAccountUseCase, ObjectMapper objectMapper) {
        super(objectMapper);
        this.issueVirtualAccountUseCase = issueVirtualAccountUseCase;
    }

    @KafkaListener(topics = "organization-events", groupId = "accounts-module-group")
    public void onOrganizationComplianceApproved(String messagePayload) {
        processEventIfMatches(messagePayload, "OrganizationComplianceApproved", log, "accounts-module-group", root -> {
            String aggregateId = root.path("aggregateId").asText(null);
            if (aggregateId == null) {
                return;
            }

            Long integrationId = Long.valueOf(aggregateId);
            
            JsonNode payloadNode = root.path("payload");
            String OrganizationName = payloadNode.path("OrganizationName").asText("Main Account");
            String countryStr = payloadNode.path("country").asText("NIGERIA");
            
            CurrencyCode currency = Country.fromString(countryStr) != null 
                    ? Country.fromString(countryStr).getDefaultCurrency() 
                    : CurrencyCode.NGN;

            log.info("Received compliance approval for Organization {}. Issuing virtual accounts...", integrationId);

            // 1. Issue Wema Account
            issueVirtualAccountUseCase.execute(new IssueVirtualAccountCommand(
                    integrationId,
                    null, // Organization's own account
                    OrganizationName,
                    "Wema Bank",
                    currency,
                    UUID.randomUUID().toString()
            ));

            // 2. Issue Zenith Account
            issueVirtualAccountUseCase.execute(new IssueVirtualAccountCommand(
                    integrationId,
                    null, // Organization's own account
                    OrganizationName,
                    "Zenith Bank",
                    currency,
                    UUID.randomUUID().toString()
            ));
        });
    }
}
