package com.atlashub.pay.transfers.infrastructure.messaging.listeners;

import com.atlashub.pay.transfers.application.commands.InitiatePayout.InitiatePayoutCommand;
import com.atlashub.pay.transfers.application.commands.InitiatePayout.InitiatePayoutHandler;
import com.atlashub.pay.transfers.domain.valueobject.PaymentProvider;
import com.atlashub.pay.transfers.domain.valueobject.SourceSystem;
import com.atlashub.pay.transfers.infrastructure.messaging.events.PayrollApprovedKafkaEvent;
import com.atlashub.shared.application.messaging.BaseKafkaEventListener;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeoutException;

@Component
public class PayrollApprovedListener extends BaseKafkaEventListener {

    private static final Logger log = LoggerFactory.getLogger(PayrollApprovedListener.class);
    private static final String GROUP_ID = "pay-transfers-payroll-approved";

    private final InitiatePayoutHandler initiatePayoutHandler;

    public PayrollApprovedListener(ObjectMapper objectMapper, InitiatePayoutHandler initiatePayoutHandler) {
        super(objectMapper);
        this.initiatePayoutHandler = initiatePayoutHandler;
    }

    @PostConstruct
    public void init() {
        registerSubscription("PayrollApprovedEvent", GROUP_ID);
    }

    @KafkaListener(topics = "hr-events", groupId = GROUP_ID)
    public void listen(String payload) {
        processEventIfMatches(
                payload,
                "PayrollApprovedEvent",
                PayrollApprovedKafkaEvent.class,
                log,
                GROUP_ID,
                error -> error instanceof TimeoutException,
                event -> {
                    String ref = "payroll-" + event.payload().payrollRunId();
                    initiatePayoutHandler.execute(new InitiatePayoutCommand(
                            event.payload().organizationId(),
                            event.payload().approvedByUserId(),
                            event.payload().totalAmount(),
                            event.payload().currency(),
                            "044",
                            "0000000000",
                            "Payroll Disbursement",
                            "Salary for period " + event.payload().period(),
                            ref,
                            PaymentProvider.PAYSTACK,
                            SourceSystem.PAYROLL,
                            String.valueOf(event.payload().payrollRunId())
                    ));
                }
        );
    }
}
