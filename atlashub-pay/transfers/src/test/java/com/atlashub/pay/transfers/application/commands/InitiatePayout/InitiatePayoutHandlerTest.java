package com.atlashub.pay.transfers.application.commands.InitiatePayout;

import com.atlashub.pay.transfers.domain.entities.Payout;
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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InitiatePayoutHandlerTest {

    @Mock
    private PayoutRepository payoutRepository;

    @Mock
    private PayoutGatewayPort payoutGatewayPort;

    private InitiatePayoutHandler handler;

    @BeforeEach
    void setUp() {
        handler = new InitiatePayoutHandler(payoutRepository, payoutGatewayPort);
    }

    @Test
    @DisplayName("Should return existing payout when reference already exists (Idempotent)")
    void shouldReturnExistingPayoutWhenReferenceExists() {
        Payout existing = Payout.create(
                1L, 10L, Money.of(new BigDecimal("50000.00"), CurrencyCode.NGN),
                "044", "0123456789", "John Doe", "Test", "REF-EXISTING",
                PaymentProvider.PAYSTACK, SourceSystem.MANUAL, "SRC-1", 100L, false
        );
        when(payoutRepository.findByReference("REF-EXISTING")).thenReturn(Optional.of(existing));

        InitiatePayoutCommand command = new InitiatePayoutCommand(
                10L, 100L, new BigDecimal("50000.00"), "NGN", "044", "0123456789",
                "John Doe", "Test", "REF-EXISTING", PaymentProvider.PAYSTACK, SourceSystem.MANUAL, "SRC-1"
        );

        InitiatePayoutResponse response = handler.execute(command);

        assertEquals(1L, response.payoutId());
        assertEquals("REF-EXISTING", response.reference());
        assertEquals("APPROVED", response.status());
        assertFalse(response.requiresApproval());
    }

    @Test
    @DisplayName("Should initiate payout requiring approval when amount exceeds threshold")
    void shouldRequireApprovalWhenAmountExceedsThreshold() {
        when(payoutRepository.findByReference("REF-HIGH")).thenReturn(Optional.empty());
        when(payoutRepository.nextIdentity()).thenReturn(2L);
        when(payoutRepository.save(any(Payout.class))).thenAnswer(invocation -> invocation.getArgument(0));

        InitiatePayoutCommand command = new InitiatePayoutCommand(
                10L, 100L, new BigDecimal("250000.00"), "NGN", "044", "0123456789",
                "John Doe", "Test High", "REF-HIGH", PaymentProvider.PAYSTACK, SourceSystem.MANUAL, "SRC-2"
        );

        InitiatePayoutResponse response = handler.execute(command);

        assertEquals(2L, response.payoutId());
        assertEquals("REF-HIGH", response.reference());
        assertEquals("PENDING_APPROVAL", response.status());
        assertTrue(response.requiresApproval());
        verify(payoutRepository).save(any(Payout.class));
    }

    @Test
    @DisplayName("Should auto-approve payout when amount is below threshold")
    void shouldAutoApproveWhenAmountBelowThreshold() {
        when(payoutRepository.findByReference("REF-LOW")).thenReturn(Optional.empty());
        when(payoutRepository.nextIdentity()).thenReturn(3L);
        when(payoutRepository.save(any(Payout.class))).thenAnswer(invocation -> invocation.getArgument(0));

        InitiatePayoutCommand command = new InitiatePayoutCommand(
                10L, 100L, new BigDecimal("45000.00"), "NGN", "044", "0123456789",
                "John Doe", "Test Low", "REF-LOW", PaymentProvider.PAYSTACK, SourceSystem.MANUAL, "SRC-3"
        );

        InitiatePayoutResponse response = handler.execute(command);

        assertEquals(3L, response.payoutId());
        assertEquals("REF-LOW", response.reference());
        assertEquals("APPROVED", response.status());
        assertFalse(response.requiresApproval());
    }

    @Test
    @DisplayName("Should resolve recipient account name via gateway when not provided")
    void shouldResolveRecipientAccountName() {
        when(payoutRepository.findByReference("REF-RESOLVE")).thenReturn(Optional.empty());
        when(payoutRepository.nextIdentity()).thenReturn(4L);
        when(payoutGatewayPort.resolveAccountName("0123456789", "044")).thenReturn("RESOLVED ACCOUNT NAME");
        when(payoutRepository.save(any(Payout.class))).thenAnswer(invocation -> invocation.getArgument(0));

        InitiatePayoutCommand command = new InitiatePayoutCommand(
                10L, 100L, new BigDecimal("10000.00"), "NGN", "044", "0123456789",
                null, "Test Resolve", "REF-RESOLVE", PaymentProvider.PAYSTACK, SourceSystem.MANUAL, "SRC-4"
        );

        InitiatePayoutResponse response = handler.execute(command);

        assertEquals(4L, response.payoutId());
        verify(payoutGatewayPort).resolveAccountName("0123456789", "044");
    }
}
