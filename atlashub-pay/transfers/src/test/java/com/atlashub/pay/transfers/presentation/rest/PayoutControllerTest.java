package com.atlashub.pay.transfers.presentation.rest;

import com.atlashub.pay.transfers.application.commands.ApprovePayout.ApprovePayoutCommand;
import com.atlashub.pay.transfers.application.commands.ApprovePayout.ApprovePayoutHandler;
import com.atlashub.pay.transfers.application.commands.InitiateBulkPayout.InitiateBulkPayoutCommand;
import com.atlashub.pay.transfers.application.commands.InitiateBulkPayout.InitiateBulkPayoutHandler;
import com.atlashub.pay.transfers.application.commands.InitiateBulkPayout.InitiateBulkPayoutResponse;
import com.atlashub.pay.transfers.application.commands.InitiatePayout.InitiatePayoutCommand;
import com.atlashub.pay.transfers.application.commands.InitiatePayout.InitiatePayoutHandler;
import com.atlashub.pay.transfers.application.commands.InitiatePayout.InitiatePayoutResponse;
import com.atlashub.pay.transfers.application.queries.GetPayout.GetPayoutHandler;
import com.atlashub.pay.transfers.application.queries.GetPayout.GetPayoutQuery;
import com.atlashub.pay.transfers.application.queries.GetPayout.PayoutResult;
import com.atlashub.pay.transfers.application.queries.ListPayouts.ListPayoutsHandler;
import com.atlashub.pay.transfers.application.queries.ListPayouts.ListPayoutsQuery;
import com.atlashub.pay.transfers.domain.valueobject.PaymentProvider;
import com.atlashub.pay.transfers.domain.valueobject.PayoutStatus;
import com.atlashub.pay.transfers.domain.valueobject.SourceSystem;
import com.atlashub.pay.transfers.presentation.dto.InitiateBulkPayoutRequest;
import com.atlashub.pay.transfers.presentation.dto.InitiatePayoutRequest;
import com.atlashub.shared.application.security.AuthenticatedPrincipal;
import com.atlashub.shared.domain.valueobject.PageResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PayoutControllerTest {

    @Mock
    private InitiatePayoutHandler initiatePayoutHandler;

    @Mock
    private InitiateBulkPayoutHandler initiateBulkPayoutHandler;

    @Mock
    private ApprovePayoutHandler approvePayoutHandler;

    @Mock
    private GetPayoutHandler getPayoutHandler;

    @Mock
    private ListPayoutsHandler listPayoutsHandler;

    private PayoutController controller;
    private AuthenticatedPrincipal principal;

    @BeforeEach
    void setUp() {
        controller = new PayoutController(
                initiatePayoutHandler,
                initiateBulkPayoutHandler,
                approvePayoutHandler,
                getPayoutHandler,
                listPayoutsHandler
        );
        principal = new AuthenticatedPrincipal(100L, 10L, "TEST", "sess-1", "tok-1", ZonedDateTime.now().plusHours(1));
    }

    @Test
    @DisplayName("POST /api/v1/pay/transfers should invoke InitiatePayoutHandler")
    void shouldInitiatePayout() {
        InitiatePayoutRequest request = new InitiatePayoutRequest(
                new BigDecimal("50000.00"), "NGN", "044", "0123456789",
                "John Doe", "Vendor", "REF-001", PaymentProvider.PAYSTACK,
                SourceSystem.MANUAL, "SRC-1"
        );
        InitiatePayoutResponse response = new InitiatePayoutResponse(1L, "REF-001", "APPROVED", false);
        when(initiatePayoutHandler.execute(any(InitiatePayoutCommand.class))).thenReturn(response);

        ResponseEntity<?> result = controller.initiatePayout(request, principal);

        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        verify(initiatePayoutHandler).execute(any(InitiatePayoutCommand.class));
    }

    @Test
    @DisplayName("POST /api/v1/pay/transfers/bulk should invoke InitiateBulkPayoutHandler")
    void shouldInitiateBulkPayout() {
        InitiateBulkPayoutRequest request = new InitiateBulkPayoutRequest(
                "BATCH-01", SourceSystem.PAYROLL, "RUN-1",
                List.of(new InitiateBulkPayoutRequest.BulkPayoutItemRequest(
                        "044", "0123456789", "Alice", new BigDecimal("50000.00"), "NGN", "Salary", "ITEM-1"
                ))
        );
        InitiateBulkPayoutResponse response = new InitiateBulkPayoutResponse("BATCH-01", 1, List.of(1L));
        when(initiateBulkPayoutHandler.execute(any(InitiateBulkPayoutCommand.class))).thenReturn(response);

        ResponseEntity<?> result = controller.initiateBulkPayout(request, principal);

        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        verify(initiateBulkPayoutHandler).execute(any(InitiateBulkPayoutCommand.class));
    }

    @Test
    @DisplayName("POST /api/v1/pay/transfers/{id}/approve should invoke ApprovePayoutHandler")
    void shouldApprovePayout() {
        ResponseEntity<?> result = controller.approvePayout(1L, principal);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        verify(approvePayoutHandler).execute(new ApprovePayoutCommand(1L, 100L));
    }

    @Test
    @DisplayName("GET /api/v1/pay/transfers should invoke ListPayoutsHandler")
    void shouldListPayouts() {
        PayoutResult item = new PayoutResult(
                1L, 10L, new BigDecimal("50000.00"), "NGN", "044", "0123456789",
                "John", "Narration", "APPROVED", "REF-001", "PAYSTACK", "PRV-1",
                "MANUAL", "SRC-1", 100L, 100L, ZonedDateTime.now(), null,
                ZonedDateTime.now(), null
        );
        when(listPayoutsHandler.execute(any(ListPayoutsQuery.class)))
                .thenReturn(new PageResult<>(List.of(item), 1, 20, 1L, 1));

        ResponseEntity<?> result = controller.listPayouts(PayoutStatus.APPROVED, 1, 20, principal);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
    }

    @Test
    @DisplayName("GET /api/v1/pay/transfers/{id} should invoke GetPayoutHandler")
    void shouldGetPayout() {
        PayoutResult item = new PayoutResult(
                1L, 10L, new BigDecimal("50000.00"), "NGN", "044", "0123456789",
                "John", "Narration", "APPROVED", "REF-001", "PAYSTACK", "PRV-1",
                "MANUAL", "SRC-1", 100L, 100L, ZonedDateTime.now(), null,
                ZonedDateTime.now(), null
        );
        when(getPayoutHandler.execute(any(GetPayoutQuery.class))).thenReturn(item);

        ResponseEntity<?> result = controller.getPayout(1L, principal);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
    }
}
