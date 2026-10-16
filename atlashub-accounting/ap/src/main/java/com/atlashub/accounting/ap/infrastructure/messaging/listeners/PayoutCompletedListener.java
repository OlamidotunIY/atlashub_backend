package com.atlashub.accounting.ap.infrastructure.messaging.listeners;

import com.atlashub.accounting.ap.application.command.MarkSupplierInvoicePaid.MarkSupplierInvoicePaidCommand;
import com.atlashub.accounting.ap.application.command.MarkSupplierInvoicePaid.MarkSupplierInvoicePaidHandler;
import com.atlashub.accounting.ap.infrastructure.messaging.events.ApInboundEvents.PayoutCompletedEvent;
import com.atlashub.shared.application.messaging.BaseKafkaEventListener;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeoutException;

@Component
public class PayoutCompletedListener extends BaseKafkaEventListener {

    private static final Logger log = LoggerFactory.getLogger(PayoutCompletedListener.class);
    private static final String GROUP_ID = "accounting-ap-payout-completed";

    private final MarkSupplierInvoicePaidHandler handler;

    public PayoutCompletedListener(ObjectMapper objectMapper, MarkSupplierInvoicePaidHandler handler) {
        super(objectMapper);
        this.handler = handler;
    }

    @PostConstruct
    public void init() {
        registerSubscription("PayoutCompletedEvent", GROUP_ID);
        registerSubscription("com.atlashub.pay.settlement.domain.events.PayoutCompletedEvent", GROUP_ID);
    }

    @KafkaListener(topics = "pay-events", groupId = GROUP_ID)
    public void onPayoutCompleted(String message) {
        processEventIfMatches(message, "PayoutCompletedEvent", PayoutCompletedEvent.class, log, GROUP_ID,
                error -> error instanceof TimeoutException,
                event -> {
                    var payload = event.payload();
                    if (payload != null && payload.invoiceId() != null) {
                        handler.execute(new MarkSupplierInvoicePaidCommand(
                                payload.invoiceId(),
                                payload.payoutReference(),
                                payload.paidAt()
                        ));
                    }
                });
    }
}
