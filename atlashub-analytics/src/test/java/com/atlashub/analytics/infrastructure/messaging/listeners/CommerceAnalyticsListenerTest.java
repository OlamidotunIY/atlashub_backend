package com.atlashub.analytics.infrastructure.messaging.listeners;

import com.atlashub.analytics.application.command.RecordAnalyticsMetricEvent.RecordAnalyticsMetricEventHandler;
import com.atlashub.analytics.application.command.UpdateCustomerPerformanceProjection.UpdateCustomerPerformanceProjectionHandler;
import com.atlashub.analytics.application.command.UpdateDailySalesProjection.UpdateDailySalesProjectionHandler;
import com.atlashub.analytics.application.command.UpdateInventoryHealthProjection.UpdateInventoryHealthProjectionHandler;
import com.atlashub.analytics.application.command.UpdateProductDemandProjection.UpdateProductDemandProjectionHandler;
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

class CommerceAnalyticsListenerTest {

    private UpdateDailySalesProjectionHandler dailySalesHandler;
    private UpdateProductDemandProjectionHandler productDemandHandler;
    private UpdateCustomerPerformanceProjectionHandler customerPerformanceHandler;
    private UpdateInventoryHealthProjectionHandler inventoryHealthHandler;
    private RecordAnalyticsMetricEventHandler metricEventHandler;
    private EventTrackerPort eventTracker;
    private CommerceAnalyticsListener listener;

    @BeforeEach
    void setUp() {
        dailySalesHandler = mock(UpdateDailySalesProjectionHandler.class);
        productDemandHandler = mock(UpdateProductDemandProjectionHandler.class);
        customerPerformanceHandler = mock(UpdateCustomerPerformanceProjectionHandler.class);
        inventoryHealthHandler = mock(UpdateInventoryHealthProjectionHandler.class);
        metricEventHandler = mock(RecordAnalyticsMetricEventHandler.class);
        eventTracker = mock(EventTrackerPort.class);

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        listener = new CommerceAnalyticsListener(
                objectMapper,
                dailySalesHandler,
                productDemandHandler,
                customerPerformanceHandler,
                inventoryHealthHandler,
                metricEventHandler
        );
        ReflectionTestUtils.setField(listener, "EventTrackerPort", eventTracker);
        ReflectionTestUtils.setField(listener, "deadLetterRepository", mock(DeadLetterRepository.class));
    }

    @Test
    @DisplayName("Should parse PosSaleCompletedEvent and invoke matching command handlers")
    void shouldProcessPosSaleCompletedEvent() {
        when(eventTracker.isProcessed("evt-123", "analytics-commerce-group")).thenReturn(false);

        String json = """
                {
                  "eventType": "PosSaleCompletedEvent",
                  "correlationId": "evt-123",
                  "event": {
                    "eventId": "evt-123",
                    "organizationId": 100,
                    "environment": "live",
                    "outletId": 200,
                    "customerId": 300,
                    "productId": 400,
                    "quantity": 2,
                    "grossAmount": 2000.0,
                    "discountAmount": 100.0,
                    "taxAmount": 150.0,
                    "netAmount": 2050.0,
                    "paymentMethod": "CARD",
                    "saleDate": "2026-10-17"
                  }
                }
                """;

        listener.listen(json);

        verify(dailySalesHandler).execute(any());
        verify(productDemandHandler).execute(any());
        verify(customerPerformanceHandler).execute(any());
        verify(metricEventHandler).execute(any());
    }

    @Test
    @DisplayName("Should parse StockAdjustedEvent and invoke inventory health handler")
    void shouldProcessStockAdjustedEvent() {
        when(eventTracker.isProcessed("evt-456", "analytics-commerce-group")).thenReturn(false);

        String json = """
                {
                  "eventType": "StockAdjustedEvent",
                  "correlationId": "evt-456",
                  "event": {
                    "eventId": "evt-456",
                    "organizationId": 100,
                    "environment": "live",
                    "outletId": 200,
                    "productId": 400,
                    "currentStock": 50,
                    "reorderPoint": 10,
                    "coverageDays": 30,
                    "inventoryValue": 50000.0,
                    "deadStock": false
                  }
                }
                """;

        listener.listen(json);

        verify(inventoryHealthHandler).execute(any());
    }
}
