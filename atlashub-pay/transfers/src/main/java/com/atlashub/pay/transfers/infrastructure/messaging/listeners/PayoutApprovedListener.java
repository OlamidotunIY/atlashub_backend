package com.atlashub.pay.transfers.infrastructure.messaging.listeners;

import com.atlashub.pay.transfers.application.commands.ExecutePayout.ExecutePayoutCommand;
import com.atlashub.pay.transfers.application.commands.ExecutePayout.ExecutePayoutHandler;
import com.atlashub.pay.transfers.infrastructure.messaging.events.PayoutApprovedKafkaEvent;
import com.atlashub.shared.application.messaging.BaseKafkaEventListener;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeoutException;

@Component
public class PayoutApprovedListener extends BaseKafkaEventListener {

    private static final Logger log = LoggerFactory.getLogger(PayoutApprovedListener.class);
    private static final String GROUP_ID = "pay-transfers-approved-group";

    private final ExecutePayoutHandler executePayoutHandler;

    public PayoutApprovedListener(ObjectMapper objectMapper, ExecutePayoutHandler executePayoutHandler) {
        super(objectMapper);
        this.executePayoutHandler = executePayoutHandler;
    }

    @PostConstruct
    public void init() {
        registerSubscription("PayoutApprovedEvent", GROUP_ID);
    }

    @KafkaListener(topics = "pay-events", groupId = GROUP_ID)
    public void listen(String payload) {
        processEventIfMatches(
                payload,
                "PayoutApprovedEvent",
                PayoutApprovedKafkaEvent.class,
                log,
                GROUP_ID,
                error -> error instanceof TimeoutException,
                event -> executePayoutHandler.execute(new ExecutePayoutCommand(
                        event.payload().payoutId()
                ))
        );
    }
}
