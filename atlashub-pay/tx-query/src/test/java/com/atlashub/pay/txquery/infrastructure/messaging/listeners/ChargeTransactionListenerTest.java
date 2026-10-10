package com.atlashub.pay.txquery.infrastructure.messaging.listeners;

import com.atlashub.pay.txquery.application.commands.ProjectChargeTransaction.ProjectChargeTransactionCommand;
import com.atlashub.pay.txquery.application.commands.ProjectChargeTransaction.ProjectChargeTransactionHandler;
import com.atlashub.pay.txquery.application.commands.UpdateChargeTransactionStatus.UpdateChargeTransactionStatusHandler;
import com.atlashub.shared.application.port.EventTrackerPort;
import com.atlashub.shared.domain.valueobject.CurrencyCode;
import com.atlashub.shared.domain.valueobject.Money;
import com.atlashub.shared.infrastructure.persistence.repository.DeadLetterRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChargeTransactionListenerTest {
    @Mock private ProjectChargeTransactionHandler projectHandler;
    @Mock private UpdateChargeTransactionStatusHandler statusHandler;
    @Mock private EventTrackerPort tracker;
    @Mock private DeadLetterRepository deadLetters;

    @Test
    void projectsVerifiedChargeEventAndMarksItProcessed() throws Exception {
        ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());
        ChargeTransactionListener listener = new ChargeTransactionListener(mapper, projectHandler, statusHandler);
        ReflectionTestUtils.setField(listener, "EventTrackerPort", tracker);
        ReflectionTestUtils.setField(listener, "deadLetterRepository", deadLetters);
        when(tracker.isProcessed(anyString(), anyString())).thenReturn(false);
        String message = mapper.writeValueAsString(Map.of(
                "eventType", "ChargeSuccessfulEvent",
                "correlationId", "event-1",
                "event", Map.of(
                        "eventId", "event-1",
                        "aggregateId", 55L,
                        "occurredAt", "2026-10-10T10:00:00Z",
                        "correlationId", "correlation-1",
                        "payload", Map.ofEntries(
                                Map.entry("organizationId", 42L),
                                Map.entry("environment", "TEST"),
                                Map.entry("chargeReference", "CHG-1"),
                                Map.entry("gatewayReference", "PS-1"),
                                Map.entry("amount", Money.of(new BigDecimal("2500"), CurrencyCode.NGN)),
                                Map.entry("providerFee", Money.of(new BigDecimal("25"), CurrencyCode.NGN)),
                                Map.entry("channel", "CARD"),
                                Map.entry("provider", "PAYSTACK"),
                                Map.entry("sourceSystem", "COMMERCE"),
                                Map.entry("sourceReferenceId", "ORDER-1"),
                                Map.entry("customerReferenceId", "CUST-1"),
                                Map.entry("succeededAt", "2026-10-10T10:00:00Z")))));

        listener.receive(message);

        ArgumentCaptor<ProjectChargeTransactionCommand> command =
                ArgumentCaptor.forClass(ProjectChargeTransactionCommand.class);
        verify(projectHandler).execute(command.capture());
        assertEquals("CHG-1", command.getValue().reference());
        assertEquals("CUST-1", command.getValue().customerReferenceId());
        verify(tracker).markSuccess("event-1", "pay-txquery-charges");
    }
}
