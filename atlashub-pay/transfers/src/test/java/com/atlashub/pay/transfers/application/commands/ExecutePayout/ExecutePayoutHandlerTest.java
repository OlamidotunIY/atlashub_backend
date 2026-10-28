package com.atlashub.pay.transfers.application.commands.ExecutePayout;

import com.atlashub.pay.transfers.domain.entities.Payout;
import com.atlashub.pay.transfers.domain.exceptions.PayoutNotFoundException;
import com.atlashub.pay.transfers.domain.repositories.PayoutRepository;
import com.atlashub.pay.transfers.domain.valueobject.PaymentProvider;
import com.atlashub.pay.transfers.domain.valueobject.PayoutStatus;
import com.atlashub.pay.transfers.domain.valueobject.SourceSystem;
import com.atlashub.shared.application.port.PayoutGatewayPort;
import com.atlashub.shared.domain.valueobject.CurrencyCode;
import com.atlashub.shared.domain.valueobject.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExecutePayoutHandlerTest {

    @Mock
    private PayoutRepository payoutRepository;

    @Mock
    private PayoutGatewayPort payoutGatewayPort;

    private ExecutePayoutHandler handler;

    @BeforeEach
    void setUp() {
        handler = new ExecutePayoutHandler(payoutRepository, payoutGatewayPort);
    }

    @Test
    @DisplayName("Provider acceptance leaves payout pending confirmation")
    void shouldExecutePayoutSuccessfully() {
        Payout payout = Payout.create(
                1L, 10L, Money.of(new BigDecimal("50000.00"), CurrencyCode.NGN),
                "044", "0123456789", "John Doe", "Test", "REF-EXEC",
                PaymentProvider.PAYSTACK, SourceSystem.MANUAL, "SRC-1", 100L, false
        );
        when(payoutRepository.findById(1L)).thenReturn(Optional.of(payout));
        when(payoutGatewayPort.initiateTransfer(eq("REF-EXEC"), any(), eq("NGN"), eq("044"), eq("0123456789"), eq("Test")))
                .thenReturn("TRF_99999");

        handler.execute(new ExecutePayoutCommand(1L));

        assertEquals(PayoutStatus.PROCESSING, payout.getStatus());
        assertEquals("TRF_99999", payout.getProviderReference());
        org.junit.jupiter.api.Assertions.assertNull(payout.getCompletedAt());
    }

    @Test
    @DisplayName("Transport uncertainty leaves payout pending confirmation")
    void shouldFailPayoutOnGatewayError() {
        Payout payout = Payout.create(
                2L, 10L, Money.of(new BigDecimal("50000.00"), CurrencyCode.NGN),
                "044", "0123456789", "John Doe", "Test", "REF-FAIL",
                PaymentProvider.PAYSTACK, SourceSystem.MANUAL, "SRC-2", 100L, false
        );
        when(payoutRepository.findById(2L)).thenReturn(Optional.of(payout));
        when(payoutGatewayPort.initiateTransfer(any(), any(), any(), any(), any(), any()))
                .thenThrow(new RuntimeException("Connection timeout to bank switch"));

        handler.execute(new ExecutePayoutCommand(2L));

        assertEquals(PayoutStatus.PROCESSING, payout.getStatus());
        org.junit.jupiter.api.Assertions.assertNull(payout.getCompletedAt());
    }

    @Test
    @DisplayName("Should throw PayoutNotFoundException when payout does not exist")
    void shouldThrowWhenNotFound() {
        when(payoutRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(PayoutNotFoundException.class, () -> handler.execute(new ExecutePayoutCommand(999L)));
    }

    @Test
    void payout_without_a_connected_gateway_fails_without_a_provider_reference() {
        Payout payout = Payout.create(3L, 10L,
                Money.of(new BigDecimal("500.00"), CurrencyCode.NGN), "044", "0123456789",
                "John Doe", "Test", "REF-NONE", PaymentProvider.PAYSTACK,
                SourceSystem.MANUAL, "SRC-3", 100L, false);
        when(payoutRepository.findById(3L)).thenReturn(Optional.of(payout));

        new ExecutePayoutHandler(payoutRepository, null).execute(new ExecutePayoutCommand(3L));

        assertEquals(PayoutStatus.FAILED, payout.getStatus());
        org.junit.jupiter.api.Assertions.assertNull(payout.getProviderReference());
    }
}
