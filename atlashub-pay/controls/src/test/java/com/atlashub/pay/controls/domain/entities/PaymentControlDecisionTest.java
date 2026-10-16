package com.atlashub.pay.controls.domain.entities;

import com.atlashub.pay.controls.domain.events.PaymentBlockedEvent;
import com.atlashub.pay.controls.domain.events.PaymentControlEvaluatedEvent;
import com.atlashub.pay.controls.domain.events.PaymentHeldEvent;
import com.atlashub.pay.controls.domain.exceptions.InvalidControlTransitionException;
import com.atlashub.pay.controls.domain.valueobject.ControlAction;
import com.atlashub.pay.controls.domain.valueobject.ControlReason;
import com.atlashub.pay.controls.domain.valueobject.ControlStatus;
import com.atlashub.pay.controls.domain.valueobject.PaymentContext;
import com.atlashub.pay.controls.domain.valueobject.PaymentRail;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PaymentControlDecisionTest {

    @Test
    @DisplayName("Should evaluate decision and register PaymentControlEvaluatedEvent")
    void shouldEvaluateDecisionAndRegisterEvent() {
        PaymentControlDecision decision = PaymentControlDecision.evaluate(
                10L,
                1L,
                100L,
                5L,
                "PAY-TX-001",
                PaymentRail.ATOMIC_TRANSFER,
                PaymentContext.PAYOUT,
                ControlAction.HOLD,
                ControlAction.HOLD,
                Set.of(ControlReason.BENEFICIARY_NOT_APPROVED),
                null
        );

        assertNotNull(decision);
        assertEquals(ControlStatus.EVALUATED, decision.getStatus());
        assertEquals(1, decision.peekDomainEvents().size());
        assertTrue(decision.peekDomainEvents().get(0) instanceof PaymentControlEvaluatedEvent);
    }

    @Test
    @DisplayName("Should confirm applied hold and register PaymentHeldEvent")
    void shouldConfirmAppliedHoldAndRegisterEvent() {
        PaymentControlDecision decision = PaymentControlDecision.evaluate(
                10L, 1L, 100L, 5L, "PAY-TX-001",
                PaymentRail.ATOMIC_TRANSFER, PaymentContext.PAYOUT,
                ControlAction.HOLD, ControlAction.HOLD,
                Set.of(ControlReason.SINGLE_AMOUNT_EXCEEDED), null
        );

        decision.confirmApplied("PROV-HOLD-999");

        assertEquals(ControlStatus.APPLIED, decision.getStatus());
        assertEquals("PROV-HOLD-999", decision.getProviderControlReference());
        assertNotNull(decision.getAppliedAt());
        assertEquals(2, decision.peekDomainEvents().size());
        assertTrue(decision.peekDomainEvents().get(1) instanceof PaymentHeldEvent);
    }

    @Test
    @DisplayName("Should confirm applied block and register PaymentBlockedEvent")
    void shouldConfirmAppliedBlockAndRegisterEvent() {
        PaymentControlDecision decision = PaymentControlDecision.evaluate(
                10L, 1L, 100L, 5L, "PAY-TX-002",
                PaymentRail.ATOMIC_TRANSFER, PaymentContext.PAYOUT,
                ControlAction.BLOCK, ControlAction.BLOCK,
                Set.of(ControlReason.CATEGORY_RESTRICTED), null
        );

        decision.confirmApplied("PROV-BLOCK-123");

        assertEquals(ControlStatus.APPLIED, decision.getStatus());
        assertTrue(decision.peekDomainEvents().get(1) instanceof PaymentBlockedEvent);
    }

    @Test
    @DisplayName("Should release decision successfully")
    void shouldReleaseDecision() {
        PaymentControlDecision decision = PaymentControlDecision.evaluate(
                10L, 1L, 100L, 5L, "PAY-TX-003",
                PaymentRail.ATOMIC_TRANSFER, PaymentContext.PAYOUT,
                ControlAction.HOLD, ControlAction.HOLD,
                Set.of(ControlReason.BENEFICIARY_NOT_APPROVED), null
        );
        decision.confirmApplied("PROV-HOLD-123");

        decision.release();
        assertEquals(ControlStatus.RELEASED, decision.getStatus());
    }

    @Test
    @DisplayName("Should throw when releasing expired or invalid decision")
    void shouldThrowWhenReleasingInvalidDecision() {
        PaymentControlDecision decision = PaymentControlDecision.evaluate(
                10L, 1L, 100L, 5L, "PAY-TX-004",
                PaymentRail.ATOMIC_TRANSFER, PaymentContext.PAYOUT,
                ControlAction.HOLD, ControlAction.HOLD,
                Set.of(ControlReason.BENEFICIARY_NOT_APPROVED), null
        );
        decision.expire();

        assertThrows(InvalidControlTransitionException.class, decision::release);
    }
}
