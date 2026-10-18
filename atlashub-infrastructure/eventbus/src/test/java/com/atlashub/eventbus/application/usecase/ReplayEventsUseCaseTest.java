package com.atlashub.eventbus.application.usecase;

import com.atlashub.eventbus.application.command.ReplayEventsCommand;
import com.atlashub.eventbus.application.port.MessageBrokerPort;
import com.atlashub.eventbus.domain.model.OutboxMessage;
import com.atlashub.eventbus.domain.repository.OutboxMessageRepository;
import com.atlashub.eventbus.domain.valueobject.OutboxStatus;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.prepost.PreAuthorize;

import java.time.ZonedDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ReplayEventsUseCaseTest {
    private final OutboxMessageRepository repository = mock(OutboxMessageRepository.class);
    private final MessageBrokerPort broker = mock(MessageBrokerPort.class);
    private final ReplayEventsUseCase useCase = new ReplayEventsUseCase(repository, broker);
    private final ZonedDateTime from = ZonedDateTime.parse("2026-10-01T00:00:00Z");
    private final ZonedDateTime to = ZonedDateTime.parse("2026-10-02T00:00:00Z");

    @Test
    void previews_candidates_without_publishing() {
        var event = event("event-1");
        when(repository.findReplayCandidates("commerce.sales", from, to, 100)).thenReturn(List.of(event));

        var result = useCase.execute(new ReplayEventsCommand("commerce.sales", from, to, 100, true, 42L));

        assertEquals(1, result.matched());
        assertEquals(0, result.replayed());
        verify(broker, never()).send(event.getTopic(), event.getId(), event.getPayload());
    }

    @Test
    void republishes_the_exact_stored_envelope() {
        var event = event("event-1");
        when(repository.findReplayCandidates("commerce.sales", from, to, 100)).thenReturn(List.of(event));

        var result = useCase.execute(new ReplayEventsCommand("commerce.sales", from, to, 100, false, 42L));

        assertEquals(1, result.replayed());
        verify(broker).send("commerce.sales", "event-1", "{\"eventId\":\"event-1\"}");
    }

    @Test
    void enforces_the_event_management_permission_at_the_handler_boundary() throws Exception {
        var annotation = ReplayEventsUseCase.class.getMethod("execute", ReplayEventsCommand.class)
                .getAnnotation(PreAuthorize.class);

        assertNotNull(annotation);
        assertEquals("hasAuthority('eventbus:events:manage')", annotation.value());
    }

    private OutboxMessage event(String id) {
        return new OutboxMessage(id, "commerce.sales", "{\"eventId\":\"event-1\"}", OutboxStatus.SENT,
                from.plusHours(1), from.plusHours(1));
    }
}
