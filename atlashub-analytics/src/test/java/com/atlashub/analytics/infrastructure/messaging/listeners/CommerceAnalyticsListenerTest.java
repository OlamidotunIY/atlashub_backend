package com.atlashub.analytics.infrastructure.messaging.listeners;

import com.atlashub.analytics.application.command.RecordAnalyticsMetricEvent.RecordAnalyticsMetricEventHandler;
import com.atlashub.analytics.application.command.UpdateDailySalesProjection.UpdateDailySalesProjectionHandler;
import com.atlashub.analytics.application.command.UpdateCustomerPerformanceProjection.UpdateCustomerPerformanceProjectionHandler;
import com.atlashub.analytics.application.command.UpdateInventoryHealthProjection.UpdateInventoryHealthProjectionHandler;
import com.atlashub.analytics.application.command.UpdateOutletPerformanceProjection.UpdateOutletPerformanceProjectionHandler;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CommerceAnalyticsListenerTest {

    private UpdateDailySalesProjectionHandler dailySalesHandler;
    private RecordAnalyticsMetricEventHandler metricEventHandler;
    private UpdateProductDemandProjectionHandler productDemandHandler;
    private UpdateCustomerPerformanceProjectionHandler customerPerformanceHandler;
    private UpdateOutletPerformanceProjectionHandler outletPerformanceHandler;
    private UpdateInventoryHealthProjectionHandler inventoryHealthHandler;
    private EventTrackerPort eventTracker;
    private CommerceAnalyticsListener listener;

    @BeforeEach
    void setUp() {
        dailySalesHandler = mock(UpdateDailySalesProjectionHandler.class);
        metricEventHandler = mock(RecordAnalyticsMetricEventHandler.class);
        productDemandHandler = mock(UpdateProductDemandProjectionHandler.class);
        customerPerformanceHandler = mock(UpdateCustomerPerformanceProjectionHandler.class);
        outletPerformanceHandler = mock(UpdateOutletPerformanceProjectionHandler.class);
        inventoryHealthHandler = mock(UpdateInventoryHealthProjectionHandler.class);
        eventTracker = mock(EventTrackerPort.class);

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        listener = new CommerceAnalyticsListener(
                objectMapper,
                dailySalesHandler,
                metricEventHandler,
                productDemandHandler,
                customerPerformanceHandler,
                outletPerformanceHandler,
                inventoryHealthHandler
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
                    "aggregateId": 1,
                    "occurredAt": "2026-10-17T10:00:00Z",
                    "payload": {
                      "salesOrderId": 1,
                      "organizationId": 100,
                      "outletId": 200,
                      "customerId": 500,
                      "totalGross": 2200.0,
                      "totalDiscount": 200.0,
                      "totalTax": 50.0,
                      "totalNet": 2050.0,
                      "currency": "NGN",
                      "paymentMethod": "CARD",
                      "cashierId": 300,
                      "items": [{"productId": 400, "variantId": 401, "quantity": 2, "totalAmount": 2050.0}],
                      "completedAt": "2026-10-17T10:00:00Z"
                    }
                  }
                }
                """;

        listener.listen(json);

        verify(dailySalesHandler).execute(any());
        verify(productDemandHandler).execute(any());
        verify(customerPerformanceHandler).execute(any());
        verify(outletPerformanceHandler).execute(any());
        verify(metricEventHandler).execute(any());
    }

    @Test
    @DisplayName("Should record StockAdjustedEvent without inventing inventory-health fields")
    void shouldProcessStockAdjustedEvent() {
        when(eventTracker.isProcessed("evt-456", "analytics-commerce-group")).thenReturn(false);

        String json = """
                {
                  "eventType": "StockAdjustedEvent",
                  "correlationId": "evt-456",
                  "event": {
                    "eventId": "evt-456",
                    "aggregateId": 1,
                    "occurredAt": "2026-10-17T10:00:00Z",
                    "payload": {
                      "organizationId": 100,
                      "outletId": 200,
                      "productId": 400,
                      "previousQty": 40,
                      "newQty": 50,
                      "reorderLevel": 12,
                      "reason": "PURCHASE_RECEIPT",
                      "adjustedBy": 1,
                      "adjustedAt": "2026-10-17T10:00:00Z"
                    }
                  }
                }
                """;

        listener.listen(json);

        verify(metricEventHandler).execute(any());
        verify(inventoryHealthHandler).execute(any());
    }
}
