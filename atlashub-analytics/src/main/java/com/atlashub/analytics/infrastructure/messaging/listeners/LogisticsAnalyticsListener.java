package com.atlashub.analytics.infrastructure.messaging.listeners;

import com.atlashub.analytics.application.command.RecordAnalyticsMetricEvent.RecordAnalyticsMetricEventCommand;
import com.atlashub.analytics.application.command.RecordAnalyticsMetricEvent.RecordAnalyticsMetricEventHandler;
import com.atlashub.analytics.application.command.UpdateDeliveryPerformanceProjection.UpdateDeliveryPerformanceProjectionCommand;
import com.atlashub.analytics.application.command.UpdateDeliveryPerformanceProjection.UpdateDeliveryPerformanceProjectionHandler;
import com.atlashub.analytics.infrastructure.messaging.events.ShipmentDeliveredEvent;
import com.atlashub.analytics.infrastructure.messaging.events.ShipmentFailedEvent;
import com.atlashub.shared.application.messaging.BaseKafkaEventListener;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeoutException;

@Component
public class LogisticsAnalyticsListener extends BaseKafkaEventListener {

    private static final Logger log = LoggerFactory.getLogger(LogisticsAnalyticsListener.class);
    private static final String GROUP_ID = "analytics-logistics-group";

    private final UpdateDeliveryPerformanceProjectionHandler deliveryHandler;
    private final RecordAnalyticsMetricEventHandler metricEventHandler;

    public LogisticsAnalyticsListener(
            ObjectMapper objectMapper,
            UpdateDeliveryPerformanceProjectionHandler deliveryHandler,
            RecordAnalyticsMetricEventHandler metricEventHandler
    ) {
        super(objectMapper);
        this.deliveryHandler = deliveryHandler;
        this.metricEventHandler = metricEventHandler;
    }

    @PostConstruct
    public void init() {
        registerSubscription("ShipmentDeliveredEvent", GROUP_ID);
        registerSubscription("ShipmentFailedEvent", GROUP_ID);
    }

    @KafkaListener(topics = "logistics-events", groupId = GROUP_ID)
    public void listen(String messagePayload) {
        processEventIfMatches(messagePayload, "ShipmentDeliveredEvent", ShipmentDeliveredEvent.class, log, GROUP_ID, e -> e instanceof TimeoutException, event -> {
            ShipmentDeliveredEvent.Payload payload = event.payload();
            java.time.ZonedDateTime deliveryTimestamp = payload.deliveredAt() != null ? payload.deliveredAt() : event.occurredAt();
            deliveryHandler.execute(new UpdateDeliveryPerformanceProjectionCommand(
                    payload.organizationId(),
                    "live",
                    metricMonth(deliveryTimestamp),
                    true,
                    false,
                    null
            ));

            metricEventHandler.execute(new RecordAnalyticsMetricEventCommand(
                    event.eventId(),
                    payload.organizationId(),
                    "live",
                    "SHIPMENT_DELIVERED",
                    "logistics",
                    null,
                    null,
                    "{}",
                    deliveryTimestamp
            ));
        });

        processEventIfMatches(messagePayload, "ShipmentFailedEvent", ShipmentFailedEvent.class, log, GROUP_ID, e -> e instanceof TimeoutException, event -> {
            ShipmentFailedEvent.Payload payload = event.payload();
            java.time.ZonedDateTime failureTimestamp = payload.failedAt() != null ? payload.failedAt() : event.occurredAt();
            deliveryHandler.execute(new UpdateDeliveryPerformanceProjectionCommand(
                    payload.organizationId(),
                    "live",
                    metricMonth(failureTimestamp),
                    false,
                    false,
                    null
            ));
        });
    }

    private String metricMonth(java.time.ZonedDateTime timestamp) {
        return String.format("%04d-%02d", timestamp.getYear(), timestamp.getMonthValue());
    }
}
