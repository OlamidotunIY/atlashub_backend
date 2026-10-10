package com.atlashub.pay.txquery.infrastructure.messaging.listeners;

import com.atlashub.pay.txquery.application.commands.ProjectAccountFunding.ProjectAccountFundingCommand;
import com.atlashub.pay.txquery.application.commands.ProjectAccountFunding.ProjectAccountFundingHandler;
import com.atlashub.pay.txquery.infrastructure.messaging.events.TransactionInboundEvents.OrganizationAccountFundedEvent;
import com.atlashub.pay.txquery.infrastructure.messaging.events.TransactionInboundEvents.ReservedAccountFundedEvent;
import com.atlashub.shared.application.messaging.BaseKafkaEventListener;
import com.atlashub.shared.domain.valueobject.CurrencyCode;import com.atlashub.shared.domain.valueobject.Money;
import com.fasterxml.jackson.databind.ObjectMapper;import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;import org.slf4j.LoggerFactory;import org.springframework.kafka.annotation.KafkaListener;import org.springframework.stereotype.Component;
import java.util.concurrent.TimeoutException;

@Component
public class FundingTransactionListener extends BaseKafkaEventListener {
    private static final Logger log=LoggerFactory.getLogger(FundingTransactionListener.class);private static final String GROUP="pay-txquery-funding";
    private final ProjectAccountFundingHandler handler;public FundingTransactionListener(ObjectMapper m,ProjectAccountFundingHandler h){super(m);handler=h;}
    @PostConstruct public void init(){registerSubscription("OrganizationAccountFundedEvent",GROUP);registerSubscription("ReservedAccountFundedEvent",GROUP);}
    @KafkaListener(topics="pay-events",groupId=GROUP) public void receive(String message){
        processEventIfMatches(message,"OrganizationAccountFundedEvent",OrganizationAccountFundedEvent.class,log,GROUP,e->e instanceof TimeoutException,event->{var p=event.payload();handler.execute(new ProjectAccountFundingCommand(p.organizationId(),p.environment(),p.anchorTransferReference(),Money.of(p.amount(),CurrencyCode.valueOf(p.currency())),p.businessAccountId(),null,null,"ANCHOR",p.receivedAt()));});
        processEventIfMatches(message,"ReservedAccountFundedEvent",ReservedAccountFundedEvent.class,log,GROUP,e->e instanceof TimeoutException,event->{var p=event.payload();handler.execute(new ProjectAccountFundingCommand(p.organizationId(),p.environment(),p.anchorTransferReference(),Money.of(p.amount(),CurrencyCode.valueOf(p.currency())),p.reservedAccountId(),p.ownerType(),p.ownerReferenceId(),"ANCHOR",p.receivedAt()));});}
}
