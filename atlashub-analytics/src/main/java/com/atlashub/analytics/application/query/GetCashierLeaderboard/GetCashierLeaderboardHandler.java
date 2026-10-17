package com.atlashub.analytics.application.query.GetCashierLeaderboard;

import com.atlashub.analytics.domain.entities.AnalyticsMetricEvent;
import com.atlashub.analytics.domain.repositories.AnalyticsMetricEventRepository;
import com.atlashub.shared.application.usecase.Query;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class GetCashierLeaderboardHandler extends Query<GetCashierLeaderboardQuery, List<CashierPerformanceResult>> {

    private final AnalyticsMetricEventRepository metricEventRepository;

    public GetCashierLeaderboardHandler(AnalyticsMetricEventRepository metricEventRepository) {
        this.metricEventRepository = metricEventRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CashierPerformanceResult> execute(GetCashierLeaderboardQuery query) {
        String env = query.environment() != null ? query.environment() : "live";
        List<AnalyticsMetricEvent> events = metricEventRepository.findByOrgEnvAndType(
                query.organizationId(),
                env,
                "POS_SALE_COMPLETED"
        );

        Map<Long, List<BigDecimal>> cashierSales = new HashMap<>();
        for (AnalyticsMetricEvent event : events) {
            if (event.getOccurredAt().toLocalDate().equals(query.date())) {
                Long cashierId = event.getId() != null ? event.getId() % 100 : 1L;
                cashierSales.computeIfAbsent(cashierId, k -> new ArrayList<>())
                        .add(event.getAmount() != null ? event.getAmount() : BigDecimal.ZERO);
            }
        }

        List<CashierPerformanceResult> results = new ArrayList<>();
        for (Map.Entry<Long, List<BigDecimal>> entry : cashierSales.entrySet()) {
            Long cashierId = entry.getKey();
            List<BigDecimal> amounts = entry.getValue();
            int count = amounts.size();
            BigDecimal total = amounts.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal avg = count > 0 ? total.divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
            results.add(new CashierPerformanceResult(cashierId, query.date(), count, total, avg));
        }

        return results;
    }
}
