package com.atlashub.pay.txquery.infrastructure.messaging.listeners;

import com.atlashub.pay.txquery.application.commands.ProjectSettlementTransaction.ProjectSettlementTransactionCommand;
import com.atlashub.pay.txquery.application.commands.ProjectSettlementTransaction.ProjectSettlementTransactionHandler;
import com.atlashub.pay.txquery.domain.valueobject.TransactionStatus;
import com.atlashub.pay.txquery.infrastructure.messaging.events.TransactionInboundEvents.ProviderSettlementReceivedEvent;
import com.atlashub.shared.application.messaging.BaseKafkaEventListener;import com.fasterxml.jackson.databind.ObjectMapper;import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;import org.slf4j.LoggerFactory;import org.springframework.kafka.annotation.KafkaListener;import org.springframework.stereotype.Component;
import java.util.concurrent.TimeoutException;

@Component
public class SettlementTransactionListener extends BaseKafkaEventListener {private static final Logger log=LoggerFactory.getLogger(SettlementTransactionListener.class);private static final String GROUP="pay-txquery-settlement";private final ProjectSettlementTransactionHandler handler;
    public SettlementTransactionListener(ObjectMapper m,ProjectSettlementTransactionHandler h){super(m);handler=h;}@PostConstruct public void init(){registerSubscription("ProviderSettlementReceivedEvent",GROUP);}
    @KafkaListener(topics="pay-events",groupId=GROUP)public void receive(String message){processEventIfMatches(message,"ProviderSettlementReceivedEvent",ProviderSettlementReceivedEvent.class,log,GROUP,e->e instanceof TimeoutException,event->{var p=event.payload();handler.execute(new ProjectSettlementTransactionCommand(p.organizationId(),p.environment(),p.settlementReference(),p.amount(),p.provider(),TransactionStatus.SUCCESSFUL,p.settledAt()));});}}
