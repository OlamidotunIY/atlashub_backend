package com.atlashub.auth.adapter.in.messaging;

import com.atlashub.auth.application.command.CreateAuthAccountCommand;
import com.atlashub.auth.application.command.CreateVerificationCommand;
import com.atlashub.auth.application.command.ResendSetupTokenCommand;
import com.atlashub.auth.application.usecase.CreateAuthAccountUseCase;
import com.atlashub.auth.application.usecase.CreateVerificationUseCase;
import com.atlashub.auth.application.usecase.ResendSetupTokenUseCase;
import com.atlashub.auth.domain.valueobject.AuthProvider;
import com.atlashub.auth.domain.valueobject.AuthStatus;
import com.atlashub.auth.domain.valueobject.PrincipalType;
import com.atlashub.auth.domain.valueobject.VerificationType;
import com.atlashub.identity.domain.event.UserCreated;
import com.atlashub.shared.adapter.in.messaging.BaseKafkaEventListener;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.retrytopic.DltStrategy;
import org.springframework.retry.annotation.Backoff;

@Slf4j
@Component
public class UserCreatedEventListener extends BaseKafkaEventListener {

    private final CreateAuthAccountUseCase createAuthAccountUseCase;
    private final CreateVerificationUseCase createVerificationUseCase;
    private final ResendSetupTokenUseCase resendSetupTokenUseCase;

    protected UserCreatedEventListener(
            ObjectMapper objectMapper,
            CreateAuthAccountUseCase createAuthAccountUseCase,
            CreateVerificationUseCase createVerificationUseCase,
            ResendSetupTokenUseCase resendSetupTokenUseCase) {
        super(objectMapper);
        this.createAuthAccountUseCase = createAuthAccountUseCase;
        this.createVerificationUseCase = createVerificationUseCase;
        this.resendSetupTokenUseCase = resendSetupTokenUseCase;
    }

    @RetryableTopic(
            attempts = "3",
            backoff = @Backoff(delay = 1000, multiplier = 2.0),
            dltStrategy = DltStrategy.FAIL_ON_ERROR
    )
    @KafkaListener(topics = "user-events", groupId = "auth-module-group")
    public void onUserCreated(String messagePayload) {
        processEventIfMatches(messagePayload, "UserCreated", UserCreated.class, log, "auth-module-group", event -> {
            Long principalId = Long.valueOf(event.aggregateId());
            String email = event.payload().email();

            boolean isInvited = Boolean.TRUE.equals(event.payload().isInvited());

            log.info("Received UserCreated event for user id {} (isInvited={}). Creating auth account...", principalId, isInvited);

            AuthStatus initialStatus = isInvited ? AuthStatus.REQUIRES_PASSWORD_SETUP : AuthStatus.PENDING_EMAIL_VERIFICATION;

            createAuthAccountUseCase.execute(new CreateAuthAccountCommand(
                    principalId,
                    PrincipalType.USER,
                    email,
                    null,
                    AuthProvider.EMAIL,
                    null,
                    "User:*",
                    initialStatus
            ));

            if (isInvited) {
                resendSetupTokenUseCase.execute(new ResendSetupTokenCommand(email));
            } else {
                createVerificationUseCase.execute(new CreateVerificationCommand(
                        principalId,
                        email,
                        VerificationType.EMAIL_VERIFICATION
                ));
            }
        });
    }
}
