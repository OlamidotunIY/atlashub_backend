package com.atlashub.pay.transfers.application.commands.ApprovePayout;

import com.atlashub.pay.transfers.domain.entities.Payout;
import com.atlashub.pay.transfers.domain.exceptions.PayoutNotFoundException;
import com.atlashub.pay.transfers.domain.exceptions.SelfApprovalNotAllowedException;
import com.atlashub.pay.transfers.domain.repositories.PayoutRepository;
import com.atlashub.pay.transfers.domain.valueobject.PaymentProvider;
import com.atlashub.pay.transfers.domain.valueobject.PayoutStatus;
import com.atlashub.pay.transfers.domain.valueobject.SourceSystem;
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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ApprovePayoutHandlerTest {

    @Mock
    private PayoutRepository payoutRepository;

    private ApprovePayoutHandler handler;

    @BeforeEach
    void setUp() {
        handler = new ApprovePayoutHandler(payoutRepository);
    }

    @Test
    @DisplayName("Should approve pending payout when approved by a different user")
    void shouldApprovePayoutSuccessfully() {
        Payout payout = Payout.create(
                1L, 10L, Money.of(new BigDecimal("200000.00"), CurrencyCode.NGN),
                "044", "0123456789", "John Doe", "Test", "REF-001",
                PaymentProvider.PAYSTACK, SourceSystem.MANUAL, "SRC-1", 100L, true
        );
        when(payoutRepository.findByIdWithPessimisticLock(1L)).thenReturn(Optional.of(payout));

        handler.execute(new ApprovePayoutCommand(1L, 200L));

        assertEquals(PayoutStatus.APPROVED, payout.getStatus());
        assertEquals(200L, payout.getApprovedBy());
        verify(payoutRepository).save(payout);
    }

    @Test
    @DisplayName("Maker-Checker: Should reject self-approval with SelfApprovalNotAllowedException")
    void shouldRejectSelfApproval() {
        Payout payout = Payout.create(
                2L, 10L, Money.of(new BigDecimal("200000.00"), CurrencyCode.NGN),
                "044", "0123456789", "John Doe", "Test", "REF-002",
                PaymentProvider.PAYSTACK, SourceSystem.MANUAL, "SRC-2", 100L, true
        );
        when(payoutRepository.findByIdWithPessimisticLock(2L)).thenReturn(Optional.of(payout));

        assertThrows(SelfApprovalNotAllowedException.class, () -> handler.execute(new ApprovePayoutCommand(2L, 100L)));
    }

    @Test
    @DisplayName("Should throw PayoutNotFoundException when payout does not exist")
    void shouldThrowWhenNotFound() {
        when(payoutRepository.findByIdWithPessimisticLock(999L)).thenReturn(Optional.empty());

        assertThrows(PayoutNotFoundException.class, () -> handler.execute(new ApprovePayoutCommand(999L, 200L)));
    }
}
