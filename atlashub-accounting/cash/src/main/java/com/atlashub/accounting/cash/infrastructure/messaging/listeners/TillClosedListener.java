package com.atlashub.accounting.cash.infrastructure.messaging.listeners;

import com.atlashub.accounting.cash.application.command.RecordCashEvacuation.RecordCashEvacuationCommand;
import com.atlashub.accounting.cash.application.command.RecordCashEvacuation.RecordCashEvacuationHandler;
import com.atlashub.accounting.cash.infrastructure.messaging.events.CashInboundEvents.TillClosedEvent;
import com.atlashub.shared.application.messaging.BaseKafkaEventListener;
import com.atlashub.shared.domain.valueobject.CurrencyCode;
import com.atlashub.shared.domain.valueobject.Money;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.concurrent.TimeoutException;

@Component
public class TillClosedListener extends BaseKafkaEventListener {

    private static final Logger log = LoggerFactory.getLogger(TillClosedListener.class);
    private static final String GROUP_ID = "accounting-till-closed";

    private final RecordCashEvacuationHandler handler;

    public TillClosedListener(ObjectMapper objectMapper, RecordCashEvacuationHandler handler) {
        super(objectMapper);
        this.handler = handler;
    }

    @PostConstruct
    public void init() {
        registerSubscription("TillClosedEvent", GROUP_ID);
        registerSubscription("com.atlashub.commerce.storefront.domain.events.TillClosedEvent", GROUP_ID);
    }

    @KafkaListener(topics = "commerce-events", groupId = GROUP_ID)
    public void onTillClosed(String message) {
        processEventIfMatches(message, "TillClosedEvent", TillClosedEvent.class, log, GROUP_ID,
                error -> error instanceof TimeoutException,
                event -> {
                    var payload = event.payload();
                    if (payload != null && payload.closingCash() != null && payload.closingCash().compareTo(BigDecimal.ZERO) > 0) {
                        CurrencyCode currency = (payload.currency() != null && !payload.currency().isBlank())
                                ? CurrencyCode.valueOf(payload.currency())
                                : CurrencyCode.NGN;
                        Money amount = Money.of(payload.closingCash(), currency);

                        handler.execute(new RecordCashEvacuationCommand(
                                payload.organizationId(),
                                payload.outletId(),
                                payload.tillSessionId(),
                                amount,
                                payload.cashierId()
                        ));
                    }
                });
    }
}
