package com.atlashub.auth.adapter.in.messaging;

import com.atlashub.auth.application.command.CreateAuthAccountCommand;
import com.atlashub.auth.application.command.CreateVerificationCommand;
import com.atlashub.auth.application.usecase.CreateAuthAccountUseCase;
import com.atlashub.auth.application.usecase.CreateVerificationUseCase;
import com.atlashub.auth.domain.model.AuthProvider;
import com.atlashub.auth.domain.model.AuthStatus;
import com.atlashub.auth.domain.model.PrincipalType;
import com.atlashub.auth.domain.model.VerificationType;
import com.atlashub.shared.event.BaseKafkaEventListener;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class OrganizationRegisteredEventListener extends BaseKafkaEventListener {

    private final CreateAuthAccountUseCase createAuthAccountUseCase;
    private final CreateVerificationUseCase createVerificationUseCase;

    protected OrganizationRegisteredEventListener(
            ObjectMapper objectMapper,
            CreateAuthAccountUseCase createAuthAccountUseCase,
            CreateVerificationUseCase createVerificationUseCase) {
        super(objectMapper);
        this.createAuthAccountUseCase = createAuthAccountUseCase;
        this.createVerificationUseCase = createVerificationUseCase;
    }

    @KafkaListener(topics = "Organization-events", groupId = "auth-module-group")
    public void onOrganizationRegistered(String messagePayload) {
        processEventIfMatches(messagePayload, "OrganizationRegistered", log, root -> {
            String aggregateId = root.path("aggregateId").asText(null);
            if (aggregateId == null) return;

            Long OrganizationId = Long.valueOf(aggregateId);
            String email = root.path("payload").path("email").asText(null);

            log.info("Received OrganizationRegistered event for Organization {}. Creating auth account and triggering verification...", OrganizationId);

            CreateAuthAccountCommand authCommand = new CreateAuthAccountCommand(
                    OrganizationId,
                    PrincipalType.Organization,
                    email,
                    null, // No secondary identifier yet
                    AuthProvider.EMAIL,
                    null, // No password yet
                    "Organization:*",
                    AuthStatus.PENDING_EMAIL_VERIFICATION
            );

            createAuthAccountUseCase.execute(authCommand);

            CreateVerificationCommand verificationCommand = new CreateVerificationCommand(
                    OrganizationId,
                    email,
                    VerificationType.EMAIL_VERIFICATION
            );

            createVerificationUseCase.execute(verificationCommand);
        });
    }
}