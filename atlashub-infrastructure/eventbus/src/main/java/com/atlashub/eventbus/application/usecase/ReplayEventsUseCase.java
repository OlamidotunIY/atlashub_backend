package com.atlashub.eventbus.application.usecase;

import com.atlashub.eventbus.application.command.ReplayEventsCommand;
import com.atlashub.eventbus.application.command.ReplayEventsResult;
import com.atlashub.eventbus.application.port.MessageBrokerPort;
import com.atlashub.eventbus.domain.model.OutboxMessage;
import com.atlashub.eventbus.domain.repository.OutboxMessageRepository;
import com.atlashub.shared.application.usecase.BaseUseCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReplayEventsUseCase extends BaseUseCase<ReplayEventsCommand, ReplayEventsResult> {
    private static final Logger log = LoggerFactory.getLogger(ReplayEventsUseCase.class);

    private final OutboxMessageRepository outboxMessageRepository;
    private final MessageBrokerPort messageBrokerPort;

    public ReplayEventsUseCase(OutboxMessageRepository outboxMessageRepository,
                               MessageBrokerPort messageBrokerPort) {
        this.outboxMessageRepository = outboxMessageRepository;
        this.messageBrokerPort = messageBrokerPort;
    }

    @Override
    @PreAuthorize("hasAuthority('eventbus:events:manage')")
    public ReplayEventsResult execute(ReplayEventsCommand input) {
        List<OutboxMessage> candidates = outboxMessageRepository.findReplayCandidates(
                input.topic(), input.from(), input.to(), input.limit());

        if (!input.dryRun()) {
            candidates.forEach(message -> messageBrokerPort.send(
                    message.getTopic(), message.getId(), message.getPayload()));
        }

        log.warn("Event replay requested topic={} from={} to={} limit={} matched={} replayed={} operator={}",
                input.topic(), input.from(), input.to(), input.limit(), candidates.size(),
                input.dryRun() ? 0 : candidates.size(), input.requestedBy());

        return new ReplayEventsResult(input.topic(), input.from(), input.to(), candidates.size(),
                input.dryRun() ? 0 : candidates.size(), input.dryRun());
    }
}
