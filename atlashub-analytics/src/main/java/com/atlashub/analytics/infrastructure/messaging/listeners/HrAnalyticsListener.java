package com.atlashub.analytics.infrastructure.messaging.listeners;

import com.atlashub.analytics.application.command.RecordAnalyticsMetricEvent.RecordAnalyticsMetricEventCommand;
import com.atlashub.analytics.application.command.RecordAnalyticsMetricEvent.RecordAnalyticsMetricEventHandler;
import com.atlashub.analytics.application.command.UpdateHeadcountProjection.UpdateHeadcountProjectionCommand;
import com.atlashub.analytics.application.command.UpdateHeadcountProjection.UpdateHeadcountProjectionHandler;
import com.atlashub.analytics.application.command.UpdatePayrollCostProjection.UpdatePayrollCostProjectionCommand;
import com.atlashub.analytics.application.command.UpdatePayrollCostProjection.UpdatePayrollCostProjectionHandler;
import com.atlashub.analytics.infrastructure.messaging.events.PayrollDisbursedPayload;
import com.atlashub.shared.application.messaging.BaseKafkaEventListener;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
import java.util.concurrent.TimeoutException;

@Component
public class HrAnalyticsListener extends BaseKafkaEventListener {

    private static final Logger log = LoggerFactory.getLogger(HrAnalyticsListener.class);
    private static final String GROUP_ID = "analytics-hr-group";

    private final UpdatePayrollCostProjectionHandler payrollCostHandler;
    private final UpdateHeadcountProjectionHandler headcountHandler;
    private final RecordAnalyticsMetricEventHandler metricEventHandler;

    public HrAnalyticsListener(
            ObjectMapper objectMapper,
            UpdatePayrollCostProjectionHandler payrollCostHandler,
            UpdateHeadcountProjectionHandler headcountHandler,
            RecordAnalyticsMetricEventHandler metricEventHandler
    ) {
        super(objectMapper);
        this.payrollCostHandler = payrollCostHandler;
        this.headcountHandler = headcountHandler;
        this.metricEventHandler = metricEventHandler;
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
        processEventIfMatches(messagePayload, "PayrollDisbursedEvent", PayrollDisbursedPayload.class, log, GROUP_ID, e -> e instanceof TimeoutException, p -> {
            payrollCostHandler.execute(new UpdatePayrollCostProjectionCommand(
                    p.organizationId(),
                    p.environment(),
                    p.month(),
                    p.grossPayroll(),
                    p.deductions(),
                    p.netPayroll(),
                    p.headcount()
            ));

            metricEventHandler.execute(new RecordAnalyticsMetricEventCommand(
                    p.eventId(),
                    p.organizationId(),
                    p.environment(),
                    "PAYROLL_DISBURSED",
                    "hr",
                    p.netPayroll(),
                    "NGN",
                    "{}",
                    ZonedDateTime.now()
            ));
        });

        processEventIfMatches(messagePayload, "EmployeeOnboardedEvent", PayrollDisbursedPayload.class, log, GROUP_ID, e -> e instanceof TimeoutException, p -> {
            headcountHandler.execute(new UpdateHeadcountProjectionCommand(
                    p.organizationId(),
                    p.environment(),
                    "HIRED"
            ));
        });

        processEventIfMatches(messagePayload, "EmployeeTerminatedEvent", PayrollDisbursedPayload.class, log, GROUP_ID, e -> e instanceof TimeoutException, p -> {
            headcountHandler.execute(new UpdateHeadcountProjectionCommand(
                    p.organizationId(),
                    p.environment(),
                    "TERMINATED"
            ));
        });

        processEventIfMatches(messagePayload, "EmployeeSuspendedEvent", PayrollDisbursedPayload.class, log, GROUP_ID, e -> e instanceof TimeoutException, p -> {
            headcountHandler.execute(new UpdateHeadcountProjectionCommand(
                    p.organizationId(),
                    p.environment(),
                    "SUSPENDED"
            ));
        });
    }
}
