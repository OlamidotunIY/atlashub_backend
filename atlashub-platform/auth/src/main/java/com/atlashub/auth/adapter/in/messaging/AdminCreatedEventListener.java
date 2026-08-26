package com.atlashub.auth.adapter.in.messaging;

import com.atlashub.auth.application.command.CreateAuthAccountCommand;
import com.atlashub.auth.application.usecase.CreateAuthAccountUseCase;
import com.atlashub.auth.domain.model.AuthProvider;
import com.atlashub.auth.domain.model.AuthStatus;
import com.atlashub.auth.domain.model.PrincipalType;
import com.atlashub.shared.event.BaseKafkaEventListener;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class AdminCreatedEventListener extends BaseKafkaEventListener {

    private final CreateAuthAccountUseCase createAuthAccountUseCase;

    protected AdminCreatedEventListener(
            ObjectMapper objectMapper,
            CreateAuthAccountUseCase createAuthAccountUseCase) {
        super(objectMapper);
        this.createAuthAccountUseCase = createAuthAccountUseCase;
    }

    @KafkaListener(topics = "admin-events", groupId = "auth-module-admin-group")
    public void onAdminCreated(String messagePayload) {
        processEventIfMatches(messagePayload, "AdminCreatedEvent", log, root -> {
            Long adminId = root.path("adminId").asLong();
            String username = root.path("username").asText(null);
            String email = root.path("email").asText(null);
            String rawEmployeeCode = root.path("rawEmployeeCode").asText(null);
            String role = root.path("role").asText(null);

            if (adminId == null || email == null || rawEmployeeCode == null) return;

            log.info("Received AdminCreatedEvent for admin {}. Creating auth account...", adminId);

            String scope = "MASTER".equals(role) ? "admin:all" : "admin:standard";

            // Admins log in using their email and the raw employee code as the password.
            // Identifier = email
            CreateAuthAccountCommand authCommand = new CreateAuthAccountCommand(
                    adminId,
                    PrincipalType.ADMIN,
                    email,
                    null,
                    AuthProvider.EMAIL,
                    rawEmployeeCode,
                    scope,
                    AuthStatus.ACTIVE // Admins don't need to change their employee code immediately
            );

            createAuthAccountUseCase.execute(authCommand);
        });
    }
}