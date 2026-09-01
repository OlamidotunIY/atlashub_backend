package com.atlashub.notifications.adapter.in.messaging.listener;

import com.atlashub.notifications.application.command.SendEmailCommand;
import com.atlashub.notifications.application.usecase.SendEmailUseCase;
import com.atlashub.shared.api.EventTrackerApi;
import com.atlashub.shared.event.EnvelopedDomainEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class InvitationEventConsumer {

    private final SendEmailUseCase sendEmailUseCase;
    private final EventTrackerApi eventTrackerApi;

    public InvitationEventConsumer(SendEmailUseCase sendEmailUseCase, EventTrackerApi eventTrackerApi) {
        this.sendEmailUseCase = sendEmailUseCase;
        this.eventTrackerApi = eventTrackerApi;
    }

    @KafkaListener(topics = "invitation-events", groupId = "notification-group")
    public void consume(EnvelopedDomainEvent<?> event) {
        String eventId = event.correlationId();
        if (eventTrackerApi.isProcessed(eventId, "notification-group")) {
            return;
        }

        String eventType = event.event().getClass().getSimpleName();
        if ("InvitationCreated".equals(eventType)) {
            // Placeholder: Extract email and token from event payload
            String email = "placeholder@example.com";
            String token = "TOKEN";
            String orgName = "Organization";
            
            String content = String.format("You have been invited to join %s. Click here to join: https://atlashub.com/join/%s", orgName, token);
            
            SendEmailCommand command = new SendEmailCommand(
                email,
                "Invitation to join " + orgName,
                content
            );
            sendEmailUseCase.execute(command);
            
            eventTrackerApi.markSuccess(eventId, "notification-group");
        } else {
            eventTrackerApi.markSuccess(eventId, "notification-group");
        }
    }
}
