package com.atlashub.analytics.infrastructure.messaging.listeners;

import com.atlashub.analytics.application.command.RecordAnalyticsMetricEvent.RecordAnalyticsMetricEventHandler;
import com.atlashub.analytics.application.command.UpdateTransactionVolumeProjection.UpdateTransactionVolumeProjectionHandler;
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

class PayAnalyticsListenerTest {

    private UpdateTransactionVolumeProjectionHandler transactionVolumeHandler;
    private RecordAnalyticsMetricEventHandler metricEventHandler;
    private EventTrackerPort eventTracker;
    private PayAnalyticsListener listener;

    @BeforeEach
    void setUp() {
        transactionVolumeHandler = mock(UpdateTransactionVolumeProjectionHandler.class);
        metricEventHandler = mock(RecordAnalyticsMetricEventHandler.class);
        eventTracker = mock(EventTrackerPort.class);

        ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        listener = new PayAnalyticsListener(objectMapper, transactionVolumeHandler, metricEventHandler);
        ReflectionTestUtils.setField(listener, "EventTrackerPort", eventTracker);
        ReflectionTestUtils.setField(listener, "deadLetterRepository", mock(DeadLetterRepository.class));
    }

    @Test
    @DisplayName("Should project a nested ChargeSuccessfulEvent exactly once")
    void shouldProcessNestedChargeSuccessfulEvent() {
        when(eventTracker.isProcessed("corr-charge-1", "analytics-pay-group")).thenReturn(false);

        listener.listen("""
                {
                  "eventType": "ChargeSuccessfulEvent",
                  "correlationId": "corr-charge-1",
                  "event": {
                    "eventId": "evt-charge-1",
                    "aggregateId": 1,
                    "occurredAt": "2026-10-17T10:00:00Z",
                    "payload": {
                      "organizationId": 100,
                      "environment": "live",
                      "amount": {"amount": "2000.0000", "currency": "NGN"},
                      "providerFee": {"amount": "50.0000", "currency": "NGN"},
                      "succeededAt": "2026-10-17T10:00:00Z"
                    }
                  }
                }
                """);

        verify(transactionVolumeHandler).execute(any());
        verify(metricEventHandler).execute(any());
        verify(eventTracker).markSuccess("corr-charge-1", "analytics-pay-group");
    }

    @Test
    @DisplayName("Should record a ledger event without treating a transaction delta as a wallet balance")
    void shouldRecordLedgerEventWithoutWalletBalanceProjection() {
        when(eventTracker.isProcessed("corr-ledger-1", "analytics-pay-group")).thenReturn(false);

        listener.listen("""
                {
                  "eventType": "LedgerTransactionPostedEvent",
                  "correlationId": "corr-ledger-1",
                  "event": {
                    "eventId": "evt-ledger-1",
                    "aggregateId": 1,
                    "occurredAt": "2026-10-17T10:00:00Z",
                    "payload": {
                      "transactionId": 1,
                      "organizationId": 100,
                      "environment": "live",
                      "currency": "NGN",
                      "postedAt": "2026-10-17T10:00:00Z",
                      "entries": []
                    }
                  }
                }
                """);

        verify(metricEventHandler).execute(any());
    }
}
