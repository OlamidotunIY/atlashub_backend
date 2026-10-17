package com.atlashub.analytics.application.query.GetSalesSummary;

import com.atlashub.analytics.domain.entities.DailySalesProjection;
import com.atlashub.analytics.domain.repositories.DailySalesProjectionRepository;
import com.atlashub.shared.application.usecase.Query;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class GetSalesSummaryHandler extends Query<GetSalesSummaryQuery, List<DailySalesSummaryResult>> {

    private final DailySalesProjectionRepository repository;

    public GetSalesSummaryHandler(DailySalesProjectionRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<DailySalesSummaryResult> execute(GetSalesSummaryQuery query) {
        String env = query.environment() != null ? query.environment() : "live";
        List<DailySalesProjection> list = repository.findByOrgEnvAndDateRange(
                query.organizationId(),
                env,
                query.outletId(),
                query.from(),
                query.to()
        );

        return list.stream()
                .map(p -> new DailySalesSummaryResult(
                        p.getMetricDate(),
                        p.getTotalTransactions(),
                        p.getTotalGross(),
                        p.getTotalDiscounts(),
                        p.getTotalTax(),
                        p.getTotalNet(),
                        p.getCashSales(),
                        p.getCardSales(),
                        p.getCreditSales()
                ))
                .collect(Collectors.toList());
    }
}
