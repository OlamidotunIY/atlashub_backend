package com.atlashub.charges.adapter.in.messaging;

import com.atlashub.charges.application.command.InitiateExternalChargeCommand;
import com.atlashub.charges.application.usecase.InitiateExternalChargeUseCase;
import com.atlashub.charges.domain.event.ExternalChargeRequestedEvent;
import com.atlashub.shared.adapter.in.messaging.BaseKafkaEventListener;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.retrytopic.DltStrategy;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Component;

@Component
public class ExternalChargeRequestedListener extends BaseKafkaEventListener {

    private static final Logger log = LoggerFactory.getLogger(ExternalChargeRequestedListener.class);
    private static final String GROUP_ID = "charges-module-external-group";

    private final InitiateExternalChargeUseCase useCase;

    public ExternalChargeRequestedListener(
            InitiateExternalChargeUseCase useCase,
            ObjectMapper objectMapper) {
        super(objectMapper);
        this.useCase = useCase;
    }

    @RetryableTopic(attempts = "3", backoff = @Backoff(delay = 1000, multiplier = 2.0), dltStrategy = DltStrategy.FAIL_ON_ERROR)
    @KafkaListener(topics = "billing-events", groupId = GROUP_ID)
    public void onExternalChargeRequested(String messagePayload) {
        processEventIfMatches(messagePayload, "ExternalChargeRequestedEvent", ExternalChargeRequestedEvent.class, log, GROUP_ID, event -> {
            var payload = event.payload();
            log.info("Received ExternalChargeRequestedEvent for invoice {}. Initiating Paystack charge...", payload.purposeId());

            InitiateExternalChargeCommand command = new InitiateExternalChargeCommand(
                    event.payload().purposeId(),
                    event.payload().organizationId(),
                    event.payload().amount(),
                    event.payload().customerEmail(),
                    event.payload().redirectUrl()
            );

            useCase.execute(command);
        });
    }
}
