package com.atlashub.pay.controls.domain.entities;

import com.atlashub.pay.controls.domain.exceptions.InvalidPolicyException;
import com.atlashub.pay.controls.domain.exceptions.PolicyNotActiveException;
import com.atlashub.pay.controls.domain.valueobject.AmountFrequencyRuleSet;
import com.atlashub.pay.controls.domain.valueobject.BeneficiaryRuleSet;
import com.atlashub.pay.controls.domain.valueobject.ControlAction;
import com.atlashub.pay.controls.domain.valueobject.ControlMode;
import com.atlashub.pay.controls.domain.valueobject.Environment;
import com.atlashub.pay.controls.domain.valueobject.PaymentRail;
import com.atlashub.pay.controls.domain.valueobject.PermittedCategory;
import com.atlashub.pay.controls.domain.valueobject.PolicyStatus;
import com.atlashub.pay.controls.domain.valueobject.ProviderControlCapability;
import com.atlashub.pay.controls.domain.valueobject.RestrictedCategory;
import com.atlashub.pay.controls.domain.valueobject.WithdrawalRuleSet;
import com.atlashub.shared.domain.valueobject.CurrencyCode;
import com.atlashub.shared.domain.valueobject.Money;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ControlledFundsPolicyTest {

    @Test
    @DisplayName("Should create policy in DRAFT status with valid parameters")
    void shouldCreatePolicyInDraftStatus() {
        ControlledFundsPolicy policy = ControlledFundsPolicy.create(
                1L,
                100L,
                10L,
                Environment.LIVE,
                "v1.0",
                ControlMode.PURPOSE_RESTRICTED,
                Set.of(new PermittedCategory("RAW_MATERIALS", "Raw Materials")),
                Set.of(new RestrictedCategory("GAMBLING", "Prohibited")),
                new BeneficiaryRuleSet(false, Set.of("ACC-123"), Set.of("ACC-BLOCKED")),
                new WithdrawalRuleSet(Money.of(new BigDecimal("100000"), CurrencyCode.NGN), null, true),
                new AmountFrequencyRuleSet(Money.of(new BigDecimal("500000"), CurrencyCode.NGN), null, null, 10),
                Set.of(ProviderControlCapability.HOLD_INSTRUCTION, ProviderControlCapability.BLOCK_INSTRUCTION),
                ZonedDateTime.now(),
                null
        );

        assertNotNull(policy);
        assertEquals(1L, policy.getId());
        assertEquals(PolicyStatus.DRAFT, policy.getStatus());
        assertEquals(ControlMode.PURPOSE_RESTRICTED, policy.getMode());
    }

    @Test
    @DisplayName("Should activate policy and register event")
    void shouldActivatePolicy() {
        ControlledFundsPolicy policy = ControlledFundsPolicy.create(
                1L, 100L, 10L, Environment.LIVE, "v1.0",
                ControlMode.MONITORED, null, null, null, null, null, null, null, null
        );

        policy.activate("LENDER_ADMIN");

        assertEquals(PolicyStatus.ACTIVE, policy.getStatus());
        assertEquals("LENDER_ADMIN", policy.getActivatedBy());
        assertEquals(1, policy.peekDomainEvents().size());
    }

    @Test
    @DisplayName("Should throw when suspending inactive policy")
    void shouldThrowWhenSuspendingInactivePolicy() {
        ControlledFundsPolicy policy = ControlledFundsPolicy.create(
                1L, 100L, 10L, Environment.LIVE, "v1.0",
                ControlMode.MONITORED, null, null, null, null, null, null, null, null
        );

        assertThrows(PolicyNotActiveException.class, () -> policy.suspend("Suspension test"));
    }

    @Test
    @DisplayName("Should suspend and expire active policy correctly")
    void shouldSuspendAndExpireActivePolicy() {
        ControlledFundsPolicy policy = ControlledFundsPolicy.create(
                1L, 100L, 10L, Environment.LIVE, "v1.0",
                ControlMode.MONITORED, null, null, null, null, null, null, null, null
        );
        policy.activate("ADMIN");

        policy.suspend("Audit pending");
        assertEquals(PolicyStatus.SUSPENDED, policy.getStatus());

        policy.expire();
        assertEquals(PolicyStatus.EXPIRED, policy.getStatus());
        assertNotNull(policy.getEffectiveUntil());
    }

    @Test
    @DisplayName("Should correctly evaluate capability support per rail")
    void shouldEvaluateCapabilitySupportPerRail() {
        ControlledFundsPolicy policy = ControlledFundsPolicy.create(
                1L, 100L, 10L, Environment.LIVE, "v1.0",
                ControlMode.PURPOSE_RESTRICTED, null, null, null, null, null,
                Set.of(ProviderControlCapability.HOLD_INSTRUCTION, ProviderControlCapability.BLOCK_INSTRUCTION),
                null, null
        );

        // Internal ledger supports holds, blocks, freezes
        assertTrue(policy.supports(ControlAction.FREEZE, PaymentRail.INTERNAL_LEDGER));
        assertTrue(policy.supports(ControlAction.HOLD, PaymentRail.INTERNAL_LEDGER));

        // External accounts can never be frozen or blocked
        assertFalse(policy.supports(ControlAction.FREEZE, PaymentRail.EXTERNAL_ACCOUNT));
        assertFalse(policy.supports(ControlAction.HOLD, PaymentRail.EXTERNAL_ACCOUNT));

        // Virtual account check against capabilities
        assertTrue(policy.supports(ControlAction.HOLD, PaymentRail.VIRTUAL_ACCOUNT));
        assertFalse(policy.supports(ControlAction.FREEZE, PaymentRail.VIRTUAL_ACCOUNT)); // not in capabilities
    }

    @Test
    @DisplayName("Should throw InvalidPolicyException when invariant breached")
    void shouldThrowInvalidPolicyExceptionWhenInvariantBreached() {
        assertThrows(InvalidPolicyException.class, () -> ControlledFundsPolicy.create(
                null, 100L, 10L, Environment.LIVE, "v1.0", ControlMode.MONITORED,
                null, null, null, null, null, null, null, null
        ));
    }
}
