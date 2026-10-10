package com.atlashub.pay.accounts.infrastructure.messaging.listeners;

import com.atlashub.pay.accounts.application.commands.ConfigurePaystackSettlementRoute.ConfigurePaystackSettlementRouteCommand;
import com.atlashub.pay.accounts.application.commands.ConfigurePaystackSettlementRoute.ConfigurePaystackSettlementRouteHandler;
import com.atlashub.pay.accounts.infrastructure.messaging.events.BusinessDepositAccountActivatedEvent;
import com.atlashub.shared.application.messaging.BaseKafkaEventListener;
import com.atlashub.shared.application.security.ApiEnvironment;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeoutException;

@Component
public class BusinessDepositAccountActivatedListener extends BaseKafkaEventListener {
    private static final Logger log = LoggerFactory.getLogger(BusinessDepositAccountActivatedListener.class);
    private static final String GROUP_ID = "pay-accounts-paystack-settlement-route";
    private final ConfigurePaystackSettlementRouteHandler handler;
    public BusinessDepositAccountActivatedListener(ObjectMapper mapper, ConfigurePaystackSettlementRouteHandler handler) {
        super(mapper); this.handler = handler;
    }
    @PostConstruct public void init() { registerSubscription("BusinessDepositAccountActivatedEvent", GROUP_ID); }
    @KafkaListener(topics = "pay-events", groupId = GROUP_ID)
    public void listen(String message) {
        processEventIfMatches(message, "BusinessDepositAccountActivatedEvent",
                BusinessDepositAccountActivatedEvent.class, log, GROUP_ID,
                error -> error instanceof TimeoutException, event -> handler.execute(
                        new ConfigurePaystackSettlementRouteCommand(event.payload().organizationId(),
                                ApiEnvironment.parse(event.payload().environment()))));
    }
}
