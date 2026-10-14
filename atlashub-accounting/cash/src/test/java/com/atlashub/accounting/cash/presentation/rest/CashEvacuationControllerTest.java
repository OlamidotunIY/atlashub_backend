package com.atlashub.accounting.cash.presentation.rest;

import com.atlashub.accounting.cash.application.command.ConfirmCashEvacuation.ConfirmCashEvacuationCommand;
import com.atlashub.accounting.cash.application.command.ConfirmCashEvacuation.ConfirmCashEvacuationHandler;
import com.atlashub.accounting.cash.application.command.RecordCashEvacuation.RecordCashEvacuationCommand;
import com.atlashub.accounting.cash.application.command.RecordCashEvacuation.RecordCashEvacuationHandler;
import com.atlashub.accounting.cash.presentation.dto.RecordCashEvacuationRequest;
import com.atlashub.accounting.cash.presentation.dto.RecordCashEvacuationResponse;
import com.atlashub.shared.application.dto.ApiResponse;
import com.atlashub.shared.application.security.AuthenticatedPrincipal;
import com.atlashub.shared.domain.valueobject.CurrencyCode;
import com.atlashub.shared.domain.valueobject.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CashEvacuationControllerTest {

    private RecordCashEvacuationHandler recordHandler;
    private ConfirmCashEvacuationHandler confirmHandler;
    private CashEvacuationController controller;

    private final AuthenticatedPrincipal principal = new AuthenticatedPrincipal(
            1001L, 10L, "TEST", "session-1", "token-1", ZonedDateTime.now());

    @BeforeEach
    void setUp() {
        recordHandler = mock(RecordCashEvacuationHandler.class);
        confirmHandler = mock(ConfirmCashEvacuationHandler.class);
        controller = new CashEvacuationController(recordHandler, confirmHandler);
    }

    @Test
    @DisplayName("Should record cash evacuation from till")
    void shouldRecordCashEvacuation() {
        when(recordHandler.execute(any())).thenReturn(500L);

        RecordCashEvacuationRequest request = new RecordCashEvacuationRequest(
                20L,
                30L,
                new BigDecimal("250000.00"),
                "NGN"
        );

        ResponseEntity<ApiResponse<RecordCashEvacuationResponse>> response = controller.recordEvacuation(principal, request);

        assertNotNull(response.getBody());
        assertEquals(500L, response.getBody().data().evacuationId());

        ArgumentCaptor<RecordCashEvacuationCommand> captor = ArgumentCaptor.forClass(RecordCashEvacuationCommand.class);
        verify(recordHandler).execute(captor.capture());

        RecordCashEvacuationCommand command = captor.getValue();
        assertEquals(10L, command.organizationId());
        assertEquals(20L, command.outletId());
        assertEquals(30L, command.tillId());
        assertEquals(Money.of(new BigDecimal("250000.00"), CurrencyCode.NGN), command.amount());
        assertEquals(1001L, command.evacuatedBy());
    }

    @Test
    @DisplayName("Should confirm cash evacuation")
    void shouldConfirmCashEvacuation() {
        ResponseEntity<ApiResponse<Void>> response = controller.confirmEvacuation(principal, 500L);

        assertNotNull(response.getBody());
        assertEquals("Cash evacuation confirmed", response.getBody().message());

        ArgumentCaptor<ConfirmCashEvacuationCommand> captor = ArgumentCaptor.forClass(ConfirmCashEvacuationCommand.class);
        verify(confirmHandler).execute(captor.capture());

        ConfirmCashEvacuationCommand command = captor.getValue();
        assertEquals(10L, command.organizationId());
        assertEquals(500L, command.evacuationId());
    }
}
