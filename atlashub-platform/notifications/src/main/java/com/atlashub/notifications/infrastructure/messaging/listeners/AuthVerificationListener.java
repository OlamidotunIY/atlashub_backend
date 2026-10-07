package com.atlashub.notifications.infrastructure.messaging.listeners;

import com.atlashub.notifications.application.command.SendRegistrationOtp.SendRegistrationOtpCommand;
import com.atlashub.notifications.application.command.SendRegistrationOtp.SendRegistrationOtpHandler;
import com.atlashub.shared.application.messaging.BaseKafkaEventListener;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;

@Slf4j
@Component("notificationsAuthVerificationListener")
public class AuthVerificationListener extends BaseKafkaEventListener {
    private static final String GROUP_ID = "notifications-auth-group";
    private final SendRegistrationOtpHandler handler;

    public AuthVerificationListener(ObjectMapper objectMapper, SendRegistrationOtpHandler handler) {
        super(objectMapper);
        this.handler = handler;
    }

    @PostConstruct
    void registerSubscriptions() {
        registerSubscription("OtpVerificationCreated", GROUP_ID);
    }

    @RetryableTopic(attempts = "4", backoff = @Backoff(delay = 5000, multiplier = 2.0))
    @KafkaListener(topics = "auth-events", groupId = GROUP_ID)
    public void onAuthEvent(String payload) {
        processEventIfMatches(payload, "OtpVerificationCreated", OtpVerificationCreatedEvent.class, log, GROUP_ID, exception -> true, event -> {
            if (!"email_verification".equals(event.payload().verificationType())) return;
            handler.execute(new SendRegistrationOtpCommand(event.payload().recipientEmail(), event.correlationId(), event.payload().expiresAt().toEpochSecond()));
        });
    }

    /**
     * Consumer-owned copy of the published auth event.
     */
    public record OtpVerificationCreatedEvent(String eventId, Long aggregateId, ZonedDateTime occurredAt,
                                              String correlationId, Payload payload) {
        public record Payload(String recipientEmail, String verificationType, ZonedDateTime expiresAt) {
        }
    }
}
