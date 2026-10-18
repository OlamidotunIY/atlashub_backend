package com.atlashub.hr.payroll.infrastructure.messaging.listeners;

import com.atlashub.hr.payroll.application.command.MarkPayrollDisbursed.MarkPayrollDisbursedCommand;
import com.atlashub.hr.payroll.application.command.MarkPayrollDisbursed.MarkPayrollDisbursedHandler;
import com.atlashub.hr.payroll.infrastructure.messaging.events.BulkPayoutCompletedKafkaEvent;
import com.atlashub.shared.application.messaging.BaseKafkaEventListener;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeoutException;

@Component
public class BulkPayoutCompletedListener extends BaseKafkaEventListener {

    private static final Logger log = LoggerFactory.getLogger(BulkPayoutCompletedListener.class);
    private static final String GROUP_ID = "hr-payroll-group";
    private final MarkPayrollDisbursedHandler handler;

    public BulkPayoutCompletedListener(ObjectMapper objectMapper, MarkPayrollDisbursedHandler handler) {
        super(objectMapper);
        this.handler = handler;
    }

    @PostConstruct
    public void init() {
        registerSubscription("BulkPayoutCompletedEvent", GROUP_ID);
    }

    @KafkaListener(topics = "pay-events", groupId = GROUP_ID)
    public void listen(String payload) {
        processEventIfMatches(
                payload,
                "BulkPayoutCompletedEvent",
                BulkPayoutCompletedKafkaEvent.class,
                log,
                GROUP_ID,
                error -> error instanceof TimeoutException,
                event -> handler.execute(new MarkPayrollDisbursedCommand(
                        event.payload().payrollRunId()
                ))
        );
    }
}
