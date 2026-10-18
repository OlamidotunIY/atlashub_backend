package com.atlashub.pay.transfers.infrastructure.messaging.listeners;

import com.atlashub.pay.transfers.application.commands.InitiatePayout.InitiatePayoutCommand;
import com.atlashub.pay.transfers.application.commands.InitiatePayout.InitiatePayoutHandler;
import com.atlashub.pay.transfers.domain.valueobject.PaymentProvider;
import com.atlashub.pay.transfers.domain.valueobject.SourceSystem;
import com.atlashub.pay.transfers.infrastructure.messaging.events.ChargeRefundInitiatedKafkaEvent;
import com.atlashub.shared.application.messaging.BaseKafkaEventListener;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeoutException;

@Component
public class ChargeRefundInitiatedListener extends BaseKafkaEventListener {

    private static final Logger log = LoggerFactory.getLogger(ChargeRefundInitiatedListener.class);
    private static final String GROUP_ID = "pay-transfers-charge-refund";

    private final InitiatePayoutHandler initiatePayoutHandler;

    public ChargeRefundInitiatedListener(ObjectMapper objectMapper, InitiatePayoutHandler initiatePayoutHandler) {
        super(objectMapper);
        this.initiatePayoutHandler = initiatePayoutHandler;
    }

    @PostConstruct
    public void init() {
        registerSubscription("ChargeRefundInitiatedEvent", GROUP_ID);
    }

    @KafkaListener(topics = "pay-events", groupId = GROUP_ID)
    public void listen(String payload) {
        processEventIfMatches(
                payload,
                "ChargeRefundInitiatedEvent",
                ChargeRefundInitiatedKafkaEvent.class,
                log,
                GROUP_ID,
                error -> error instanceof TimeoutException,
                event -> {
                    String ref = "refund-" + event.payload().chargeReference();
                    initiatePayoutHandler.execute(new InitiatePayoutCommand(
                            event.payload().organizationId(),
                            1L,
                            event.payload().amount() != null ? event.payload().amount().amount() : null,
                            event.payload().amount() != null && event.payload().amount().currency() != null ? event.payload().amount().currency().name() : "NGN",
                            "044",
                            "0000000000",
                            "Customer Refund",
                            event.payload().reason() != null ? event.payload().reason() : "Charge refund",
                            ref,
                            PaymentProvider.PAYSTACK,
                            SourceSystem.COMMERCE_REFUND,
                            event.payload().chargeReference()
                    ));
                }
        );
    }
}
