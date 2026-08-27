package com.atlashub.auth.adapter.in.messaging;

import com.atlashub.auth.adapter.in.messaging.events.UserCreatedEvent;
import com.atlashub.auth.application.command.CreateAuthAccountCommand;
import com.atlashub.auth.application.command.CreateVerificationCommand;
import com.atlashub.auth.application.usecase.CreateAuthAccountUseCase;
import com.atlashub.auth.application.usecase.CreateVerificationUseCase;
import com.atlashub.auth.domain.valueobject.AuthProvider;
import com.atlashub.auth.domain.valueobject.AuthStatus;
import com.atlashub.auth.domain.valueobject.PrincipalType;
import com.atlashub.auth.domain.valueobject.VerificationType;
import com.atlashub.shared.event.BaseKafkaEventListener;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class UserCreatedEventListener extends BaseKafkaEventListener {

    private final CreateAuthAccountUseCase createAuthAccountUseCase;
    private final CreateVerificationUseCase createVerificationUseCase;

    protected UserCreatedEventListener(
            ObjectMapper objectMapper,
            CreateAuthAccountUseCase createAuthAccountUseCase,
            CreateVerificationUseCase createVerificationUseCase) {
        super(objectMapper);
        this.createAuthAccountUseCase = createAuthAccountUseCase;
        this.createVerificationUseCase = createVerificationUseCase;
    }

    @KafkaListener(topics = "User-events", groupId = "auth-module-group")
    public void onUserCreated(String messagePayload) {
        processEventIfMatches(messagePayload, "UserCreated", UserCreatedEvent.class, log, event -> {
            Long principalId = Long.valueOf(event.aggregateId());
            String email = event.payload().email();

            log.info("Received UserCreated event for user id {}. Creating auth account and triggering email verification...", principalId);

            createAuthAccountUseCase.execute(new CreateAuthAccountCommand(
                    principalId,
                    PrincipalType.USER,
                    email,
                    null,
                    AuthProvider.EMAIL,
                    null,
                    "User:*",
                    AuthStatus.PENDING_EMAIL_VERIFICATION
            ));

            createVerificationUseCase.execute(new CreateVerificationCommand(
                    principalId,
                    email,
                    VerificationType.EMAIL_VERIFICATION
            ));
        });
    }
}
