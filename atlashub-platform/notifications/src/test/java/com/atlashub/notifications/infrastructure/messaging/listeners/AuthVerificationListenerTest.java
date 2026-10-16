package com.atlashub.notifications.infrastructure.messaging.listeners;

import com.atlashub.notifications.application.command.SendRegistrationOtp.SendRegistrationOtpCommand;
import com.atlashub.notifications.application.command.SendRegistrationOtp.SendRegistrationOtpHandler;
import com.atlashub.shared.application.port.EventTrackerPort;
import com.atlashub.shared.infrastructure.persistence.repository.DeadLetterRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class AuthVerificationListenerTest {

    private final SendRegistrationOtpHandler handler = mock(SendRegistrationOtpHandler.class);
    private final EventTrackerPort eventTracker = mock(EventTrackerPort.class);
    private AuthVerificationListener listener;

    @BeforeEach
    void setUp() {
        listener = new AuthVerificationListener(
                new ObjectMapper().findAndRegisterModules(),
                handler
        );
        ReflectionTestUtils.setField(listener, "EventTrackerPort", eventTracker);
        ReflectionTestUtils.setField(listener, "deadLetterRepository", mock(DeadLetterRepository.class));
    }

    @Test
    void forwardsDeviceAuthorizationOtpToTheEmailHandler() {
        when(eventTracker.isProcessed("delivery-1", "notifications-auth-group"))
                .thenReturn(false);

        listener.onAuthEvent(otpEvent("email_otp"));

        ArgumentCaptor<SendRegistrationOtpCommand> command =
                ArgumentCaptor.forClass(SendRegistrationOtpCommand.class);
        verify(handler).execute(command.capture());
        assertThat(command.getValue())
                .extracting(
                        SendRegistrationOtpCommand::recipientEmail,
                        SendRegistrationOtpCommand::correlationId,
                        SendRegistrationOtpCommand::expiresAtEpochSeconds
                )
                .containsExactly("person@example.com", "otp-correlation-1", 1_792_152_600L);
        verify(eventTracker).markSuccess("delivery-1", "notifications-auth-group");
    }

    @Test
    void ignoresOtpTypesThatDoNotUseEmailDelivery() {
        when(eventTracker.isProcessed("delivery-1", "notifications-auth-group"))
                .thenReturn(false);

        listener.onAuthEvent(otpEvent("password_reset"));

        verifyNoInteractions(handler);
    }

    private String otpEvent(String verificationType) {
        return """
                {
                  "eventType": "OtpVerificationCreated",
                  "correlationId": "delivery-1",
                  "event": {
                    "eventId": "event-1",
                    "aggregateId": 1,
                    "occurredAt": "2026-10-16T12:00:00Z",
                    "correlationId": "otp-correlation-1",
                    "payload": {
                      "recipientEmail": "person@example.com",
                      "verificationType": "%s",
                      "expiresAt": "2026-10-16T12:10:00Z"
                    }
                  }
                }
                """.formatted(verificationType);
    }
}
