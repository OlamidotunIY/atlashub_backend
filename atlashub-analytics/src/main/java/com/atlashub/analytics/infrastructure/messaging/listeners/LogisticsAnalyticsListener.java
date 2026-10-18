package com.atlashub.analytics.infrastructure.messaging.listeners;

import com.atlashub.analytics.application.command.RecordAnalyticsMetricEvent.RecordAnalyticsMetricEventCommand;
import com.atlashub.analytics.application.command.RecordAnalyticsMetricEvent.RecordAnalyticsMetricEventHandler;
import com.atlashub.analytics.application.command.UpdateDeliveryPerformanceProjection.UpdateDeliveryPerformanceProjectionCommand;
import com.atlashub.analytics.application.command.UpdateDeliveryPerformanceProjection.UpdateDeliveryPerformanceProjectionHandler;
import com.atlashub.analytics.infrastructure.messaging.events.ShipmentStatusPayload;
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
        processEventIfMatches(messagePayload, "ShipmentDeliveredEvent", ShipmentStatusPayload.class, log, GROUP_ID, e -> e instanceof TimeoutException, p -> {
            deliveryHandler.execute(new UpdateDeliveryPerformanceProjectionCommand(
                    p.organizationId(),
                    p.environment(),
                    p.month(),
                    true,
                    false,
                    p.deliveryMinutes()
            ));

            metricEventHandler.execute(new RecordAnalyticsMetricEventCommand(
                    p.eventId(),
                    p.organizationId(),
                    p.environment(),
                    "SHIPMENT_DELIVERED",
                    "logistics",
                    null,
                    null,
                    "{}",
                    ZonedDateTime.now()
            ));
        });

        processEventIfMatches(messagePayload, "ShipmentFailedEvent", ShipmentStatusPayload.class, log, GROUP_ID, e -> e instanceof TimeoutException, p -> {
            deliveryHandler.execute(new UpdateDeliveryPerformanceProjectionCommand(
                    p.organizationId(),
                    p.environment(),
                    p.month(),
                    false,
                    false,
                    null
            ));
        });
    }
}
