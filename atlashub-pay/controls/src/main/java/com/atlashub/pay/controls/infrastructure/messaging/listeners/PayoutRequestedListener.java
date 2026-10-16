package com.atlashub.pay.controls.infrastructure.messaging.listeners;

import com.atlashub.pay.controls.application.commands.EvaluatePaymentInstruction.EvaluatePaymentInstructionCommand;
import com.atlashub.pay.controls.application.commands.EvaluatePaymentInstruction.EvaluatePaymentInstructionHandler;
import com.atlashub.pay.controls.domain.valueobject.PaymentContext;
import com.atlashub.pay.controls.domain.valueobject.PaymentRail;
import com.atlashub.pay.controls.infrastructure.messaging.events.PayoutRequestedKafkaEvent;
import com.atlashub.shared.application.messaging.BaseKafkaEventListener;
import com.atlashub.shared.domain.valueobject.CurrencyCode;
import com.atlashub.shared.domain.valueobject.Money;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeoutException;

@Component
public class PayoutRequestedListener extends BaseKafkaEventListener {

    private static final Logger log = LoggerFactory.getLogger(PayoutRequestedListener.class);
    private static final String GROUP_ID = "pay-controls-payout-requested";

    private final EvaluatePaymentInstructionHandler evaluatePaymentHandler;

    public PayoutRequestedListener(ObjectMapper objectMapper,
                                  EvaluatePaymentInstructionHandler evaluatePaymentHandler) {
        super(objectMapper);
        this.evaluatePaymentHandler = evaluatePaymentHandler;
    }

    @PostConstruct
    public void init() {
        registerSubscription("PayoutRequestedEvent", GROUP_ID);
    }

    @KafkaListener(topics = "transfer-events", groupId = GROUP_ID)
    public void listen(String payload) {
        processEventIfMatches(
                payload,
                "PayoutRequestedEvent",
                PayoutRequestedKafkaEvent.class,
                log,
                GROUP_ID,
                error -> error instanceof TimeoutException,
                event -> {
                    PayoutRequestedKafkaEvent.Payload p = event.payload();
                    if (p.facilityId() != null && p.reference() != null) {
                        String currency = p.currency() != null ? p.currency() : "NGN";
                        Money amount = p.amount() != null ? Money.of(p.amount(), CurrencyCode.valueOf(currency)) : null;

                        evaluatePaymentHandler.execute(new EvaluatePaymentInstructionCommand(
                                p.facilityId(),
                                p.organizationId(),
                                p.reference(),
                                PaymentRail.ATOMIC_TRANSFER,
                                PaymentContext.PAYOUT,
                                p.categoryCode(),
                                p.recipientAccountNumber(),
                                amount,
                                null
                        ));
                    }
                }
        );
    }
}
