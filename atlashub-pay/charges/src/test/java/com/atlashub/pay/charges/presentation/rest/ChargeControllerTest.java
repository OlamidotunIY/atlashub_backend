package com.atlashub.pay.charges.presentation.rest;

import com.atlashub.pay.charges.application.commands.InitializeCharge.InitializeChargeCommand;
import com.atlashub.pay.charges.application.commands.InitializeCharge.InitializeChargeHandler;
import com.atlashub.pay.charges.application.commands.InitializeCharge.InitializeChargeResult;
import com.atlashub.pay.charges.application.commands.RefundCharge.RefundChargeCommand;
import com.atlashub.pay.charges.application.commands.RefundCharge.RefundChargeHandler;
import com.atlashub.pay.charges.application.commands.RefundCharge.RefundChargeResult;
import com.atlashub.pay.charges.application.queries.GetChargeDetails.ChargeResult;
import com.atlashub.pay.charges.application.queries.GetChargeDetails.GetChargeDetailsHandler;
import com.atlashub.pay.charges.application.queries.GetChargeDetails.GetChargeDetailsQuery;
import com.atlashub.pay.charges.domain.valueobject.ChargeChannel;
import com.atlashub.pay.charges.domain.valueobject.ChargeStatus;
import com.atlashub.pay.charges.domain.valueobject.PaymentProvider;
import com.atlashub.pay.charges.presentation.dto.ChargeDetailsResponse;
import com.atlashub.pay.charges.presentation.dto.InitializeChargeRequest;
import com.atlashub.pay.charges.presentation.dto.InitializeChargeResponse;
import com.atlashub.pay.charges.presentation.dto.RefundChargeRequest;
import com.atlashub.pay.charges.presentation.dto.RefundChargeResponse;
import com.atlashub.shared.application.dto.ApiResponse;
import com.atlashub.shared.application.security.ApiEnvironment;
import com.atlashub.shared.application.security.AuthenticatedPrincipal;
import com.atlashub.shared.domain.valueobject.CurrencyCode;
import com.atlashub.shared.domain.valueobject.Money;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChargeControllerTest {

    @Mock
    private InitializeChargeHandler initializeChargeHandler;

    @Mock
    private RefundChargeHandler refundChargeHandler;

    @Mock
    private GetChargeDetailsHandler getChargeDetailsHandler;

    @InjectMocks
    private ChargeController controller;

    private AuthenticatedPrincipal createPrincipal() {
        return new AuthenticatedPrincipal(
                1L,
                10L,
                "TEST",
                "sess-123",
                "tok-123",
                ZonedDateTime.now().plusHours(1)
        );
    }

    @Test
    @DisplayName("Should initialize charge and return 201 with InitializeChargeResponse")
    void shouldInitializeChargeSuccessfully() {
        AuthenticatedPrincipal principal = createPrincipal();
        InitializeChargeRequest request = new InitializeChargeRequest(
                "REF-001",
                new BigDecimal("5000.00"),
                CurrencyCode.NGN,
                ChargeChannel.CARD,
                "customer@example.com",
                "COMMERCE",
                "ORD-1",
                "CUST-1",
                null,
                Map.of("item", "book")
        );

        ZonedDateTime expiresAt = ZonedDateTime.now().plusHours(2);
        InitializeChargeResult result = new InitializeChargeResult(
                100L,
                10L,
                ApiEnvironment.TEST,
                "REF-001",
                Money.of(new BigDecimal("5000.00"), CurrencyCode.NGN),
                ChargeChannel.CARD,
                ChargeStatus.INITIALIZED,
                "PAYSTACK-REF-100",
                "https://checkout.paystack.com/pay",
                "AUTH-123",
                expiresAt
        );

        when(initializeChargeHandler.execute(any(InitializeChargeCommand.class))).thenReturn(result);

        ResponseEntity<ApiResponse<InitializeChargeResponse>> response = controller.initialize(principal, request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().status());
        InitializeChargeResponse data = response.getBody().data();
        assertEquals(100L, data.chargeId());
        assertEquals("REF-001", data.reference());
        assertEquals(0, data.amount().compareTo(new BigDecimal("5000.00")));
        assertEquals(CurrencyCode.NGN, data.currency());
        assertEquals(ChargeChannel.CARD, data.channel());
        assertEquals(ChargeStatus.INITIALIZED, data.status());
        assertEquals("https://checkout.paystack.com/pay", data.authorizationUrl());
        assertEquals("AUTH-123", data.accessCode());

        ArgumentCaptor<InitializeChargeCommand> captor = ArgumentCaptor.forClass(InitializeChargeCommand.class);
        verify(initializeChargeHandler).execute(captor.capture());
        InitializeChargeCommand executedCommand = captor.getValue();
        assertEquals(10L, executedCommand.organizationId());
        assertEquals(ApiEnvironment.TEST, executedCommand.environment());
        assertEquals("REF-001", executedCommand.reference());
        assertEquals("customer@example.com", executedCommand.email());
    }

    @Test
    @DisplayName("Should initiate refund and return 200 with RefundChargeResponse")
    void shouldInitiateRefundSuccessfully() {
        AuthenticatedPrincipal principal = createPrincipal();
        RefundChargeRequest request = new RefundChargeRequest("Customer requested refund");

        RefundChargeResult result = new RefundChargeResult(
                100L,
                "REF-001",
                ChargeStatus.REFUND_PENDING,
                "Customer requested refund"
        );

        when(refundChargeHandler.execute(any(RefundChargeCommand.class))).thenReturn(result);

        ResponseEntity<ApiResponse<RefundChargeResponse>> response = controller.refund(principal, 100L, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().status());
        RefundChargeResponse data = response.getBody().data();
        assertEquals(100L, data.chargeId());
        assertEquals("REF-001", data.reference());
        assertEquals(ChargeStatus.REFUND_PENDING, data.status());
        assertEquals("Customer requested refund", data.reason());

        ArgumentCaptor<RefundChargeCommand> captor = ArgumentCaptor.forClass(RefundChargeCommand.class);
        verify(refundChargeHandler).execute(captor.capture());
        RefundChargeCommand executedCommand = captor.getValue();
        assertEquals(100L, executedCommand.chargeId());
        assertEquals(10L, executedCommand.organizationId());
        assertEquals(ApiEnvironment.TEST, executedCommand.environment());
        assertEquals("Customer requested refund", executedCommand.reason());
    }

    @Test
    @DisplayName("Should get charge details and return 200 with ChargeDetailsResponse")
    void shouldGetChargeDetailsSuccessfully() {
        AuthenticatedPrincipal principal = createPrincipal();
        ZonedDateTime now = ZonedDateTime.now();

        ChargeResult result = new ChargeResult(
                100L,
                10L,
                ApiEnvironment.TEST,
                "REF-001",
                Money.of(new BigDecimal("5000.00"), CurrencyCode.NGN),
                ChargeChannel.CARD,
                PaymentProvider.PAYSTACK,
                20L,
                "PAYSTACK-REF-100",
                "COMMERCE",
                "ORD-1",
                null,
                null,
                ChargeStatus.SUCCESSFUL,
                "https://checkout.paystack.com/pay",
                "AUTH-123",
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                now,
                now.plusHours(2),
                now.minusHours(1),
                now
        );

        when(getChargeDetailsHandler.execute(any(GetChargeDetailsQuery.class))).thenReturn(result);

        ResponseEntity<ApiResponse<ChargeDetailsResponse>> response = controller.getDetails(principal, 100L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().status());
        ChargeDetailsResponse data = response.getBody().data();
        assertEquals(100L, data.id());
        assertEquals("REF-001", data.reference());
        assertEquals(0, data.amount().compareTo(new BigDecimal("5000.00")));
        assertEquals(CurrencyCode.NGN, data.currency());
        assertEquals(ChargeChannel.CARD, data.channel());
        assertEquals(ChargeStatus.SUCCESSFUL, data.status());
        assertEquals(now, data.successfulAt());

        ArgumentCaptor<GetChargeDetailsQuery> captor = ArgumentCaptor.forClass(GetChargeDetailsQuery.class);
        verify(getChargeDetailsHandler).execute(captor.capture());
        GetChargeDetailsQuery executedQuery = captor.getValue();
        assertEquals(100L, executedQuery.chargeId());
        assertEquals(10L, executedQuery.organizationId());
        assertEquals(ApiEnvironment.TEST, executedQuery.environment());
    }
}
