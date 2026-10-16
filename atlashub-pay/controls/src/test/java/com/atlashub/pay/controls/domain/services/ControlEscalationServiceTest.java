package com.atlashub.pay.controls.domain.services;

import com.atlashub.pay.controls.domain.entities.ControlledFundsPolicy;
import com.atlashub.pay.controls.domain.valueobject.ControlAction;
import com.atlashub.pay.controls.domain.valueobject.ControlMode;
import com.atlashub.pay.controls.domain.valueobject.Environment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ControlEscalationServiceTest {

    private ControlEscalationService escalationService;

    @BeforeEach
    void setUp() {
        escalationService = new ControlEscalationService();
    }

    @Test
    @DisplayName("Should escalate to FREEZE on CRITICAL severity under PURPOSE_RESTRICTED policy")
    void shouldEscalateToFreezeOnCriticalSeverity() {
        ControlledFundsPolicy policy = ControlledFundsPolicy.create(
                1L, 100L, 10L, Environment.LIVE, "v1.0",
                ControlMode.PURPOSE_RESTRICTED, null, null, null, null, null, null, null, null
        );

        ControlEscalationService.EscalationOutcome outcome = escalationService.evaluateAnomaly(policy, "CRITICAL");

        assertEquals(ControlAction.FREEZE, outcome.suggestedAction());
    }

    @Test
    @DisplayName("Should escalate to HOLD on HIGH severity under PURPOSE_RESTRICTED policy")
    void shouldEscalateToHoldOnHighSeverity() {
        ControlledFundsPolicy policy = ControlledFundsPolicy.create(
                1L, 100L, 10L, Environment.LIVE, "v1.0",
                ControlMode.PURPOSE_RESTRICTED, null, null, null, null, null, null, null, null
        );

        ControlEscalationService.EscalationOutcome outcome = escalationService.evaluateAnomaly(policy, "HIGH");

        assertEquals(ControlAction.HOLD, outcome.suggestedAction());
    }

    @Test
    @DisplayName("Should escalate to REVIEW on MEDIUM severity")
    void shouldEscalateToReviewOnMediumSeverity() {
        ControlledFundsPolicy policy = ControlledFundsPolicy.create(
                1L, 100L, 10L, Environment.LIVE, "v1.0",
                ControlMode.MONITORED, null, null, null, null, null, null, null, null
        );

        ControlEscalationService.EscalationOutcome outcome = escalationService.evaluateAnomaly(policy, "MEDIUM");

        assertEquals(ControlAction.REVIEW, outcome.suggestedAction());
    }
}
