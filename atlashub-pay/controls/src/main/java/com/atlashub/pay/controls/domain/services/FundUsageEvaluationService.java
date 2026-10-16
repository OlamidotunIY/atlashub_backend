package com.atlashub.pay.controls.domain.services;

import com.atlashub.pay.controls.domain.entities.ControlledFundsPolicy;
import com.atlashub.pay.controls.domain.valueobject.AmountFrequencyRuleSet;
import com.atlashub.pay.controls.domain.valueobject.BeneficiaryRuleSet;
import com.atlashub.pay.controls.domain.valueobject.ControlAction;
import com.atlashub.pay.controls.domain.valueobject.ControlMode;
import com.atlashub.pay.controls.domain.valueobject.ControlReason;
import com.atlashub.pay.controls.domain.valueobject.PaymentContext;
import com.atlashub.pay.controls.domain.valueobject.PolicyStatus;
import com.atlashub.pay.controls.domain.valueobject.WithdrawalRuleSet;
import com.atlashub.shared.domain.valueobject.Money;

import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class FundUsageEvaluationService {

    public record EvaluationResult(
            ControlAction evaluatedAction,
            Set<ControlReason> reasons
    ) {}

    public EvaluationResult evaluate(ControlledFundsPolicy policy,
                                     String categoryCode,
                                     String beneficiary,
                                     Money amount,
                                     PaymentContext paymentContext) {
        Set<ControlReason> reasons = new HashSet<>();

        if (policy.getStatus() == PolicyStatus.DRAFT || policy.getStatus() == PolicyStatus.SUSPENDED) {
            reasons.add(policy.getStatus() == PolicyStatus.SUSPENDED ? ControlReason.POLICY_SUSPENDED : ControlReason.POLICY_NOT_ACTIVE);
            return new EvaluationResult(ControlAction.BLOCK, reasons);
        }
        if (policy.getStatus() == PolicyStatus.EXPIRED || !policy.isEffective(ZonedDateTime.now())) {
            reasons.add(ControlReason.POLICY_EXPIRED);
            return new EvaluationResult(ControlAction.BLOCK, reasons);
        }

        ControlMode mode = policy.getMode();
        if (mode == ControlMode.UNRESTRICTED) {
            return new EvaluationResult(ControlAction.ALLOW, Collections.emptySet());
        }

        if (mode == ControlMode.MONITORED) {
            BeneficiaryRuleSet benRules = policy.getBeneficiaryRules();
            if (benRules != null && beneficiary != null && benRules.blockedBeneficiaries().contains(beneficiary)) {
                reasons.add(ControlReason.BENEFICIARY_BLOCKED);
                return new EvaluationResult(ControlAction.REVIEW, reasons);
            }
            return new EvaluationResult(ControlAction.ALLOW, Collections.emptySet());
        }

        // Check restricted categories
        if (categoryCode != null && policy.getRestrictedCategories() != null) {
            boolean isRestricted = policy.getRestrictedCategories().stream()
                    .anyMatch(r -> r.code().equalsIgnoreCase(categoryCode));
            if (isRestricted) {
                reasons.add(ControlReason.CATEGORY_RESTRICTED);
                return new EvaluationResult(ControlAction.BLOCK, reasons);
            }
        }

        // Check permitted categories
        if (mode == ControlMode.PURPOSE_RESTRICTED && categoryCode != null && policy.getPermittedCategories() != null && !policy.getPermittedCategories().isEmpty()) {
            boolean isPermitted = policy.getPermittedCategories().stream()
                    .anyMatch(p -> p.code().equalsIgnoreCase(categoryCode));
            if (!isPermitted) {
                reasons.add(ControlReason.CATEGORY_NOT_PERMITTED);
                return new EvaluationResult(ControlAction.BLOCK, reasons);
            }
        }

        // Check beneficiary rules
        BeneficiaryRuleSet benRules = policy.getBeneficiaryRules();
        if (benRules != null && beneficiary != null) {
            if (benRules.blockedBeneficiaries().contains(beneficiary)) {
                reasons.add(ControlReason.BENEFICIARY_BLOCKED);
                return new EvaluationResult(ControlAction.BLOCK, reasons);
            }
            if (!benRules.allowAnyBeneficiary() && !benRules.approvedBeneficiaries().contains(beneficiary)) {
                reasons.add(ControlReason.BENEFICIARY_NOT_APPROVED);
                if (mode == ControlMode.PURPOSE_RESTRICTED) {
                    return new EvaluationResult(ControlAction.HOLD, reasons);
                } else {
                    return new EvaluationResult(ControlAction.REVIEW, reasons);
                }
            }
        }

        // Check withdrawal rules
        if (paymentContext == PaymentContext.WITHDRAWAL) {
            WithdrawalRuleSet withdrawalRules = policy.getWithdrawalRules();
            if (withdrawalRules != null) {
                if (amount != null && withdrawalRules.maxSingleWithdrawal() != null &&
                        amount.isGreaterThan(withdrawalRules.maxSingleWithdrawal())) {
                    reasons.add(ControlReason.SINGLE_AMOUNT_EXCEEDED);
                    return new EvaluationResult(ControlAction.HOLD, reasons);
                }
                if (withdrawalRules.requiresApproval()) {
                    reasons.add(ControlReason.WITHDRAWAL_APPROVAL_REQUIRED);
                    return new EvaluationResult(ControlAction.HOLD, reasons);
                }
            }
        }

        // Check amount rules
        AmountFrequencyRuleSet amountRules = policy.getAmountAndFrequencyRules();
        if (amountRules != null && amount != null) {
            if (amountRules.maxTransactionAmount() != null && amount.isGreaterThan(amountRules.maxTransactionAmount())) {
                reasons.add(ControlReason.SINGLE_AMOUNT_EXCEEDED);
                return new EvaluationResult(mode == ControlMode.PURPOSE_RESTRICTED ? ControlAction.HOLD : ControlAction.REVIEW, reasons);
            }
        }

        return new EvaluationResult(ControlAction.ALLOW, Collections.emptySet());
    }
}
