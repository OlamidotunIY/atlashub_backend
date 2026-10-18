package com.atlashub.analytics.infrastructure.messaging.listeners;

import com.atlashub.analytics.application.command.RecordAnalyticsMetricEvent.RecordAnalyticsMetricEventCommand;
import com.atlashub.analytics.application.command.RecordAnalyticsMetricEvent.RecordAnalyticsMetricEventHandler;
import com.atlashub.analytics.application.command.UpdateTransactionVolumeProjection.UpdateTransactionVolumeProjectionCommand;
import com.atlashub.analytics.application.command.UpdateTransactionVolumeProjection.UpdateTransactionVolumeProjectionHandler;
import com.atlashub.analytics.infrastructure.messaging.events.ChargeFailedEvent;
import com.atlashub.analytics.infrastructure.messaging.events.ChargeSuccessfulEvent;
import com.atlashub.analytics.infrastructure.messaging.events.LedgerTransactionPostedEvent;
import com.atlashub.analytics.infrastructure.messaging.events.PayoutCompletedEvent;
import com.atlashub.shared.application.messaging.BaseKafkaEventListener;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeoutException;

@Component
public class PayAnalyticsListener extends BaseKafkaEventListener {

    private static final Logger log = LoggerFactory.getLogger(PayAnalyticsListener.class);
    private static final String GROUP_ID = "analytics-pay-group";

    private final UpdateTransactionVolumeProjectionHandler txVolumeHandler;
    private final RecordAnalyticsMetricEventHandler metricEventHandler;

    public PayAnalyticsListener(
            ObjectMapper objectMapper,
            UpdateTransactionVolumeProjectionHandler txVolumeHandler,
            RecordAnalyticsMetricEventHandler metricEventHandler
    ) {
        super(objectMapper);
        this.txVolumeHandler = txVolumeHandler;
        this.metricEventHandler = metricEventHandler;
    }

    @PostConstruct
    public void init() {
        registerSubscription("ChargeSuccessfulEvent", GROUP_ID);
        registerSubscription("ChargeFailedEvent", GROUP_ID);
        registerSubscription("PayoutCompletedEvent", GROUP_ID);
        registerSubscription("LedgerTransactionPostedEvent", GROUP_ID);
    }

    @KafkaListener(topics = "pay-events", groupId = GROUP_ID)
    public void listen(String messagePayload) {
        processEventIfMatches(messagePayload, "ChargeSuccessfulEvent", ChargeSuccessfulEvent.class, log, GROUP_ID, e -> e instanceof TimeoutException, event -> {
            ChargeSuccessfulEvent.Payload payload = event.payload();
            txVolumeHandler.execute(new UpdateTransactionVolumeProjectionCommand(
                    payload.organizationId(),
                    payload.environment(),
                    metricMonth(payload.succeededAt(), event.occurredAt()),
                    true,
                    true,
                    false,
                    payload.amount().amount(),
                    null
            ));

            metricEventHandler.execute(new RecordAnalyticsMetricEventCommand(
                    event.eventId(),
                    payload.organizationId(),
                    payload.environment(),
                    "CHARGE_SUCCESSFUL",
                    "pay",
                    payload.amount().amount(),
                    payload.amount().currency().name(),
                    "{}",
                    payload.succeededAt() != null ? payload.succeededAt() : event.occurredAt()
            ));
        });

        processEventIfMatches(messagePayload, "ChargeFailedEvent", ChargeFailedEvent.class, log, GROUP_ID, e -> e instanceof TimeoutException, event -> {
            ChargeFailedEvent.Payload payload = event.payload();
            txVolumeHandler.execute(new UpdateTransactionVolumeProjectionCommand(
                    payload.organizationId(),
                    payload.environment(),
                    metricMonth(payload.failedAt(), event.occurredAt()),
                    true,
                    false,
                    false,
                    payload.amount().amount(),
                    null
            ));
        });

        processEventIfMatches(messagePayload, "PayoutCompletedEvent", PayoutCompletedEvent.class, log, GROUP_ID, e -> e instanceof TimeoutException, event -> {
            PayoutCompletedEvent.Payload payload = event.payload();
            txVolumeHandler.execute(new UpdateTransactionVolumeProjectionCommand(
                    payload.organizationId(),
                    "live",
                    metricMonth(payload.completedAt(), event.occurredAt()),
                    false,
                    true,
                    true,
                    payload.amount(),
                    null
            ));
        });

        processEventIfMatches(messagePayload, "LedgerTransactionPostedEvent", LedgerTransactionPostedEvent.class, log, GROUP_ID, e -> e instanceof TimeoutException, event -> {
            LedgerTransactionPostedEvent.Payload payload = event.payload();
            metricEventHandler.execute(new RecordAnalyticsMetricEventCommand(
                    event.eventId(),
                    payload.organizationId(),
                    payload.environment(),
                    "LEDGER_TRANSACTION_POSTED",
                    "pay",
                    null,
                    payload.currency(),
                    "{}",
                    payload.postedAt() != null ? payload.postedAt() : event.occurredAt()
            ));
        });
    }

    private String metricMonth(java.time.ZonedDateTime primaryTimestamp, java.time.ZonedDateTime fallbackTimestamp) {
        java.time.ZonedDateTime timestamp = primaryTimestamp != null ? primaryTimestamp : fallbackTimestamp;
        return String.format("%04d-%02d", timestamp.getYear(), timestamp.getMonthValue());
    }
}
