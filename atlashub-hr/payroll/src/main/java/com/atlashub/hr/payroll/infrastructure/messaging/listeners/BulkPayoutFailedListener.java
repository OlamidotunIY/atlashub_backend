package com.atlashub.hr.payroll.infrastructure.messaging.listeners;

import com.atlashub.hr.payroll.application.command.RevertPayrollToApproved.RevertPayrollToApprovedCommand;
import com.atlashub.hr.payroll.application.command.RevertPayrollToApproved.RevertPayrollToApprovedHandler;
import com.atlashub.hr.payroll.infrastructure.messaging.events.BulkPayoutFailedKafkaEvent;
import com.atlashub.shared.application.messaging.BaseKafkaEventListener;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeoutException;

@Component
public class BulkPayoutFailedListener extends BaseKafkaEventListener {

    private static final Logger log = LoggerFactory.getLogger(BulkPayoutFailedListener.class);
    private static final String GROUP_ID = "hr-payroll-group";
    private final RevertPayrollToApprovedHandler handler;

    public BulkPayoutFailedListener(ObjectMapper objectMapper, RevertPayrollToApprovedHandler handler) {
        super(objectMapper);
        this.handler = handler;
    }

    @PostConstruct
    public void init() {
        registerSubscription("BulkPayoutFailedEvent", GROUP_ID);
    }

    @KafkaListener(topics = "pay-events", groupId = GROUP_ID)
    public void listen(String payload) {
        processEventIfMatches(
                payload,
                "BulkPayoutFailedEvent",
                BulkPayoutFailedKafkaEvent.class,
                log,
                GROUP_ID,
                error -> error instanceof TimeoutException,
                event -> handler.execute(new RevertPayrollToApprovedCommand(
                        event.payload().payrollRunId(),
                        event.payload().failureReason()
                ))
        );
    }
}
