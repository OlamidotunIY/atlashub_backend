package com.atlashub.eventbus.application.usecase;

import com.atlashub.eventbus.application.command.ReplayEventCommand;
import com.atlashub.eventbus.application.port.MessageBrokerPort;
import com.atlashub.eventbus.domain.model.OutboxMessage;
import com.atlashub.eventbus.domain.repository.OutboxMessageRepository;
import com.atlashub.shared.exception.NotFoundException;
import com.atlashub.shared.usecase.BaseUseCase;
import org.springframework.stereotype.Service;

@Service
public class ReplayEventUseCase extends BaseUseCase<ReplayEventCommand, Void> {

    private final OutboxMessageRepository outboxMessageRepository;
    private final MessageBrokerPort messageBrokerPort;

    public ReplayEventUseCase(OutboxMessageRepository outboxMessageRepository,
                              MessageBrokerPort messageBrokerPort) {
        this.outboxMessageRepository = outboxMessageRepository;
        this.messageBrokerPort = messageBrokerPort;
    }

    @Override
    public Void execute(ReplayEventCommand input) {
        OutboxMessage message = outboxMessageRepository.findById(input.eventId())
                .orElseThrow(() -> new NotFoundException(com.atlashub.eventbus.domain.exception.EventbusErrorCode.EVENT_NOT_FOUND, "Event with ID " + input.eventId() + " not found in outbox"));

        // Simply re-publish the exact same payload to the original topic.
        // The eventbus subscribers will pick it up, and if they previously failed, 
        // they will re-process it (because the event Tracker state for their consumer group is not SUCCESS).
        // If they already succeeded, the Idempotency check inside the listener will skip it.
        messageBrokerPort.send(message.getTopic(), message.getId(), message.getPayload());
        
        return null;
    }
}
