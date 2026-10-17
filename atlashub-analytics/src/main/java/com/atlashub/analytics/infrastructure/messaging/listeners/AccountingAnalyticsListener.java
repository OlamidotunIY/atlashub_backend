package com.atlashub.analytics.infrastructure.messaging.listeners;

import com.atlashub.analytics.application.command.RecordAnalyticsMetricEvent.RecordAnalyticsMetricEventCommand;
import com.atlashub.analytics.application.command.RecordAnalyticsMetricEvent.RecordAnalyticsMetricEventHandler;
import com.atlashub.analytics.application.command.UpdateFinancialHealthProjection.UpdateFinancialHealthProjectionCommand;
import com.atlashub.analytics.application.command.UpdateFinancialHealthProjection.UpdateFinancialHealthProjectionHandler;
import com.atlashub.analytics.infrastructure.messaging.events.FinancialPeriodClosedPayload;
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
public class AccountingAnalyticsListener extends BaseKafkaEventListener {

    private static final Logger log = LoggerFactory.getLogger(AccountingAnalyticsListener.class);
    private static final String GROUP_ID = "analytics-accounting-group";

    private final UpdateFinancialHealthProjectionHandler financialHealthHandler;
    private final RecordAnalyticsMetricEventHandler metricEventHandler;

    public AccountingAnalyticsListener(
            ObjectMapper objectMapper,
            UpdateFinancialHealthProjectionHandler financialHealthHandler,
            RecordAnalyticsMetricEventHandler metricEventHandler
    ) {
        super(objectMapper);
        this.financialHealthHandler = financialHealthHandler;
        this.metricEventHandler = metricEventHandler;
    }

    @PostConstruct
    public void init() {
        registerSubscription("FinancialPeriodClosedEvent", GROUP_ID);
    }

    @KafkaListener(topics = "accounting-events", groupId = GROUP_ID)
    public void listen(String messagePayload) {
        processEventIfMatches(messagePayload, "FinancialPeriodClosedEvent", FinancialPeriodClosedPayload.class, log, GROUP_ID, e -> e instanceof TimeoutException, p -> {
            financialHealthHandler.execute(new UpdateFinancialHealthProjectionCommand(
                    p.organizationId(),
                    p.environment(),
                    p.periodEnd(),
                    p.revenue(),
                    p.costOfSales(),
                    p.operatingExpense(),
                    p.cashBalance(),
                    p.accountsReceivable(),
                    p.accountsPayable(),
                    p.reconciliationStatus()
            ));

            metricEventHandler.execute(new RecordAnalyticsMetricEventCommand(
                    p.eventId(),
                    p.organizationId(),
                    p.environment(),
                    "FINANCIAL_PERIOD_CLOSED",
                    "accounting",
                    p.revenue(),
                    "NGN",
                    "{}",
                    ZonedDateTime.now()
            ));
        });
    }
}
