package com.atlashub.pay.settlement.infrastructure.messaging.listeners;

import com.atlashub.pay.settlement.application.commands.ConfirmSettlement.ConfirmSettlementCommand;
import com.atlashub.pay.settlement.application.commands.ConfirmSettlement.ConfirmSettlementHandler;
import com.atlashub.pay.settlement.infrastructure.messaging.events.OrganizationAccountFundedEvent;
import com.atlashub.shared.application.messaging.BaseKafkaEventListener;
import com.atlashub.shared.application.security.ApiEnvironment;
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
public class AnchorFundingSettlementListener extends BaseKafkaEventListener {
    private static final Logger log = LoggerFactory.getLogger(AnchorFundingSettlementListener.class);
    private static final String GROUP_ID = "pay-settlement-anchor-funding";
    private final ConfirmSettlementHandler handler;
    public AnchorFundingSettlementListener(ObjectMapper mapper, ConfirmSettlementHandler handler) {
        super(mapper); this.handler = handler;
    }
    @PostConstruct public void init() { registerSubscription("OrganizationAccountFundedEvent", GROUP_ID); }
    @KafkaListener(topics = "pay-events", groupId = GROUP_ID)
    public void listen(String message) {
        processEventIfMatches(message, "OrganizationAccountFundedEvent", OrganizationAccountFundedEvent.class,
                log, GROUP_ID, error -> error instanceof TimeoutException, event -> handler.execute(
                        new ConfirmSettlementCommand(event.payload().organizationId(),
                                ApiEnvironment.parse(event.payload().environment()),
                                event.payload().businessAccountId(), event.payload().anchorTransferReference(),
                                new Money(event.payload().amount(), CurrencyCode.valueOf(event.payload().currency())),
                                event.payload().receivedAt())));
    }
}
