package com.atlashub.pay.txquery.infrastructure.messaging.listeners;

import com.atlashub.pay.txquery.application.commands.ProjectLedgerPosting.ProjectLedgerPostingCommand;
import com.atlashub.pay.txquery.application.commands.ProjectLedgerPosting.ProjectLedgerPostingHandler;
import com.atlashub.pay.txquery.infrastructure.messaging.events.TransactionInboundEvents.LedgerTransactionPostedEvent;
import com.atlashub.shared.application.messaging.BaseKafkaEventListener;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;import org.springframework.stereotype.Component;
import java.util.concurrent.TimeoutException;

@Component
public class LedgerPostingTransactionListener extends BaseKafkaEventListener {
    private static final Logger log=LoggerFactory.getLogger(LedgerPostingTransactionListener.class);
    private static final String GROUP="pay-txquery-ledger-posted"; private final ProjectLedgerPostingHandler handler;
    public LedgerPostingTransactionListener(ObjectMapper mapper,ProjectLedgerPostingHandler handler){super(mapper);this.handler=handler;}
    @PostConstruct public void init(){registerSubscription("LedgerTransactionPostedEvent",GROUP);}
    @KafkaListener(topics="pay-events",groupId=GROUP) public void receive(String message){processEventIfMatches(message,
            "LedgerTransactionPostedEvent",LedgerTransactionPostedEvent.class,log,GROUP,e->e instanceof TimeoutException,event->{var p=event.payload();
                handler.execute(new ProjectLedgerPostingCommand(p.organizationId(),p.environment(),p.reference(),p.sourceSystem(),
                        p.sourceReferenceId(),p.description(),p.currency(),p.postedAt(),p.entries().stream().map(x->
                        new ProjectLedgerPostingCommand.Entry(x.accountId(),x.entryType(),x.amount())).toList()));});}
}
