package com.atlashub.pay.mandates.infrastructure.messaging.listeners;

import com.atlashub.pay.mandates.application.commands.ExpireMandate.ExpireMandateCommand;
import com.atlashub.pay.mandates.application.commands.ExpireMandate.ExpireMandateHandler;
import com.atlashub.pay.mandates.infrastructure.messaging.events.ChargeFailedKafkaEvent;
import com.atlashub.shared.application.messaging.BaseKafkaEventListener;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeoutException;

@Component
public class MandateChargeFailedListener extends BaseKafkaEventListener {

    private static final Logger log = LoggerFactory.getLogger(MandateChargeFailedListener.class);
    private static final String GROUP_ID = "pay-mandates-charge-failed";

    private final ExpireMandateHandler expireMandateHandler;

    public MandateChargeFailedListener(ObjectMapper objectMapper, ExpireMandateHandler expireMandateHandler) {
        super(objectMapper);
        this.expireMandateHandler = expireMandateHandler;
    }

    @PostConstruct
    public void init() {
        registerSubscription("ChargeFailedEvent", GROUP_ID);
    }

    @KafkaListener(topics = "pay-events", groupId = GROUP_ID)
    public void listen(String payload) {
        processEventIfMatches(
                payload,
                "ChargeFailedEvent",
                ChargeFailedKafkaEvent.class,
                log,
                GROUP_ID,
                error -> error instanceof TimeoutException,
                event -> {
                    if ("MANDATE_DEBIT".equalsIgnoreCase(event.payload().sourceSystem())
                            && event.payload().sourceReferenceId() != null) {
                        String reason = event.payload().reason() != null ? event.payload().reason() : "";
                        boolean isCardInvalid = reason.toLowerCase().contains("expired")
                                || reason.toLowerCase().contains("invalid card")
                                || reason.toLowerCase().contains("lost card")
                                || reason.toLowerCase().contains("stolen card");

                        if (isCardInvalid) {
                            try {
                                Long mandateId = Long.parseLong(event.payload().sourceReferenceId());
                                expireMandateHandler.execute(new ExpireMandateCommand(mandateId, reason));
                            } catch (NumberFormatException e) {
                                log.warn("Invalid mandate ID in sourceReferenceId: {}", event.payload().sourceReferenceId());
                            }
                        } else {
                            log.info("Mandate charge failed with transient reason: {}. Skipping expiration.", reason);
                        }
                    }
                }
        );
    }
}
