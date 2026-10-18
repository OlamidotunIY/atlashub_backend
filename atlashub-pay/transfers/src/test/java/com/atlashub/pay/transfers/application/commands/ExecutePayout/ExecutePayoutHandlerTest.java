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
    @DisplayName("Should execute payout and transition to SUCCESSFUL when gateway returns reference")
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

        assertEquals(PayoutStatus.SUCCESSFUL, payout.getStatus());
        assertEquals("TRF_99999", payout.getProviderReference());
        assertNotNull(payout.getCompletedAt());
    }

    @Test
    @DisplayName("Should mark payout FAILED when gateway throws an error")
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

        assertEquals(PayoutStatus.FAILED, payout.getStatus());
        assertEquals("Connection timeout to bank switch", payout.getFailureReason());
        assertNotNull(payout.getCompletedAt());
    }

    @Test
    @DisplayName("Should throw PayoutNotFoundException when payout does not exist")
    void shouldThrowWhenNotFound() {
        when(payoutRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(PayoutNotFoundException.class, () -> handler.execute(new ExecutePayoutCommand(999L)));
    }
}
