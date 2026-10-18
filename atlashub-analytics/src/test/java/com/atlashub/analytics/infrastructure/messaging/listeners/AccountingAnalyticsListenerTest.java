package com.atlashub.analytics.infrastructure.messaging.listeners;

import com.atlashub.analytics.application.command.RecordAnalyticsMetricEvent.RecordAnalyticsMetricEventHandler;
import com.atlashub.analytics.application.command.UpdateFinancialHealthProjection.UpdateFinancialHealthProjectionHandler;
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

class AccountingAnalyticsListenerTest {

    private UpdateFinancialHealthProjectionHandler financialHealthHandler;
    private RecordAnalyticsMetricEventHandler metricEventHandler;
    private EventTrackerPort eventTracker;
    private AccountingAnalyticsListener listener;

    @BeforeEach
    void setUp() {
        financialHealthHandler = mock(UpdateFinancialHealthProjectionHandler.class);
        metricEventHandler = mock(RecordAnalyticsMetricEventHandler.class);
        eventTracker = mock(EventTrackerPort.class);

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        listener = new AccountingAnalyticsListener(
                objectMapper,
                financialHealthHandler,
                metricEventHandler
        );
        ReflectionTestUtils.setField(listener, "EventTrackerPort", eventTracker);
        ReflectionTestUtils.setField(listener, "deadLetterRepository", mock(DeadLetterRepository.class));
    }

    @Test
    @DisplayName("Should parse FinancialPeriodClosedEvent and invoke financial health handler")
    void shouldProcessFinancialPeriodClosedEvent() {
        when(eventTracker.isProcessed("evt-close-1", "analytics-accounting-group")).thenReturn(false);

        String json = """
                {
                  "eventType": "FinancialPeriodClosedEvent",
                  "correlationId": "evt-close-1",
                  "event": {
                    "eventId": "evt-close-1",
                    "organizationId": 100,
                    "environment": "live",
                    "periodEnd": "2026-09-30",
                    "revenue": 500000.0,
                    "costOfSales": 200000.0,
                    "operatingExpense": 100000.0,
                    "cashBalance": 750000.0,
                    "accountsReceivable": 50000.0,
                    "accountsPayable": 30000.0,
                    "reconciliationStatus": "RECONCILED"
                  }
                }
                """;

        listener.listen(json);

        verify(financialHealthHandler).execute(any());
        verify(metricEventHandler).execute(any());
    }
}
