package com.atlashub.eventbus.application.usecase;

import com.atlashub.eventbus.application.command.ProcessOutboxMessagesCommand;
import com.atlashub.eventbus.application.port.MessageBrokerPort;
import com.atlashub.eventbus.domain.model.OutboxMessage;
import com.atlashub.eventbus.domain.repository.OutboxMessageRepository;
import com.atlashub.shared.usecase.BaseUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProcessOutboxMessagesUseCase extends BaseUseCase<ProcessOutboxMessagesCommand, Void> {

    private static final Logger log = LoggerFactory.getLogger(ProcessOutboxMessagesUseCase.class);
    private final OutboxMessageRepository repository;
    private final MessageBrokerPort messageBrokerPort;

    public ProcessOutboxMessagesUseCase(OutboxMessageRepository repository, MessageBrokerPort messageBrokerPort) {
        this.repository = repository;
        this.messageBrokerPort = messageBrokerPort;
    }

    @Override
    @Transactional
    public Void execute(ProcessOutboxMessagesCommand command) {
        List<OutboxMessage> messages = repository.findPendingMessagesBatch(command.batchSize());
        
        if (messages.isEmpty()) {
            return null;
        }

        log.debug("Processing {} pending outbox messages", messages.size());

        for (OutboxMessage message : messages) {
            try {
                messageBrokerPort.send(message.getTopic(), message.getId(), message.getPayload());
                message.markAsSent();
            } catch (Exception e) {
                log.error("Failed to process outbox message {}", message.getId(), e);
                message.markAsFailed();
            }
        }

        repository.saveAll(messages);
        return null;
    }
}
