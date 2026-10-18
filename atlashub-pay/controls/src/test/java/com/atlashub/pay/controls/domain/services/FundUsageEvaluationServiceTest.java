package com.atlashub.pay.controls.domain.services;

import com.atlashub.pay.controls.domain.entities.ControlledFundsPolicy;
import com.atlashub.pay.controls.domain.valueobject.AmountFrequencyRuleSet;
import com.atlashub.pay.controls.domain.valueobject.BeneficiaryRuleSet;
import com.atlashub.pay.controls.domain.valueobject.ControlAction;
import com.atlashub.pay.controls.domain.valueobject.ControlMode;
import com.atlashub.pay.controls.domain.valueobject.ControlReason;
import com.atlashub.pay.controls.domain.valueobject.Environment;
import com.atlashub.pay.controls.domain.valueobject.PaymentContext;
import com.atlashub.pay.controls.domain.valueobject.PermittedCategory;
import com.atlashub.pay.controls.domain.valueobject.RestrictedCategory;
import com.atlashub.pay.controls.domain.valueobject.WithdrawalRuleSet;
import com.atlashub.shared.domain.valueobject.CurrencyCode;
import com.atlashub.shared.domain.valueobject.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FundUsageEvaluationServiceTest {

    private FundUsageEvaluationService evaluationService;

    @BeforeEach
    void setUp() {
        evaluationService = new FundUsageEvaluationService();
    }

    @Test
    @DisplayName("Should allow payment when mode is UNRESTRICTED")
    void shouldAllowWhenUnrestricted() {
        ControlledFundsPolicy policy = ControlledFundsPolicy.create(
                1L, 100L, 10L, Environment.LIVE, "v1.0",
                ControlMode.UNRESTRICTED, null, null, null, null, null, null, null, null
        );
        policy.activate("ADMIN");

        FundUsageEvaluationService.EvaluationResult result = evaluationService.evaluate(
                policy, "ANY_CATEGORY", "ACC-123", Money.of(new BigDecimal("10000"), CurrencyCode.NGN), PaymentContext.PAYOUT
        );

        assertEquals(ControlAction.ALLOW, result.evaluatedAction());
        assertTrue(result.reasons().isEmpty());
    }

    @Test
    @DisplayName("Should block payment when category is restricted")
    void shouldBlockWhenCategoryIsRestricted() {
        ControlledFundsPolicy policy = ControlledFundsPolicy.create(
                1L, 100L, 10L, Environment.LIVE, "v1.0",
                ControlMode.PURPOSE_RESTRICTED,
                Set.of(new PermittedCategory("RAW_MATERIALS", "Raw Materials")),
                Set.of(new RestrictedCategory("GAMBLING", "Prohibited")),
                null, null, null, null, null, null
        );
        policy.activate("ADMIN");

        FundUsageEvaluationService.EvaluationResult result = evaluationService.evaluate(
                policy, "GAMBLING", "ACC-123", Money.of(new BigDecimal("10000"), CurrencyCode.NGN), PaymentContext.PAYOUT
        );

        assertEquals(ControlAction.BLOCK, result.evaluatedAction());
        assertTrue(result.reasons().contains(ControlReason.CATEGORY_RESTRICTED));
    }

    @Test
    @DisplayName("Should hold payment when beneficiary is not approved in purpose restricted mode")
    void shouldHoldWhenBeneficiaryNotApproved() {
        ControlledFundsPolicy policy = ControlledFundsPolicy.create(
                1L, 100L, 10L, Environment.LIVE, "v1.0",
                ControlMode.PURPOSE_RESTRICTED,
                Set.of(new PermittedCategory("RAW_MATERIALS", "Raw Materials")),
                null,
                new BeneficiaryRuleSet(false, Set.of("APPROVED-SUPPLIER"), Set.of("FRAUD-ACC")),
                null, null, null, null, null
        );
        policy.activate("ADMIN");

        FundUsageEvaluationService.EvaluationResult result = evaluationService.evaluate(
                policy, "RAW_MATERIALS", "UNKNOWN-ACC", Money.of(new BigDecimal("10000"), CurrencyCode.NGN), PaymentContext.PAYOUT
        );

        assertEquals(ControlAction.HOLD, result.evaluatedAction());
        assertTrue(result.reasons().contains(ControlReason.BENEFICIARY_NOT_APPROVED));
    }

    @Test
    @DisplayName("Should hold withdrawal when amount exceeds limit")
    void shouldHoldWhenWithdrawalExceedsLimit() {
        ControlledFundsPolicy policy = ControlledFundsPolicy.create(
                1L, 100L, 10L, Environment.LIVE, "v1.0",
                ControlMode.PURPOSE_RESTRICTED,
                null, null, null,
                new WithdrawalRuleSet(Money.of(new BigDecimal("50000"), CurrencyCode.NGN), null, false),
                null, null, null, null
        );
        policy.activate("ADMIN");

        FundUsageEvaluationService.EvaluationResult result = evaluationService.evaluate(
                policy, null, "ANY-ACC", Money.of(new BigDecimal("60000"), CurrencyCode.NGN), PaymentContext.WITHDRAWAL
        );

        assertEquals(ControlAction.HOLD, result.evaluatedAction());
        assertTrue(result.reasons().contains(ControlReason.SINGLE_AMOUNT_EXCEEDED));
    }
}
