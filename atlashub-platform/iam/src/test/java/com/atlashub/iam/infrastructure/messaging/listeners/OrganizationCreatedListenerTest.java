package com.atlashub.iam.infrastructure.messaging.listeners;

import com.atlashub.iam.application.commands.InitializeOrganizationIam.InitializeOrganizationIamHandler;
import com.atlashub.shared.application.port.EventTrackerPort;
import com.atlashub.shared.infrastructure.persistence.repository.DeadLetterRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OrganizationCreatedListenerTest {

    @Test
    void delegates_an_organization_registered_event_to_the_initialization_handler() {
        InitializeOrganizationIamHandler handler = mock(InitializeOrganizationIamHandler.class);
        EventTrackerPort eventTracker = mock(EventTrackerPort.class);
        when(eventTracker.isProcessed("event-1", "iam-group")).thenReturn(false);

        OrganizationCreatedListener listener = new OrganizationCreatedListener(
                new ObjectMapper().findAndRegisterModules(), handler);
        ReflectionTestUtils.setField(listener, "EventTrackerPort", eventTracker);
        ReflectionTestUtils.setField(listener, "deadLetterRepository", mock(DeadLetterRepository.class));

        listener.listen("""
                {
                  "eventType": "OrganizationRegistered",
                  "correlationId": "event-1",
                  "event": {
                    "eventId": "event-1",
                    "aggregateId": 2,
                    "occurredAt": "2026-10-07T06:51:45Z",
                    "correlationId": "event-1",
                    "payload": { "ownerUserId": 1 }
                  }
                }
                """);

        verify(handler).execute(any());
        verify(eventTracker).markSuccess("event-1", "iam-group");
    }
}
