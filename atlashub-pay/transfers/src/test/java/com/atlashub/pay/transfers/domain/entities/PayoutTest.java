package com.atlashub.pay.transfers.domain.entities;

import com.atlashub.pay.transfers.domain.events.PayoutApprovedEvent;
import com.atlashub.pay.transfers.domain.events.PayoutCompletedEvent;
import com.atlashub.pay.transfers.domain.events.PayoutFailedEvent;
import com.atlashub.pay.transfers.domain.exceptions.InvalidBankDetailsException;
import com.atlashub.pay.transfers.domain.exceptions.InvalidPayoutStateException;
import com.atlashub.pay.transfers.domain.exceptions.SelfApprovalNotAllowedException;
import com.atlashub.pay.transfers.domain.valueobject.PaymentProvider;
import com.atlashub.pay.transfers.domain.valueobject.PayoutStatus;
import com.atlashub.pay.transfers.domain.valueobject.SourceSystem;
import com.atlashub.shared.domain.valueobject.CurrencyCode;
import com.atlashub.shared.domain.valueobject.Money;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PayoutTest {

    @Test
    @DisplayName("Should create payout requiring approval in PENDING_APPROVAL status")
    void shouldCreatePayoutRequiringApproval() {
        Money amount = Money.of(new BigDecimal("250000.00"), CurrencyCode.NGN);
        Payout payout = Payout.create(
                1L, 10L, amount, "044", "0123456789", "John Doe",
                "Vendor payment", "REF-001", PaymentProvider.PAYSTACK,
                SourceSystem.MANUAL, "SRC-1", 100L, true
        );

        assertNotNull(payout);
        assertEquals(PayoutStatus.PENDING_APPROVAL, payout.getStatus());
        assertNull(payout.getApprovedBy());
        assertNull(payout.getApprovedAt());
    }

    @Test
    @DisplayName("Should create payout not requiring approval in APPROVED status and register event")
    void shouldCreatePayoutAutoApproved() {
        Money amount = Money.of(new BigDecimal("50000.00"), CurrencyCode.NGN);
        Payout payout = Payout.create(
                2L, 10L, amount, "044", "0123456789", "John Doe",
                "Float replenishment", "REF-002", PaymentProvider.PAYSTACK,
                SourceSystem.MANUAL, "SRC-2", 100L, false
        );

        assertEquals(PayoutStatus.APPROVED, payout.getStatus());
        assertEquals(100L, payout.getApprovedBy());
        assertNotNull(payout.getApprovedAt());
        assertTrue(payout.peekDomainEvents().stream().anyMatch(e -> e instanceof PayoutApprovedEvent));
    }

    @Test
    @DisplayName("Should enforce Maker-Checker: initiator cannot approve own payout")
    void shouldEnforceMakerCheckerSelfApproval() {
        Money amount = Money.of(new BigDecimal("300000.00"), CurrencyCode.NGN);
        Payout payout = Payout.create(
                3L, 10L, amount, "044", "0123456789", "John Doe",
                "Vendor payment", "REF-003", PaymentProvider.PAYSTACK,
                SourceSystem.MANUAL, "SRC-3", 100L, true
        );

        assertThrows(SelfApprovalNotAllowedException.class, () -> payout.approve(100L));
    }

    @Test
    @DisplayName("Should allow second authorized user to approve payout")
    void shouldAllowSecondUserToApprove() {
        Money amount = Money.of(new BigDecimal("300000.00"), CurrencyCode.NGN);
        Payout payout = Payout.create(
                4L, 10L, amount, "044", "0123456789", "John Doe",
                "Vendor payment", "REF-004", PaymentProvider.PAYSTACK,
                SourceSystem.MANUAL, "SRC-4", 100L, true
        );

        payout.approve(200L);

        assertEquals(PayoutStatus.APPROVED, payout.getStatus());
        assertEquals(200L, payout.getApprovedBy());
        assertNotNull(payout.getApprovedAt());
        assertTrue(payout.peekDomainEvents().stream().anyMatch(e -> e instanceof PayoutApprovedEvent));
    }

    @Test
    @DisplayName("Should transition from APPROVED to PROCESSING to SUCCESSFUL")
    void shouldCompletePayoutSuccessfully() {
        Money amount = Money.of(new BigDecimal("50000.00"), CurrencyCode.NGN);
        Payout payout = Payout.create(
                5L, 10L, amount, "044", "0123456789", "John Doe",
                "Replenishment", "REF-005", PaymentProvider.PAYSTACK,
                SourceSystem.MANUAL, "SRC-5", 100L, false
        );

        payout.markProcessing();
        assertEquals(PayoutStatus.PROCESSING, payout.getStatus());

        payout.complete("PRV-12345");
        assertEquals(PayoutStatus.SUCCESSFUL, payout.getStatus());
        assertEquals("PRV-12345", payout.getProviderReference());
        assertNotNull(payout.getCompletedAt());
        assertTrue(payout.peekDomainEvents().stream().anyMatch(e -> e instanceof PayoutCompletedEvent));
    }

    @Test
    @DisplayName("Should transition from PROCESSING to FAILED")
    void shouldFailPayout() {
        Money amount = Money.of(new BigDecimal("50000.00"), CurrencyCode.NGN);
        Payout payout = Payout.create(
                6L, 10L, amount, "044", "0123456789", "John Doe",
                "Replenishment", "REF-006", PaymentProvider.PAYSTACK,
                SourceSystem.MANUAL, "SRC-6", 100L, false
        );

        payout.markProcessing();
        payout.fail("Account closed");

        assertEquals(PayoutStatus.FAILED, payout.getStatus());
        assertEquals("Account closed", payout.getFailureReason());
        assertNotNull(payout.getCompletedAt());
        assertTrue(payout.peekDomainEvents().stream().anyMatch(e -> e instanceof PayoutFailedEvent));
    }

    @Test
    @DisplayName("Should reject invalid state transitions")
    void shouldRejectInvalidStateTransitions() {
        Money amount = Money.of(new BigDecimal("250000.00"), CurrencyCode.NGN);
        Payout payout = Payout.create(
                7L, 10L, amount, "044", "0123456789", "John Doe",
                "Vendor", "REF-007", PaymentProvider.PAYSTACK,
                SourceSystem.MANUAL, "SRC-7", 100L, true
        );

        assertThrows(InvalidPayoutStateException.class, payout::markProcessing);
        assertThrows(InvalidPayoutStateException.class, () -> payout.complete("REF"));
    }

    @Test
    @DisplayName("Should throw InvalidBankDetailsException when bank details are missing")
    void shouldValidateBankDetails() {
        Money amount = Money.of(new BigDecimal("50000.00"), CurrencyCode.NGN);

        assertThrows(InvalidBankDetailsException.class, () -> Payout.create(
                8L, 10L, amount, "", "0123456789", "John", "Test", "REF-008",
                PaymentProvider.PAYSTACK, SourceSystem.MANUAL, "S", 100L, false
        ));

        assertThrows(InvalidBankDetailsException.class, () -> Payout.create(
                8L, 10L, amount, "044", null, "John", "Test", "REF-008",
                PaymentProvider.PAYSTACK, SourceSystem.MANUAL, "S", 100L, false
        ));
    }
}
