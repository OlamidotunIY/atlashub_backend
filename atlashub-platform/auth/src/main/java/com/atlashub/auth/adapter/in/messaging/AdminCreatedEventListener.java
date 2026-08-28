package com.atlashub.auth.adapter.in.messaging;

import com.atlashub.admin.domain.event.AdminCreatedEvent;
import com.atlashub.auth.application.command.CreateAuthAccountCommand;
import com.atlashub.auth.application.usecase.CreateAuthAccountUseCase;
import com.atlashub.auth.domain.valueobject.AuthProvider;
import com.atlashub.auth.domain.valueobject.AuthStatus;
import com.atlashub.auth.domain.valueobject.PrincipalType;
import com.atlashub.shared.event.BaseKafkaEventListener;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.retrytopic.DltStrategy;
import org.springframework.retry.annotation.Backoff;

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

    @RetryableTopic(
            attempts = "3",
            backoff = @Backoff(delay = 1000, multiplier = 2.0),
            dltStrategy = DltStrategy.FAIL_ON_ERROR
    )
    @KafkaListener(topics = "admin-events", groupId = "auth-module-admin-group")
    public void onAdminCreated(String messagePayload) {
        processEventIfMatches(messagePayload, "AdminCreatedEvent", AdminCreatedEvent.class, log, "auth-module-admin-group", event -> {
            Long adminId = Long.valueOf(event.aggregateId());
            
            String username = event.payload().username();
            String email = event.payload().email();
            String rawEmployeeCode = event.payload().rawEmployeeCode();
            String role = event.payload().role();

            if (adminId == 0 || email == null || rawEmployeeCode == null) {
                log.warn("Missing required fields in AdminCreatedEvent. aggregateId={}, email={}, rawEmployeeCode={}", adminId, email, rawEmployeeCode);
                return;
            }

            log.info("Received AdminCreatedEvent for admin {}. Creating auth account...", username);

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
