package com.atlashub.analytics.application.query.GetPayrollCostTrend;

import com.atlashub.analytics.domain.entities.PayrollCostProjection;
import com.atlashub.analytics.domain.repositories.PayrollCostProjectionRepository;
import com.atlashub.shared.application.usecase.Query;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class GetPayrollCostTrendHandler extends Query<GetPayrollCostTrendQuery, List<MonthlyPayrollCostResult>> {

    private final PayrollCostProjectionRepository repository;

    public GetPayrollCostTrendHandler(PayrollCostProjectionRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<MonthlyPayrollCostResult> execute(GetPayrollCostTrendQuery query) {
        String env = query.environment() != null ? query.environment() : "live";
        List<PayrollCostProjection> list = repository.findByOrgEnvAndYear(
                query.organizationId(),
                env,
                query.year()
        );

        return list.stream()
                .map(p -> new MonthlyPayrollCostResult(
                        p.getMetricMonth(),
                        p.getTotalGrossPayroll(),
                        p.getTotalDeductions(),
                        p.getTotalNetPayroll(),
                        p.getHeadcount()
                ))
                .collect(Collectors.toList());
    }
}
