package com.atlashub.analytics.infrastructure.messaging.listeners;

import com.atlashub.analytics.application.command.RecordAnalyticsMetricEvent.RecordAnalyticsMetricEventHandler;
import com.atlashub.analytics.application.command.UpdateDeliveryPerformanceProjection.UpdateDeliveryPerformanceProjectionHandler;
import com.atlashub.shared.application.port.EventTrackerPort;
import com.atlashub.shared.infrastructure.persistence.repository.DeadLetterRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class LogisticsAnalyticsListenerTest {

    private UpdateDeliveryPerformanceProjectionHandler deliveryHandler;
    private RecordAnalyticsMetricEventHandler metricEventHandler;
    private EventTrackerPort eventTracker;
    private LogisticsAnalyticsListener listener;

    @BeforeEach
    void setUp() {
        deliveryHandler = mock(UpdateDeliveryPerformanceProjectionHandler.class);
        metricEventHandler = mock(RecordAnalyticsMetricEventHandler.class);
        eventTracker = mock(EventTrackerPort.class);

        ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        listener = new LogisticsAnalyticsListener(objectMapper, deliveryHandler, metricEventHandler);
        ReflectionTestUtils.setField(listener, "EventTrackerPort", eventTracker);
        ReflectionTestUtils.setField(listener, "deadLetterRepository", mock(DeadLetterRepository.class));
    }

    @Test
    @DisplayName("Should project a nested ShipmentDeliveredEvent")
    void shouldProcessNestedShipmentDeliveredEvent() {
        when(eventTracker.isProcessed("corr-shipment-1", "analytics-logistics-group")).thenReturn(false);

        listener.listen("""
                {
                  "eventType": "ShipmentDeliveredEvent",
                  "correlationId": "corr-shipment-1",
                  "event": {
                    "eventId": "evt-shipment-1",
                    "aggregateId": 1,
                    "occurredAt": "2026-10-17T10:00:00Z",
                    "payload": {
                      "shipmentId": 1,
                      "organizationId": 100,
                      "deliveredAt": "2026-10-17T10:00:00Z"
                    }
                  }
                }
                """);

        verify(deliveryHandler).execute(any());
        verify(metricEventHandler).execute(any());
    }
}
