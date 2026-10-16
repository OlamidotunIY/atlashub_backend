package com.atlashub.pay.controls.application.queries.GetPaymentControlDecision;

import com.atlashub.pay.controls.domain.entities.PaymentControlDecision;
import com.atlashub.pay.controls.domain.exceptions.DecisionNotFoundException;
import com.atlashub.pay.controls.domain.repositories.PaymentControlDecisionRepository;
import com.atlashub.shared.application.usecase.Query;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

@Component
public class GetPaymentControlDecisionHandler extends Query<GetPaymentControlDecisionQuery, PaymentControlDecisionResult> {

    private final PaymentControlDecisionRepository decisionRepository;

    public GetPaymentControlDecisionHandler(PaymentControlDecisionRepository decisionRepository) {
        this.decisionRepository = decisionRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentControlDecisionResult execute(GetPaymentControlDecisionQuery input) {
        PaymentControlDecision decision = decisionRepository.findById(input.decisionId())
                .orElseThrow(() -> new DecisionNotFoundException("Payment control decision not found: " + input.decisionId()));

        Set<String> reasons = decision.getReasonCodes().stream()
                .map(Enum::name)
                .collect(Collectors.toSet());

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
                decision.getAppliedAt()
        );
    }
}
