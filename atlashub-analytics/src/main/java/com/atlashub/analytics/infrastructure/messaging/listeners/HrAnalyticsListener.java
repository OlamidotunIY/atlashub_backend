package com.atlashub.analytics.infrastructure.messaging.listeners;

import com.atlashub.analytics.application.command.RecordAnalyticsMetricEvent.RecordAnalyticsMetricEventCommand;
import com.atlashub.analytics.application.command.RecordAnalyticsMetricEvent.RecordAnalyticsMetricEventHandler;
import com.atlashub.analytics.application.command.UpdateHeadcountProjection.UpdateHeadcountProjectionCommand;
import com.atlashub.analytics.application.command.UpdateHeadcountProjection.UpdateHeadcountProjectionHandler;
import com.atlashub.analytics.application.command.UpdatePayrollCostProjection.UpdatePayrollCostProjectionCommand;
import com.atlashub.analytics.application.command.UpdatePayrollCostProjection.UpdatePayrollCostProjectionHandler;
import com.atlashub.analytics.infrastructure.messaging.events.EmployeeOnboardedEvent;
import com.atlashub.analytics.infrastructure.messaging.events.EmployeeSuspendedEvent;
import com.atlashub.analytics.infrastructure.messaging.events.EmployeeTerminatedEvent;
import com.atlashub.analytics.infrastructure.messaging.events.PayrollDisbursedEvent;
import com.atlashub.shared.application.messaging.BaseKafkaEventListener;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeoutException;

@Component
public class HrAnalyticsListener extends BaseKafkaEventListener {

    private static final Logger log = LoggerFactory.getLogger(HrAnalyticsListener.class);
    private static final String GROUP_ID = "analytics-hr-group";

    private final UpdateHeadcountProjectionHandler headcountHandler;
    private final RecordAnalyticsMetricEventHandler metricEventHandler;
    private final UpdatePayrollCostProjectionHandler payrollCostHandler;

    public HrAnalyticsListener(
            ObjectMapper objectMapper,
            UpdateHeadcountProjectionHandler headcountHandler,
            RecordAnalyticsMetricEventHandler metricEventHandler,
            UpdatePayrollCostProjectionHandler payrollCostHandler
    ) {
        super(objectMapper);
        this.headcountHandler = headcountHandler;
        this.metricEventHandler = metricEventHandler;
        this.payrollCostHandler = payrollCostHandler;
    }

    @PostConstruct
    public void init() {
        registerSubscription("PayrollDisbursedEvent", GROUP_ID);
        registerSubscription("EmployeeOnboardedEvent", GROUP_ID);
        registerSubscription("EmployeeTerminatedEvent", GROUP_ID);
        registerSubscription("EmployeeSuspendedEvent", GROUP_ID);
    }

    @KafkaListener(topics = "hr-events", groupId = GROUP_ID)
    public void listen(String messagePayload) {
        processEventIfMatches(messagePayload, "PayrollDisbursedEvent", PayrollDisbursedEvent.class, log, GROUP_ID, e -> e instanceof TimeoutException, event -> {
            PayrollDisbursedEvent.Payload payload = event.payload();
            if (payload.totalNet() != null) {
                payrollCostHandler.execute(new UpdatePayrollCostProjectionCommand(
                        payload.organizationId(), "live", payload.period(), payload.totalGross(),
                        payload.totalDeductions(), payload.totalNet(), payload.headcount()));
            }
            metricEventHandler.execute(new RecordAnalyticsMetricEventCommand(
                    event.eventId(),
                    payload.organizationId(),
                    "live",
                    "PAYROLL_DISBURSED",
                    "hr",
                    payload.totalNet() != null ? payload.totalNet() : payload.totalAmount(),
                    payload.currency(),
                    "{}",
                    payload.disbursedAt() != null ? payload.disbursedAt() : event.occurredAt()
            ));
        });

        processEventIfMatches(messagePayload, "EmployeeOnboardedEvent", EmployeeOnboardedEvent.class, log, GROUP_ID, e -> e instanceof TimeoutException, event -> {
            headcountHandler.execute(new UpdateHeadcountProjectionCommand(
                    event.payload().organizationId(),
                    "live",
                    "HIRED"
            ));
        });

        processEventIfMatches(messagePayload, "EmployeeTerminatedEvent", EmployeeTerminatedEvent.class, log, GROUP_ID, e -> e instanceof TimeoutException, event -> {
            headcountHandler.execute(new UpdateHeadcountProjectionCommand(
                    event.payload().organizationId(),
                    "live",
                    "TERMINATED"
            ));
        });

        processEventIfMatches(messagePayload, "EmployeeSuspendedEvent", EmployeeSuspendedEvent.class, log, GROUP_ID, e -> e instanceof TimeoutException, event -> {
            headcountHandler.execute(new UpdateHeadcountProjectionCommand(
                    event.payload().organizationId(),
                    "live",
                    "SUSPENDED"
            ));
        });
    }
}
