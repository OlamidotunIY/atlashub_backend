package com.atlashub.analytics.infrastructure.messaging.listeners;

import com.atlashub.analytics.application.command.RecordAnalyticsMetricEvent.RecordAnalyticsMetricEventCommand;
import com.atlashub.analytics.application.command.RecordAnalyticsMetricEvent.RecordAnalyticsMetricEventHandler;
import com.atlashub.analytics.application.command.UpdateDailySalesProjection.UpdateDailySalesProjectionCommand;
import com.atlashub.analytics.application.command.UpdateDailySalesProjection.UpdateDailySalesProjectionHandler;
import com.atlashub.analytics.infrastructure.messaging.events.PosSaleCompletedEvent;
import com.atlashub.analytics.infrastructure.messaging.events.PosSaleRefundedEvent;
import com.atlashub.analytics.infrastructure.messaging.events.StockAdjustedEvent;
import com.atlashub.shared.application.messaging.BaseKafkaEventListener;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.concurrent.TimeoutException;

@Component
public class CommerceAnalyticsListener extends BaseKafkaEventListener {

    private static final Logger log = LoggerFactory.getLogger(CommerceAnalyticsListener.class);
    private static final String GROUP_ID = "analytics-commerce-group";

    private final UpdateDailySalesProjectionHandler dailySalesHandler;
    private final RecordAnalyticsMetricEventHandler metricEventHandler;

    public CommerceAnalyticsListener(
            ObjectMapper objectMapper,
            UpdateDailySalesProjectionHandler dailySalesHandler,
            RecordAnalyticsMetricEventHandler metricEventHandler
    ) {
        super(objectMapper);
        this.dailySalesHandler = dailySalesHandler;
        this.metricEventHandler = metricEventHandler;
    }

    @PostConstruct
    public void init() {
        registerSubscription("PosSaleCompletedEvent", GROUP_ID);
        registerSubscription("PosSaleRefundedEvent", GROUP_ID);
        registerSubscription("StockAdjustedEvent", GROUP_ID);
    }

    @KafkaListener(topics = "commerce-events", groupId = GROUP_ID)
    public void listen(String messagePayload) {
        processEventIfMatches(messagePayload, "PosSaleCompletedEvent", PosSaleCompletedEvent.class, log, GROUP_ID, e -> e instanceof TimeoutException, event -> {
            PosSaleCompletedEvent.Payload payload = event.payload();
            java.time.ZonedDateTime saleTimestamp = payload.completedAt() != null ? payload.completedAt() : event.occurredAt();
            dailySalesHandler.execute(new UpdateDailySalesProjectionCommand(
                    payload.organizationId(),
                    "live",
                    payload.outletId(),
                    saleTimestamp.toLocalDate(),
                    payload.totalNet(),
                    BigDecimal.ZERO,
                    BigDecimal.ZERO,
                    payload.totalNet(),
                    payload.paymentMethod(),
                    false
            ));

            metricEventHandler.execute(new RecordAnalyticsMetricEventCommand(
                    event.eventId(),
                    payload.organizationId(),
                    "live",
                    "POS_SALE_COMPLETED",
                    "commerce",
                    payload.totalNet(),
                    payload.currency(),
                    "{}",
                    saleTimestamp
            ));
        });

        processEventIfMatches(messagePayload, "PosSaleRefundedEvent", PosSaleRefundedEvent.class, log, GROUP_ID, e -> e instanceof TimeoutException, event -> {
            PosSaleRefundedEvent.Payload payload = event.payload();
            metricEventHandler.execute(new RecordAnalyticsMetricEventCommand(
                    event.eventId(),
                    payload.organizationId(),
                    "live",
                    "POS_SALE_REFUNDED",
                    "commerce",
                    payload.refundAmount(),
                    payload.currency(),
                    "{}",
                    payload.refundedAt() != null ? payload.refundedAt() : event.occurredAt()
            ));
        });

        processEventIfMatches(messagePayload, "StockAdjustedEvent", StockAdjustedEvent.class, log, GROUP_ID, e -> e instanceof TimeoutException, event -> {
            StockAdjustedEvent.Payload payload = event.payload();
            metricEventHandler.execute(new RecordAnalyticsMetricEventCommand(
                    event.eventId(),
                    payload.organizationId(),
                    "live",
                    "STOCK_ADJUSTED",
                    "commerce",
                    null,
                    null,
                    "{}",
                    payload.adjustedAt() != null ? payload.adjustedAt() : event.occurredAt()
            ));
        });
    }
}
