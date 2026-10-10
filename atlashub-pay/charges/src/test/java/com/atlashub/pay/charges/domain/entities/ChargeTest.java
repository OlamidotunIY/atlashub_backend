package com.atlashub.pay.charges.domain.entities;

import com.atlashub.pay.charges.domain.events.ChargeFailedEvent;
import com.atlashub.pay.charges.domain.events.ChargeRefundInitiatedEvent;
import com.atlashub.pay.charges.domain.events.ChargeSuccessfulEvent;
import com.atlashub.pay.charges.domain.exceptions.InvalidChargeException;
import com.atlashub.pay.charges.domain.valueobject.ChargeChannel;
import com.atlashub.pay.charges.domain.valueobject.ChargeStatus;
import com.atlashub.pay.charges.domain.valueobject.PaymentProvider;
import com.atlashub.shared.application.security.ApiEnvironment;
import com.atlashub.shared.domain.event.DomainEvent;
import com.atlashub.shared.domain.valueobject.CurrencyCode;
import com.atlashub.shared.domain.valueobject.Money;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ChargeTest {

    private Charge createInitializedCharge() {
        return Charge.initialize(
                1L,
                10L,
                ApiEnvironment.TEST,
                "REF-100",
                Money.of(new BigDecimal("5000.00"), CurrencyCode.NGN),
                ChargeChannel.CARD,
                20L,
                "COMMERCE",
                "ORD-999"
        );
    }

    private Charge createPendingCharge() {
        Charge charge = createInitializedCharge();
        charge.markPending("PAYSTACK-REF-100", "https://checkout.paystack.com/123", "AUTH_CODE_123");
        return charge;
    }

    private Charge createSuccessfulCharge() {
        Charge charge = createPendingCharge();
        charge.succeed("PAYSTACK-REF-100", Money.of(new BigDecimal("5000.00"), CurrencyCode.NGN), CurrencyCode.NGN);
        return charge;
    }

    @Test
    @DisplayName("Should initialize charge in INITIALIZED status")
    void shouldInitializeCharge() {
        Charge charge = createInitializedCharge();

        assertEquals(1L, charge.getId());
        assertEquals(10L, charge.getOrganizationId());
        assertEquals(ApiEnvironment.TEST, charge.getEnvironment());
        assertEquals("REF-100", charge.getReference());
        assertEquals(new BigDecimal("5000.0000"), charge.getAmount().amount());
        assertEquals(ChargeChannel.CARD, charge.getChannel());
        assertEquals(PaymentProvider.PAYSTACK, charge.getProvider());
        assertEquals(20L, charge.getProviderProfileId());
        assertEquals("COMMERCE", charge.getSourceSystem());
        assertEquals("ORD-999", charge.getSourceReferenceId());
        assertEquals(ChargeStatus.INITIALIZED, charge.getStatus());
        assertNull(charge.getProviderReference());
        assertNull(charge.getAuthorizationUrl());
        assertNull(charge.getAccessCode());
        assertNotNull(charge.getExpiresAt());
        assertTrue(charge.getExpiresAt().isAfter(ZonedDateTime.now()));
    }

    @Test
    @DisplayName("Should transition initialized charge to PENDING")
    void shouldTransitionToPending() {
        Charge charge = createInitializedCharge();
        charge.markPending("PROV-REF", "https://pay.com", "ACCESS-123");

        assertEquals(ChargeStatus.PENDING, charge.getStatus());
        assertEquals("PROV-REF", charge.getProviderReference());
        assertEquals("https://pay.com", charge.getAuthorizationUrl());
        assertEquals("ACCESS-123", charge.getAccessCode());
    }

    @Test
    @DisplayName("Should throw exception when marking pending from non-INITIALIZED state")
    void shouldThrowWhenMarkingPendingFromInvalidState() {
        Charge charge = createPendingCharge();

        InvalidChargeException ex = assertThrows(InvalidChargeException.class, () ->
                charge.markPending("PROV-REF-2", "https://pay.com", "ACCESS-456")
        );
        assertTrue(ex.getMessage().contains("Only an initialized charge"));
    }

    @Test
    @DisplayName("Should throw exception when marking pending with blank provider details")
    void shouldThrowWhenMarkingPendingWithBlankDetails() {
        Charge charge = createInitializedCharge();

        assertThrows(InvalidChargeException.class, () ->
                charge.markPending("", "https://pay.com", "ACCESS-123")
        );
        assertThrows(InvalidChargeException.class, () ->
                charge.markPending("REF", " ", "ACCESS-123")
        );
        assertThrows(InvalidChargeException.class, () ->
                charge.markPending("REF", "https://pay.com", "")
        );
    }

    @Test
    @DisplayName("Should succeed pending charge and register event")
    void shouldSucceedPendingCharge() {
        Charge charge = createPendingCharge();
        charge.succeed("GW-REF-FINAL", Money.of(new BigDecimal("5000.00"), CurrencyCode.NGN), CurrencyCode.NGN);

        assertEquals(ChargeStatus.SUCCESSFUL, charge.getStatus());
        assertEquals("GW-REF-FINAL", charge.getProviderReference());
        assertNotNull(charge.getSuccessfulAt());

        List<DomainEvent<?>> events = charge.pullDomainEvents();
        assertEquals(1, events.size());
        assertTrue(events.get(0) instanceof ChargeSuccessfulEvent);
        ChargeSuccessfulEvent event = (ChargeSuccessfulEvent) events.get(0);
        assertEquals(1L, event.aggregateId());
        assertEquals(10L, event.payload().organizationId());
        assertEquals("GW-REF-FINAL", event.payload().gatewayReference());
    }

    @Test
    @DisplayName("Should be idempotent when succeeding already successful charge")
    void shouldBeIdempotentOnSucceed() {
        Charge charge = createSuccessfulCharge();
        charge.pullDomainEvents(); // clear

        charge.succeed("GW-REF-FINAL", Money.of(new BigDecimal("5000.00"), CurrencyCode.NGN), CurrencyCode.NGN);

        assertEquals(ChargeStatus.SUCCESSFUL, charge.getStatus());
        assertTrue(charge.pullDomainEvents().isEmpty());
    }

    @Test
    @DisplayName("Should throw when succeeding charge that is not pending")
    void shouldThrowWhenSucceedingNonPendingCharge() {
        Charge charge = createInitializedCharge();

        InvalidChargeException ex = assertThrows(InvalidChargeException.class, () ->
                charge.succeed("GW-REF", Money.of(new BigDecimal("5000.00"), CurrencyCode.NGN), CurrencyCode.NGN)
        );
        assertTrue(ex.getMessage().contains("Only a pending charge can succeed"));
    }

    @Test
    @DisplayName("Should throw when confirmed amount or currency does not match")
    void shouldThrowWhenAmountOrCurrencyMismatchOnSucceed() {
        Charge charge = createPendingCharge();

        assertThrows(InvalidChargeException.class, () ->
                charge.succeed("GW-REF", Money.of(new BigDecimal("4999.00"), CurrencyCode.NGN), CurrencyCode.NGN)
        );
    }

    @Test
    @DisplayName("Should fail charge and register event")
    void shouldFailCharge() {
        Charge charge = createPendingCharge();
        charge.fail("Insufficient funds");

        assertEquals(ChargeStatus.FAILED, charge.getStatus());
        assertEquals("Insufficient funds", charge.getFailureMessage());

        List<DomainEvent<?>> events = charge.pullDomainEvents();
        assertEquals(1, events.size());
        assertTrue(events.get(0) instanceof ChargeFailedEvent);
        ChargeFailedEvent event = (ChargeFailedEvent) events.get(0);
        assertEquals("Insufficient funds", event.payload().reason());
    }

    @Test
    @DisplayName("Should be idempotent when failing already failed charge")
    void shouldBeIdempotentOnFail() {
        Charge charge = createPendingCharge();
        charge.fail("Card expired");
        charge.pullDomainEvents();

        charge.fail("Card expired again");

        assertEquals(ChargeStatus.FAILED, charge.getStatus());
        assertTrue(charge.pullDomainEvents().isEmpty());
    }

    @Test
    @DisplayName("Should throw when failing already successful charge")
    void shouldThrowWhenFailingSuccessfulCharge() {
        Charge charge = createSuccessfulCharge();

        InvalidChargeException ex = assertThrows(InvalidChargeException.class, () ->
                charge.fail("Too late")
        );
        assertTrue(ex.getMessage().contains("A final successful charge cannot fail"));
    }

    @Test
    @DisplayName("Should initiate refund on successful charge and register event")
    void shouldInitiateRefund() {
        Charge charge = createSuccessfulCharge();
        charge.pullDomainEvents();

        charge.initiateRefund("Customer requested refund");

        assertEquals(ChargeStatus.REFUND_PENDING, charge.getStatus());

        List<DomainEvent<?>> events = charge.pullDomainEvents();
        assertEquals(1, events.size());
        assertTrue(events.get(0) instanceof ChargeRefundInitiatedEvent);
        ChargeRefundInitiatedEvent event = (ChargeRefundInitiatedEvent) events.get(0);
        assertEquals(1L, event.aggregateId());
        assertEquals("Customer requested refund", event.payload().reason());
        assertEquals(charge.getAmount(), event.payload().amount());
    }

    @Test
    @DisplayName("Should be idempotent when initiating refund on already REFUND_PENDING charge")
    void shouldBeIdempotentOnInitiateRefund() {
        Charge charge = createSuccessfulCharge();
        charge.initiateRefund("First request");
        charge.pullDomainEvents();

        charge.initiateRefund("First request duplicate");

        assertEquals(ChargeStatus.REFUND_PENDING, charge.getStatus());
        assertTrue(charge.pullDomainEvents().isEmpty());
    }

    @Test
    @DisplayName("Should throw when initiating refund on non-successful charge")
    void shouldThrowWhenInitiatingRefundOnNonSuccessfulCharge() {
        Charge charge = createPendingCharge();

        InvalidChargeException ex = assertThrows(InvalidChargeException.class, () ->
                charge.initiateRefund("Refund please")
        );
        assertTrue(ex.getMessage().contains("Only a successful charge can be refunded"));
    }

    @Test
    @DisplayName("Should throw when initiating refund with blank reason")
    void shouldThrowWhenInitiatingRefundWithBlankReason() {
        Charge charge = createSuccessfulCharge();

        assertThrows(InvalidChargeException.class, () ->
                charge.initiateRefund("")
        );
        assertThrows(InvalidChargeException.class, () ->
                charge.initiateRefund("   ")
        );
    }

    @Test
    @DisplayName("Should validate invariants on construction")
    void shouldValidateInvariants() {
        assertThrows(InvalidChargeException.class, () ->
                Charge.initialize(null, 10L, ApiEnvironment.TEST, "REF",
                        Money.of(new BigDecimal("100"), CurrencyCode.NGN), ChargeChannel.CARD, 1L, "SYS", "SREF")
        );
        assertThrows(InvalidChargeException.class, () ->
                Charge.initialize(1L, null, ApiEnvironment.TEST, "REF",
                        Money.of(new BigDecimal("100"), CurrencyCode.NGN), ChargeChannel.CARD, 1L, "SYS", "SREF")
        );
        assertThrows(InvalidChargeException.class, () ->
                Charge.initialize(1L, 10L, null, "REF",
                        Money.of(new BigDecimal("100"), CurrencyCode.NGN), ChargeChannel.CARD, 1L, "SYS", "SREF")
        );
        assertThrows(InvalidChargeException.class, () ->
                Charge.initialize(1L, 10L, ApiEnvironment.TEST, "",
                        Money.of(new BigDecimal("100"), CurrencyCode.NGN), ChargeChannel.CARD, 1L, "SYS", "SREF")
        );
        assertThrows(InvalidChargeException.class, () ->
                Charge.initialize(1L, 10L, ApiEnvironment.TEST, "REF",
                        Money.of(BigDecimal.ZERO, CurrencyCode.NGN), ChargeChannel.CARD, 1L, "SYS", "SREF")
        );
    }
}
