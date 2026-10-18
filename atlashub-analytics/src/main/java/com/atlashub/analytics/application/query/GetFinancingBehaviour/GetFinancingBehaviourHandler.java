package com.atlashub.analytics.application.query.GetFinancingBehaviour;

import com.atlashub.analytics.domain.entities.FinancingBehaviourProjection;
import com.atlashub.analytics.domain.repositories.FinancingBehaviourProjectionRepository;
import com.atlashub.shared.application.usecase.Query;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Component
public class GetFinancingBehaviourHandler extends Query<GetFinancingBehaviourQuery, FinancingBehaviourResult> {

    private final FinancingBehaviourProjectionRepository repository;

    public GetFinancingBehaviourHandler(FinancingBehaviourProjectionRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(readOnly = true)
    public FinancingBehaviourResult execute(GetFinancingBehaviourQuery query) {
        String env = query.environment() != null ? query.environment() : "live";
        FinancingBehaviourProjection projection = repository.findByOrgEnvAndContextKey(
                query.organizationId(),
                env,
                query.paymentContextKey()
        ).orElseGet(() -> FinancingBehaviourProjection.create(
                null,
                query.organizationId(),
                env,
                query.paymentContextKey()
        ));

        return new FinancingBehaviourResult(
                projection.getOrganizationId(),
                projection.getEnvironment(),
                projection.getPaymentContextKey(),
                projection.getTotalInflow() != null ? projection.getTotalInflow() : BigDecimal.ZERO,
                projection.getTotalOutflow() != null ? projection.getTotalOutflow() : BigDecimal.ZERO,
                projection.getAverageDailyBalance() != null ? projection.getAverageDailyBalance() : BigDecimal.ZERO,
                projection.getRepaymentSuccessRate() != null ? projection.getRepaymentSuccessRate() : BigDecimal.ZERO,
                projection.getAnomalyCount()
        );
    }
}
