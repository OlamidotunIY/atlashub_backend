package com.atlashub.analytics.infrastructure.messaging.listeners;

import com.atlashub.analytics.application.command.RecordAnalyticsMetricEvent.RecordAnalyticsMetricEventHandler;
import com.atlashub.analytics.application.command.UpdateHeadcountProjection.UpdateHeadcountProjectionHandler;
import com.atlashub.analytics.application.command.UpdatePayrollCostProjection.UpdatePayrollCostProjectionHandler;
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

class HrAnalyticsListenerTest {

    private UpdateHeadcountProjectionHandler headcountHandler;
    private RecordAnalyticsMetricEventHandler metricEventHandler;
    private UpdatePayrollCostProjectionHandler payrollCostHandler;
    private EventTrackerPort eventTracker;
    private HrAnalyticsListener listener;

    @BeforeEach
    void setUp() {
        headcountHandler = mock(UpdateHeadcountProjectionHandler.class);
        metricEventHandler = mock(RecordAnalyticsMetricEventHandler.class);
        payrollCostHandler = mock(UpdatePayrollCostProjectionHandler.class);
        eventTracker = mock(EventTrackerPort.class);

        ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        listener = new HrAnalyticsListener(objectMapper, headcountHandler, metricEventHandler, payrollCostHandler);
        ReflectionTestUtils.setField(listener, "EventTrackerPort", eventTracker);
        ReflectionTestUtils.setField(listener, "deadLetterRepository", mock(DeadLetterRepository.class));
    }

    @Test
    @DisplayName("Should update headcount from a nested EmployeeOnboardedEvent")
    void shouldProcessNestedEmployeeOnboardedEvent() {
        when(eventTracker.isProcessed("corr-employee-1", "analytics-hr-group")).thenReturn(false);

        listener.listen("""
                {
                  "eventType": "EmployeeOnboardedEvent",
                  "correlationId": "corr-employee-1",
                  "event": {
                    "eventId": "evt-employee-1",
                    "aggregateId": 1,
                    "occurredAt": "2026-10-17T10:00:00Z",
                    "payload": {
                      "employeeId": 1,
                      "organizationId": 100
                    }
                  }
                }
                """);

        verify(headcountHandler).execute(any());
    }

    @Test
    @DisplayName("Should project producer-owned payroll totals and headcount")
    void shouldRecordNestedPayrollDisbursedEvent() {
        when(eventTracker.isProcessed("corr-payroll-1", "analytics-hr-group")).thenReturn(false);

        listener.listen("""
                {
                  "eventType": "PayrollDisbursedEvent",
                  "correlationId": "corr-payroll-1",
                  "event": {
                    "eventId": "evt-payroll-1",
                    "aggregateId": 1,
                    "occurredAt": "2026-10-17T10:00:00Z",
                    "payload": {
                      "payrollRunId": 1,
                      "organizationId": 100,
                      "period": "2026-10",
                      "totalAmount": 500000.0,
                      "totalGross": 625000.0,
                      "totalDeductions": 125000.0,
                      "totalNet": 500000.0,
                      "headcount": 8,
                      "currency": "NGN",
                      "disbursedAt": "2026-10-17T10:00:00Z"
                    }
                  }
                }
                """);

        verify(metricEventHandler).execute(any());
        verify(payrollCostHandler).execute(any());
    }
}
