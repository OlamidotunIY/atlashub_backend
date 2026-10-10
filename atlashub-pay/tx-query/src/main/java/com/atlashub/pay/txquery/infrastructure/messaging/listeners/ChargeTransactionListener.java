package com.atlashub.pay.txquery.infrastructure.messaging.listeners;

import com.atlashub.pay.txquery.application.commands.ProjectChargeTransaction.ProjectChargeTransactionCommand;
import com.atlashub.pay.txquery.application.commands.ProjectChargeTransaction.ProjectChargeTransactionHandler;
import com.atlashub.pay.txquery.application.commands.UpdateChargeTransactionStatus.UpdateChargeTransactionStatusCommand;
import com.atlashub.pay.txquery.application.commands.UpdateChargeTransactionStatus.UpdateChargeTransactionStatusHandler;
import com.atlashub.pay.txquery.domain.valueobject.TransactionStatus;
import com.atlashub.pay.txquery.infrastructure.messaging.events.TransactionInboundEvents.ChargeDisputeResolvedEvent;
import com.atlashub.pay.txquery.infrastructure.messaging.events.TransactionInboundEvents.ChargeDisputedEvent;
import com.atlashub.pay.txquery.infrastructure.messaging.events.TransactionInboundEvents.ChargeFailedEvent;
import com.atlashub.pay.txquery.infrastructure.messaging.events.TransactionInboundEvents.ChargeInitializedEvent;
import com.atlashub.pay.txquery.infrastructure.messaging.events.TransactionInboundEvents.ChargeRefundedEvent;
import com.atlashub.pay.txquery.infrastructure.messaging.events.TransactionInboundEvents.ChargeRefundInitiatedEvent;
import com.atlashub.pay.txquery.infrastructure.messaging.events.TransactionInboundEvents.ChargeStatePayload;
import com.atlashub.pay.txquery.infrastructure.messaging.events.TransactionInboundEvents.ChargeSuccessfulEvent;
import com.atlashub.shared.application.messaging.BaseKafkaEventListener;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeoutException;

@Component
public class ChargeTransactionListener extends BaseKafkaEventListener {
    private static final Logger log = LoggerFactory.getLogger(ChargeTransactionListener.class);
    private static final String GROUP = "pay-txquery-charges";
    private final ProjectChargeTransactionHandler projectHandler;
    private final UpdateChargeTransactionStatusHandler statusHandler;

    public ChargeTransactionListener(ObjectMapper mapper, ProjectChargeTransactionHandler projectHandler,
                                     UpdateChargeTransactionStatusHandler statusHandler) {
        super(mapper); this.projectHandler = projectHandler; this.statusHandler = statusHandler;
    }

    @PostConstruct public void init() {
        for (String type : new String[]{"ChargeInitializedEvent", "ChargeSuccessfulEvent", "ChargeFailedEvent",
                "ChargeRefundInitiatedEvent", "ChargeRefundedEvent", "ChargeDisputedEvent",
                "ChargeDisputeResolvedEvent"}) registerSubscription(type, GROUP);
    }

    @KafkaListener(topics = "pay-events", groupId = GROUP)
    public void receive(String message) {
        processEventIfMatches(message, "ChargeInitializedEvent", ChargeInitializedEvent.class, log, GROUP,
                e -> e instanceof TimeoutException, e -> {
                    var p=e.payload(); projectHandler.execute(new ProjectChargeTransactionCommand(p.organizationId(),
                            p.environment(),p.chargeReference(),p.amount(),null,p.channel(),p.provider(),p.sourceSystem(),
                            p.sourceReferenceId(),p.customerReferenceId(),TransactionStatus.PENDING,null,p.initializedAt()));
                });
        processEventIfMatches(message, "ChargeSuccessfulEvent", ChargeSuccessfulEvent.class, log, GROUP,
                e -> e instanceof TimeoutException, e -> {
                    var p=e.payload(); projectHandler.execute(new ProjectChargeTransactionCommand(p.organizationId(),
                            p.environment(),p.chargeReference(),p.amount(),p.providerFee(),p.channel(),p.provider(),
                            p.sourceSystem(),p.sourceReferenceId(),p.customerReferenceId(),TransactionStatus.SUCCESSFUL,
                            null,p.succeededAt()));
                });
        processEventIfMatches(message, "ChargeFailedEvent", ChargeFailedEvent.class, log, GROUP,
                e -> e instanceof TimeoutException, e -> {
                    var p=e.payload(); projectHandler.execute(new ProjectChargeTransactionCommand(p.organizationId(),
                            p.environment(),p.chargeReference(),p.amount(),null,p.channel(),p.provider(),p.sourceSystem(),
                            p.sourceReferenceId(),p.customerReferenceId(),TransactionStatus.FAILED,p.reason(),p.failedAt()));
                });
        update(message,"ChargeRefundInitiatedEvent",ChargeRefundInitiatedEvent.class,TransactionStatus.REFUND_PENDING);
        update(message,"ChargeRefundedEvent",ChargeRefundedEvent.class,TransactionStatus.REFUNDED);
        update(message,"ChargeDisputedEvent",ChargeDisputedEvent.class,TransactionStatus.DISPUTED);
        update(message,"ChargeDisputeResolvedEvent",ChargeDisputeResolvedEvent.class,TransactionStatus.SUCCESSFUL);
    }

    private <T> void update(String message,String type,Class<T> eventType,TransactionStatus status) {
        processEventIfMatches(message,type,eventType,log,GROUP,e -> e instanceof TimeoutException,event -> {
            ChargeStatePayload p = payload(event);
            statusHandler.execute(new UpdateChargeTransactionStatusCommand(p.organizationId(),p.environment(),
                    p.chargeReference(),status,p.reason(),p.occurredAt()));
        });
    }

    private ChargeStatePayload payload(Object event) {
        if(event instanceof ChargeRefundInitiatedEvent e)return e.payload();
        if(event instanceof ChargeRefundedEvent e)return e.payload();
        if(event instanceof ChargeDisputedEvent e)return e.payload();
        return ((ChargeDisputeResolvedEvent)event).payload();
    }
}
