package com.atlashub.accounting.ar.infrastructure.messaging.listeners;

import com.atlashub.accounting.ar.application.command.CreateCustomerReceivable.CreateCustomerReceivableCommand;
import com.atlashub.accounting.ar.application.command.CreateCustomerReceivable.CreateCustomerReceivableHandler;
import com.atlashub.accounting.ar.infrastructure.messaging.events.ArInboundEvents.CreditSaleCompletedEvent;
import com.atlashub.shared.application.messaging.BaseKafkaEventListener;
import com.atlashub.shared.domain.valueobject.CurrencyCode;
import com.atlashub.shared.domain.valueobject.Money;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.concurrent.TimeoutException;

@Component
public class CreditSaleCompletedListener extends BaseKafkaEventListener {

    private static final Logger log = LoggerFactory.getLogger(CreditSaleCompletedListener.class);
    private static final String GROUP_ID = "accounting-ar-credit-sale";

    private final CreateCustomerReceivableHandler handler;

    public CreditSaleCompletedListener(ObjectMapper objectMapper, CreateCustomerReceivableHandler handler) {
        super(objectMapper);
        this.handler = handler;
    }

    @PostConstruct
    public void init() {
        registerSubscription("CreditSaleCompletedEvent", GROUP_ID);
        registerSubscription("com.atlashub.commerce.storefront.domain.events.CreditSaleCompletedEvent", GROUP_ID);
    }

    @KafkaListener(topics = "commerce-events", groupId = GROUP_ID)
    public void onCreditSaleCompleted(String message) {
        processEventIfMatches(message, "CreditSaleCompletedEvent", CreditSaleCompletedEvent.class, log, GROUP_ID,
                error -> error instanceof TimeoutException,
                event -> {
                    var payload = event.payload();
                    if (payload != null && payload.amount() != null && payload.amount().signum() > 0) {
                        CurrencyCode currency = (payload.currency() != null && !payload.currency().isBlank())
                                ? CurrencyCode.valueOf(payload.currency())
                                : CurrencyCode.NGN;
                        Money amount = Money.of(payload.amount(), currency);
                        LocalDate dueDate = payload.dueDate() != null ? payload.dueDate() : LocalDate.now().plusDays(30);

                        handler.execute(new CreateCustomerReceivableCommand(
                                payload.organizationId(),
                                payload.customerId(),
                                payload.salesOrderId(),
                                amount,
                                dueDate
                        ));
                    }
                });
    }
}
