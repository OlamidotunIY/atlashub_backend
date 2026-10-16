package com.atlashub.pay.controls.application.commands.EvaluatePaymentInstruction;

import com.atlashub.pay.controls.domain.entities.ControlledFundsPolicy;
import com.atlashub.pay.controls.domain.entities.PaymentControlDecision;
import com.atlashub.pay.controls.domain.exceptions.PolicyNotFoundException;
import com.atlashub.pay.controls.domain.repositories.ControlledFundsPolicyRepository;
import com.atlashub.pay.controls.domain.repositories.PaymentControlDecisionRepository;
import com.atlashub.pay.controls.domain.services.FundUsageEvaluationService;
import com.atlashub.pay.controls.domain.services.ProviderCapabilityService;
import com.atlashub.shared.application.usecase.Command;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class EvaluatePaymentInstructionHandler extends Command<EvaluatePaymentInstructionCommand, PaymentControlDecisionResult> {

    private final PaymentControlDecisionRepository decisionRepository;
    private final ControlledFundsPolicyRepository policyRepository;
    private final FundUsageEvaluationService evaluationService;
    private final ProviderCapabilityService capabilityService;

    public EvaluatePaymentInstructionHandler(PaymentControlDecisionRepository decisionRepository,
                                            ControlledFundsPolicyRepository policyRepository,
                                            FundUsageEvaluationService evaluationService,
                                            ProviderCapabilityService capabilityService) {
        this.decisionRepository = decisionRepository;
        this.policyRepository = policyRepository;
        this.evaluationService = evaluationService;
        this.capabilityService = capabilityService;
    }

    @Override
    @Transactional
    public PaymentControlDecisionResult execute(EvaluatePaymentInstructionCommand input) {
        Optional<PaymentControlDecision> existing = decisionRepository.findByPaymentInstructionReference(input.paymentInstructionReference());
        if (existing.isPresent()) {
            PaymentControlDecision d = existing.get();
            Set<String> reasons = d.getReasonCodes().stream().map(Enum::name).collect(Collectors.toSet());
            return new PaymentControlDecisionResult(
                    d.getId(),
                    d.getPolicyId(),
                    d.getFacilityId(),
                    d.getOrganizationId(),
                    d.getPaymentInstructionReference(),
                    d.getPaymentRail().name(),
                    d.getPaymentContext().name(),
                    d.getEvaluatedAction().name(),
                    d.getEnforcedAction().name(),
                    reasons,
                    d.getRiskSignalId(),
                    d.getProviderControlReference(),
                    d.getStatus().name(),
                    d.getEvaluatedAt(),
                    d.getAppliedAt(),
                    false,
                    null
            );
        }

        ControlledFundsPolicy policy = policyRepository.findActiveByFacilityId(input.facilityId())
                .orElseThrow(() -> new PolicyNotFoundException("Active controlled funds policy not found for facility: " + input.facilityId()));

        FundUsageEvaluationService.EvaluationResult evalResult = evaluationService.evaluate(
                policy,
                input.categoryCode(),
                input.beneficiary(),
                input.amount(),
                input.paymentContext()
        );

        ProviderCapabilityService.DowngradeResult downgrade = capabilityService.resolveEnforcedAction(
                evalResult.evaluatedAction(),
                input.paymentRail(),
                policy.getProviderCapabilities()
        );

        Long decisionId = decisionRepository.nextIdentity();
        PaymentControlDecision decision = PaymentControlDecision.evaluate(
                decisionId,
                policy.getId(),
                input.facilityId(),
                input.organizationId(),
                input.paymentInstructionReference(),
                input.paymentRail(),
                input.paymentContext(),
                evalResult.evaluatedAction(),
                downgrade.enforcedAction(),
                evalResult.reasons(),
                input.riskSignalId()
        );

        decisionRepository.save(decision);

        Set<String> reasons = decision.getReasonCodes().stream().map(Enum::name).collect(Collectors.toSet());
        return new PaymentControlDecisionResult(
                decision.getId(),
                decision.getPolicyId(),
                decision.getFacilityId(),
                decision.getOrganizationId(),
                decision.getPaymentInstructionReference(),
                decision.getPaymentRail().name(),
                decision.getPaymentContext().name(),
                decision.getEvaluatedAction().name(),
                decision.getEnforcedAction().name(),
                reasons,
                decision.getRiskSignalId(),
                decision.getProviderControlReference(),
                decision.getStatus().name(),
                decision.getEvaluatedAt(),
                decision.getAppliedAt(),
                downgrade.downgraded(),
                downgrade.reason()
        );
    }
}
