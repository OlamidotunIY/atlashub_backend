package com.atlashub.pay.mandates.domain.entities;

import com.atlashub.pay.mandates.domain.events.MandateChargeDueEvent;
import com.atlashub.pay.mandates.domain.events.MandateChargedEvent;
import com.atlashub.pay.mandates.domain.events.MandateExpiredEvent;
import com.atlashub.pay.mandates.domain.events.MandatePausedEvent;
import com.atlashub.pay.mandates.domain.events.MandateResumedEvent;
import com.atlashub.pay.mandates.domain.events.MandateRevokedEvent;
import com.atlashub.pay.mandates.domain.exceptions.ExpiredMandateException;
import com.atlashub.pay.mandates.domain.exceptions.MandatePausedException;
import com.atlashub.pay.mandates.domain.exceptions.MandateRevokedException;
import com.atlashub.pay.mandates.domain.exceptions.InvalidMandateDetailsException;
import com.atlashub.pay.mandates.domain.exceptions.InvalidMandateStateException;
import com.atlashub.pay.mandates.domain.valueobject.MandateFrequency;
import com.atlashub.pay.mandates.domain.valueobject.MandateStatus;
import com.atlashub.shared.domain.valueobject.CurrencyCode;
import com.atlashub.shared.domain.valueobject.EmailAddress;
import com.atlashub.shared.domain.valueobject.Money;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PaymentMandateTest {

    private PaymentMandate createActiveMandate() {
        return PaymentMandate.create(
                1L,
                10L,
                "CUST-001",
                new EmailAddress("customer@example.com"),
                Money.of(new BigDecimal("15000.00"), CurrencyCode.NGN),
                MandateFrequency.MONTHLY,
                "AUTH_test_code_123",
                LocalDate.of(2026, 11, 1)
        );
    }

    @Test
    @DisplayName("Should create active mandate with valid fields")
    void shouldCreateActiveMandate() {
        PaymentMandate mandate = createActiveMandate();

        assertEquals(1L, mandate.getId());
        assertEquals(10L, mandate.getOrganizationId());
        assertEquals("CUST-001", mandate.getCustomerId());
        assertEquals("customer@example.com", mandate.getEmail().value());
        assertEquals(MandateStatus.ACTIVE, mandate.getStatus());
        assertEquals(MandateFrequency.MONTHLY, mandate.getFrequency());
        assertEquals(LocalDate.of(2026, 11, 1), mandate.getNextChargeDate());
        assertNotNull(mandate.getCreatedAt());
    }

    @Test
    @DisplayName("Should pause active mandate and register MandatePausedEvent")
    void shouldPauseActiveMandate() {
        PaymentMandate mandate = createActiveMandate();

        mandate.pause();

        assertEquals(MandateStatus.PAUSED, mandate.getStatus());
        assertNotNull(mandate.getUpdatedAt());
        assertTrue(mandate.peekDomainEvents().stream().anyMatch(e -> e instanceof MandatePausedEvent));
    }

    @Test
    @DisplayName("Should throw MandatePausedException when pausing already paused mandate")
    void shouldThrowWhenPausingAlreadyPaused() {
        PaymentMandate mandate = createActiveMandate();
        mandate.pause();

        assertThrows(MandatePausedException.class, mandate::pause);
    }

    @Test
    @DisplayName("Should resume paused mandate and register MandateResumedEvent")
    void shouldResumePausedMandate() {
        PaymentMandate mandate = createActiveMandate();
        mandate.pause();

        mandate.resume();

        assertEquals(MandateStatus.ACTIVE, mandate.getStatus());
        assertTrue(mandate.peekDomainEvents().stream().anyMatch(e -> e instanceof MandateResumedEvent));
    }

    @Test
    @DisplayName("Should throw InvalidMandateStateException when resuming active mandate")
    void shouldThrowWhenResumingActiveMandate() {
        PaymentMandate mandate = createActiveMandate();

        assertThrows(InvalidMandateStateException.class, mandate::resume);
    }

    @Test
    @DisplayName("Should revoke mandate and register MandateRevokedEvent")
    void shouldRevokeMandate() {
        PaymentMandate mandate = createActiveMandate();

        mandate.revoke();

        assertEquals(MandateStatus.REVOKED, mandate.getStatus());
        assertTrue(mandate.peekDomainEvents().stream().anyMatch(e -> e instanceof MandateRevokedEvent));

        // Revoking already revoked mandate is idempotent
        mandate.revoke();
        assertEquals(MandateStatus.REVOKED, mandate.getStatus());
    }

    @Test
    @DisplayName("Should reject pause and resume when revoked")
    void shouldRejectOperationsWhenRevoked() {
        PaymentMandate mandate = createActiveMandate();
        mandate.revoke();

        assertThrows(MandateRevokedException.class, mandate::pause);
        assertThrows(MandateRevokedException.class, mandate::resume);
    }

    @Test
    @DisplayName("Should mark mandate expired and register MandateExpiredEvent")
    void shouldMarkExpired() {
        PaymentMandate mandate = createActiveMandate();

        mandate.markExpired("Card expired");

        assertEquals(MandateStatus.EXPIRED, mandate.getStatus());
        assertTrue(mandate.peekDomainEvents().stream().anyMatch(e -> e instanceof MandateExpiredEvent));

        assertThrows(ExpiredMandateException.class, mandate::pause);
        assertThrows(ExpiredMandateException.class, mandate::resume);
    }

    @Test
    @DisplayName("Should advance next charge date correctly for all frequencies")
    void shouldAdvanceNextChargeDate() {
        LocalDate start = LocalDate.of(2026, 1, 1);

        PaymentMandate daily = PaymentMandate.create(2L, 10L, "C", new EmailAddress("a@b.com"),
                Money.of(new BigDecimal("100"), CurrencyCode.NGN), MandateFrequency.DAILY, "AUTH", start);
        daily.advanceNextChargeDate();
        assertEquals(LocalDate.of(2026, 1, 2), daily.getNextChargeDate());

        PaymentMandate weekly = PaymentMandate.create(3L, 10L, "C", new EmailAddress("a@b.com"),
                Money.of(new BigDecimal("100"), CurrencyCode.NGN), MandateFrequency.WEEKLY, "AUTH", start);
        weekly.advanceNextChargeDate();
        assertEquals(LocalDate.of(2026, 1, 8), weekly.getNextChargeDate());

        PaymentMandate monthly = PaymentMandate.create(4L, 10L, "C", new EmailAddress("a@b.com"),
                Money.of(new BigDecimal("100"), CurrencyCode.NGN), MandateFrequency.MONTHLY, "AUTH", start);
        monthly.advanceNextChargeDate();
        assertEquals(LocalDate.of(2026, 2, 1), monthly.getNextChargeDate());

        PaymentMandate quarterly = PaymentMandate.create(5L, 10L, "C", new EmailAddress("a@b.com"),
                Money.of(new BigDecimal("100"), CurrencyCode.NGN), MandateFrequency.QUARTERLY, "AUTH", start);
        quarterly.advanceNextChargeDate();
        assertEquals(LocalDate.of(2026, 4, 1), quarterly.getNextChargeDate());

        PaymentMandate annually = PaymentMandate.create(6L, 10L, "C", new EmailAddress("a@b.com"),
                Money.of(new BigDecimal("100"), CurrencyCode.NGN), MandateFrequency.ANNUALLY, "AUTH", start);
        annually.advanceNextChargeDate();
        assertEquals(LocalDate.of(2027, 1, 1), annually.getNextChargeDate());
    }

    @Test
    @DisplayName("Should trigger charge, register MandateChargeDueEvent, and advance date")
    void shouldTriggerCharge() {
        PaymentMandate mandate = createActiveMandate();

        mandate.triggerCharge("mandate-1-ref", ZonedDateTime.now());

        assertEquals(LocalDate.of(2026, 12, 1), mandate.getNextChargeDate());
        assertTrue(mandate.peekDomainEvents().stream().anyMatch(e -> e instanceof MandateChargeDueEvent));
    }

    @Test
    @DisplayName("Should record charge success and register MandateChargedEvent")
    void shouldRecordChargeSuccess() {
        PaymentMandate mandate = createActiveMandate();

        mandate.recordChargeSuccess("mandate-1-ref");

        assertTrue(mandate.peekDomainEvents().stream().anyMatch(e -> e instanceof MandateChargedEvent));
    }

    @Test
    @DisplayName("Should validate required fields in create")
    void shouldValidateRequiredFields() {
        assertThrows(InvalidMandateDetailsException.class, () -> PaymentMandate.create(
                null, 10L, "C", new EmailAddress("a@b.com"),
                Money.of(new BigDecimal("100"), CurrencyCode.NGN), MandateFrequency.MONTHLY, "AUTH", LocalDate.now()
        ));
        assertThrows(InvalidMandateDetailsException.class, () -> PaymentMandate.create(
                1L, null, "C", new EmailAddress("a@b.com"),
                Money.of(new BigDecimal("100"), CurrencyCode.NGN), MandateFrequency.MONTHLY, "AUTH", LocalDate.now()
        ));
        assertThrows(InvalidMandateDetailsException.class, () -> PaymentMandate.create(
                1L, 10L, "", new EmailAddress("a@b.com"),
                Money.of(new BigDecimal("100"), CurrencyCode.NGN), MandateFrequency.MONTHLY, "AUTH", LocalDate.now()
        ));
    }
}
