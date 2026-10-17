package com.atlashub.analytics.infrastructure.persistence.adapters;

import com.atlashub.analytics.application.query.GetBusinessPerformance.BusinessPerformanceResult;
import com.atlashub.analytics.application.query.GetBusinessPerformance.GetBusinessPerformanceHandler;
import com.atlashub.analytics.application.query.GetBusinessPerformance.GetBusinessPerformanceQuery;
import com.atlashub.shared.application.port.BusinessPerformanceQueryPort;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Optional;

@Component
public class BusinessPerformanceQueryPortAdapter implements BusinessPerformanceQueryPort {

    private final GetBusinessPerformanceHandler handler;

    public BusinessPerformanceQueryPortAdapter(GetBusinessPerformanceHandler handler) {
        this.handler = handler;
    }

    @Override
    public Optional<BusinessPerformanceSnapshot> getPerformance(Long organizationId, String environment, LocalDate asOfDate) {
        if (organizationId == null) {
            return Optional.empty();
        }
        BusinessPerformanceResult result = handler.execute(
                new GetBusinessPerformanceQuery(organizationId, environment, asOfDate)
        );
        return Optional.of(new BusinessPerformanceSnapshot(
                result.organizationId(),
                result.environment(),
                result.asOfDate(),
                result.totalGrossRevenue(),
                result.totalNetRevenue(),
                result.totalTransactions(),
                result.averageOrderValue(),
                result.repeatCustomerRate(),
                result.activeOutletsCount()
        ));
    }
}
