package com.atlashub.pay.controls.application.queries.ListControlDecisions;

import com.atlashub.pay.controls.application.queries.GetPaymentControlDecision.PaymentControlDecisionResult;
import com.atlashub.pay.controls.domain.entities.PaymentControlDecision;
import com.atlashub.pay.controls.domain.repositories.PaymentControlDecisionRepository;
import com.atlashub.shared.application.usecase.Query;
import com.atlashub.shared.domain.valueobject.PageResult;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class ListControlDecisionsHandler extends Query<ListControlDecisionsQuery, PageResult<PaymentControlDecisionResult>> {

    private final PaymentControlDecisionRepository decisionRepository;

    public ListControlDecisionsHandler(PaymentControlDecisionRepository decisionRepository) {
        this.decisionRepository = decisionRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<PaymentControlDecisionResult> execute(ListControlDecisionsQuery input) {
        PageResult<PaymentControlDecision> page = decisionRepository.findAllByOrganizationId(
                input.organizationId(),
                input.pageNumber(),
                input.pageSize()
        );

        List<PaymentControlDecisionResult> results = page.content().stream()
                .map(d -> {
                    Set<String> reasons = d.getReasonCodes().stream()
                            .map(Enum::name)
                            .collect(Collectors.toSet());
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
                            d.getAppliedAt()
                    );
                })
                .collect(Collectors.toList());

        return new PageResult<>(
                results,
                page.pageNumber(),
                page.pageSize(),
                page.totalElements(),
                page.totalPages()
        );
    }
}
