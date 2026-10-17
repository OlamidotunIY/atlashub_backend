package com.atlashub.analytics.application.query.GetOutletPerformance;

import com.atlashub.analytics.domain.entities.OutletPerformanceProjection;
import com.atlashub.analytics.domain.repositories.OutletPerformanceProjectionRepository;
import com.atlashub.shared.application.usecase.Query;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class GetOutletPerformanceHandler extends Query<GetOutletPerformanceQuery, List<OutletPerformanceResult>> {

    private final OutletPerformanceProjectionRepository repository;

    public GetOutletPerformanceHandler(OutletPerformanceProjectionRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<OutletPerformanceResult> execute(GetOutletPerformanceQuery query) {
        String env = query.environment() != null ? query.environment() : "live";
        List<OutletPerformanceProjection> list = repository.findByOrgEnvAndMonth(
                query.organizationId(),
                env,
                query.month()
        );

        return list.stream()
                .map(p -> new OutletPerformanceResult(
                        p.getOutletId(),
                        p.getMetricMonth(),
                        p.getTotalSales(),
                        p.getTotalDiscounts(),
                        p.getTotalExpenses(),
                        p.getGrossMargin(),
                        p.getNetProfit(),
                        p.getTransactionCount()
                ))
                .collect(Collectors.toList());
    }
}
