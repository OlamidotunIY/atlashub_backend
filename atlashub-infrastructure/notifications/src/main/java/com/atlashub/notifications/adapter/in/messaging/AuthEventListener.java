package com.atlashub.notifications.adapter.in.messaging;

import com.atlashub.auth.domain.event.AuthPasswordSetupInitiatedEvent;
import com.atlashub.auth.domain.event.AuthNewDeviceLoginEvent;
import com.atlashub.auth.domain.event.AuthVerificationCreatedEvent;
import com.atlashub.notifications.application.port.EmailSenderPort;
import com.atlashub.notifications.application.usecase.SendVerificationEmailUseCase;
import com.atlashub.shared.adapter.out.external.dlq.DeadLetterRepository;
import com.atlashub.shared.api.AdminQueryApi;
import com.atlashub.shared.api.UserQueryApi;
import com.atlashub.shared.event.BaseKafkaEventListener;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.retrytopic.DltStrategy;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Component
public class AuthEventListener extends BaseKafkaEventListener {

    private static final Logger log = LoggerFactory.getLogger(AuthEventListener.class);

    private final SendVerificationEmailUseCase sendVerificationEmailUseCase;
    private final EmailSenderPort emailSenderPort;
    private final UserQueryApi userQueryApi;
    private final AdminQueryApi adminQueryApi;

    public AuthEventListener(
            SendVerificationEmailUseCase sendVerificationEmailUseCase,
            EmailSenderPort emailSenderPort,
            UserQueryApi userQueryApi,
            AdminQueryApi adminQueryApi,
            ObjectMapper objectMapper,
            DeadLetterRepository deadLetterRepository
    ) {
        super(objectMapper);
        this.sendVerificationEmailUseCase = sendVerificationEmailUseCase;
        this.emailSenderPort = emailSenderPort;
        this.userQueryApi = userQueryApi;
        this.adminQueryApi = adminQueryApi;
        this.deadLetterRepository = deadLetterRepository;
    }

    @RetryableTopic(
            attempts = "3",
            backoff = @Backoff(delay = 5000, multiplier = 2.0),
            dltStrategy = DltStrategy.FAIL_ON_ERROR
    )
    @KafkaListener(topics = "auth-events", groupId = "notifications-auth-group")
    public void handleAuthEvent(String message) {
        processEventIfMatches(message, "AuthVerificationCreatedEvent", AuthVerificationCreatedEvent.class, log, "notifications-auth-group", event -> {
            String email = event.payload().identifier();
            String rawCode = event.payload().rawCode();
            
            if (email != null && rawCode != null) {
                sendVerificationEmailUseCase.execute(new SendVerificationEmailUseCase.Input(email, rawCode));
                log.info("Handled VerificationCreated event for {}", email);
            }
        });

        processEventIfMatches(message, "AuthPasswordSetupInitiatedEvent", AuthPasswordSetupInitiatedEvent.class, log, "notifications-auth-group", event -> {
            String email = event.payload().identifier();
            String setupToken = event.payload().setupToken();
            
            if (email != null && setupToken != null) {
                emailSenderPort.sendUserSetupPasswordEmail(email, "User", setupToken);
                log.info("Handled PasswordSetupInitiated event for {}. Setup link sent.", email);
            }
        });

        processEventIfMatches(message, "AuthNewDeviceLoginEvent", AuthNewDeviceLoginEvent.class, log, "notifications-auth-group", event -> {
            if (event.payload().principalId() == null) {
                log.warn("AuthNewDeviceLoginEvent missing principalId. Cannot send login email.");
                return;
            }
            
            String email = null;
            String firstName = null;

            if ("ADMIN".equals(event.payload().principalType().name())) {
                Optional<AdminQueryApi.AdminSharedDto> adminOpt = adminQueryApi.getAdminById(event.payload().principalId());
                if (adminOpt.isPresent()) {
                    email = adminOpt.get().email();
                    firstName = adminOpt.get().username(); // Admin has no firstName, fallback to username
                }
            } else {
                Optional<UserQueryApi.UserSharedDto> userOpt = userQueryApi.getUserById(event.payload().principalId());
                if (userOpt.isPresent()) {
                    email = userOpt.get().email();
                    firstName = userOpt.get().firstName();
                }
            }

            if (email != null) {
                String loginTime = event.occurredAt().format(DateTimeFormatter.RFC_1123_DATE_TIME);
                emailSenderPort.sendLoginNotificationEmail(
                        email,
                        firstName,
                        event.payload().ipAddress(),
                        event.payload().userAgent(),
                        loginTime
                );
                log.info("Handled AuthNewDeviceLoginEvent for {}. Login notification email sent.", email);
            }
        });
    }
}