package com.atlashub.eventbus.application.usecase;

import com.atlashub.eventbus.application.command.ReplayEventCommand;
import com.atlashub.eventbus.application.port.MessageBrokerPort;
import com.atlashub.eventbus.domain.exception.EventbusErrorCode;
import com.atlashub.eventbus.domain.model.OutboxMessage;
import com.atlashub.eventbus.domain.repository.OutboxMessageRepository;
import com.atlashub.shared.domain.exception.NotFoundException;
import com.atlashub.shared.application.usecase.BaseUseCase;
import org.springframework.stereotype.Service;

import com.atlashub.shared.adapter.out.external.dlq.DeadLetterRepository;

@Service
public class ReplayEventUseCase extends BaseUseCase<ReplayEventCommand, Void> {

    private final OutboxMessageRepository outboxMessageRepository;
    private final MessageBrokerPort messageBrokerPort;
    private final DeadLetterRepository deadLetterRepository;

    public ReplayEventUseCase(OutboxMessageRepository outboxMessageRepository,
                              MessageBrokerPort messageBrokerPort,
                              DeadLetterRepository deadLetterRepository) {
        this.outboxMessageRepository = outboxMessageRepository;
        this.messageBrokerPort = messageBrokerPort;
        this.deadLetterRepository = deadLetterRepository;
    }

    @Override
    public Void execute(ReplayEventCommand input) {
        OutboxMessage message = outboxMessageRepository.findByEventId(input.eventId())
                .orElseThrow(() -> new NotFoundException(EventbusErrorCode.EVENT_NOT_FOUND, "Event with ID " + input.eventId() + " not found in outbox"));

        // Simply re-publish the exact same payload to the original topic.
        // The eventbus subscribers will pick it up, and if they previously failed, 
        // they will re-process it (because the event Tracker state for their consumer group is not SUCCESS).
        // If they already succeeded, the Idempotency check inside the listener will skip it.
        messageBrokerPort.send(message.getTopic(), message.getId(), message.getPayload());
        deadLetterRepository.deleteByPayloadContaining(input.eventId());
        
        return null;
    }
}
