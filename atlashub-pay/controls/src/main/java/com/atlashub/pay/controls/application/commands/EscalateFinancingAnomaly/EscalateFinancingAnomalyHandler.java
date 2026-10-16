package com.atlashub.pay.controls.application.commands.EscalateFinancingAnomaly;

import com.atlashub.pay.controls.application.commands.OpenControlCase.OpenControlCaseCommand;
import com.atlashub.pay.controls.application.commands.OpenControlCase.OpenControlCaseHandler;
import com.atlashub.pay.controls.domain.entities.ControlledFundsPolicy;
import com.atlashub.pay.controls.domain.entities.PaymentControlDecision;
import com.atlashub.pay.controls.domain.repositories.ControlledFundsPolicyRepository;
import com.atlashub.pay.controls.domain.repositories.PaymentControlDecisionRepository;
import com.atlashub.pay.controls.domain.services.ControlEscalationService;
import com.atlashub.pay.controls.domain.valueobject.ControlAction;
import com.atlashub.pay.controls.domain.valueobject.EvidenceRequirement;
import com.atlashub.pay.controls.domain.valueobject.PaymentContext;
import com.atlashub.pay.controls.domain.valueobject.PaymentRail;
import com.atlashub.shared.application.usecase.Command;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class EscalateFinancingAnomalyHandler extends Command<EscalateFinancingAnomalyCommand, Void> {

    private final ControlledFundsPolicyRepository policyRepository;
    private final ControlEscalationService escalationService;
    private final PaymentControlDecisionRepository decisionRepository;
    private final OpenControlCaseHandler openControlCaseHandler;

    public EscalateFinancingAnomalyHandler(ControlledFundsPolicyRepository policyRepository,
                                           ControlEscalationService escalationService,
                                           PaymentControlDecisionRepository decisionRepository,
                                           OpenControlCaseHandler openControlCaseHandler) {
        this.policyRepository = policyRepository;
        this.escalationService = escalationService;
        this.decisionRepository = decisionRepository;
        this.openControlCaseHandler = openControlCaseHandler;
    }

    @Override
    @Transactional
    public Void execute(EscalateFinancingAnomalyCommand input) {
        Optional<ControlledFundsPolicy> policyOpt = policyRepository.findActiveByFacilityId(input.facilityId());
        if (policyOpt.isEmpty()) {
            return null;
        }

        ControlledFundsPolicy policy = policyOpt.get();
        ControlEscalationService.EscalationOutcome outcome = escalationService.evaluateAnomaly(policy, input.severity());

        if (outcome.suggestedAction() != ControlAction.ALLOW) {
            Long decisionId = decisionRepository.nextIdentity();
            String reference = "ANOMALY-" + UUID.randomUUID().toString().substring(0, 8);
            PaymentControlDecision decision = PaymentControlDecision.evaluate(
                    decisionId,
                    policy.getId(),
                    input.facilityId(),
                    input.organizationId(),
                    reference,
                    PaymentRail.INTERNAL_LEDGER,
                    PaymentContext.EXPENSE,
                    outcome.suggestedAction(),
                    outcome.suggestedAction(),
                    outcome.reasons(),
                    input.riskSignalId()
            );

            decisionRepository.save(decision);

            List<EvidenceRequirement> requirements = List.of(
                    new EvidenceRequirement("INVOICE_OR_RECEIPT", "Provide business purpose justification for anomaly flag", true)
            );
            openControlCaseHandler.execute(new OpenControlCaseCommand(
                    decision.getId(),
                    input.organizationId(),
                    requirements
            ));
        }

        return null;
    }
}
